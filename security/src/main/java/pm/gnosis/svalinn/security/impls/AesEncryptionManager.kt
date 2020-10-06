package pm.gnosis.svalinn.security.impls

import android.app.Application
import android.os.Handler
import android.os.Looper
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.SCrypt
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.common.base.TrackingActivityLifecycleCallbacks
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.svalinn.security.AuthenticationFailed
import pm.gnosis.svalinn.security.AuthenticationHelp
import pm.gnosis.svalinn.security.AuthenticationResultSuccess
import pm.gnosis.svalinn.security.EncryptionManager
import pm.gnosis.svalinn.security.EncryptionManager.CryptoData
import pm.gnosis.svalinn.security.FingerprintHelper
import pm.gnosis.svalinn.security.FingerprintUnlockError
import pm.gnosis.svalinn.security.FingerprintUnlockFailed
import pm.gnosis.svalinn.security.FingerprintUnlockHelp
import pm.gnosis.svalinn.security.FingerprintUnlockResult
import pm.gnosis.svalinn.security.FingerprintUnlockSuccessful
import pm.gnosis.svalinn.security.KeyStorage
import pm.gnosis.svalinn.security.exceptions.DeviceIsLockedException
import pm.gnosis.utils.nullOnThrow
import pm.gnosis.utils.toHexString
import java.security.SecureRandom
import javax.crypto.spec.IvParameterSpec

/**
 * @param passwordIterations Number of iterations the password is hashed to prevent brute force attacks.
 *                           Will be disabled when set to 0 else has to be larger than 1, a power of 2 and less than <code>2^128</code>.
 */
class AesEncryptionManager(
    application: Application,
    private val preferencesManager: PreferencesManager,
    private val fingerprintHelper: FingerprintHelper,
    private val keyStorage: KeyStorage,
    private val passwordIterations: Int = SCRYPT_ITERATIONS
) : EncryptionManager {

    private val secureRandom = SecureRandom()
    private val keyLock = Any()
    private val handler = Handler(Looper.getMainLooper())
    private var key: ByteArray? = null
    private var lockRunnable: Runnable? = null

    init {
        application.registerActivityLifecycleCallbacks(object : TrackingActivityLifecycleCallbacks() {

            override fun active() {
                lockRunnable?.let {
                    handler.removeCallbacks(lockRunnable)
                }
                lockRunnable = null
            }

            override fun inactive() {
                val runnable = Runnable { lock() }
                handler.postDelayed(runnable, LOCK_DELAY_MS)
                lockRunnable = runnable
            }
        })
    }

    private fun randomIv(): ByteArray {
        val randomBytes = ByteArray(16)
        secureRandom.nextBytes(randomBytes)
        return randomBytes
    }

    override fun initialized(): Boolean {
        return preferencesManager.prefs.getString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, null) != null
    }

    private fun deriveKeyFromPassword(password: ByteArray): ByteArray =
        // If password iterations is set to 0 we will not use SCrypt
        if (passwordIterations == 0)
            Sha3Utils.sha3(password)
        else
            SCrypt.generate(password, Sha3Utils.sha3(password), passwordIterations, SCRYPT_BLOCK_SIZE, SCRYPT_PARALLELIZATION, SCRYPT_KEY_LENGTH)

    override fun setupPassword(newPassword: ByteArray, oldPassword: ByteArray?): Boolean {
        return synchronized(keyLock) {
            val checksum = preferencesManager.prefs.getString(PREF_KEY_PASSWORD_CHECKSUM, null)
            var previousKey: ByteArray? = null
            if (checksum != null) {
                previousKey = buildPasswordKeyIfValid(oldPassword, checksum) ?: return false
            }
            val passwordKey = deriveKeyFromPassword(newPassword)
            key = previousKey?.let {
                decryptAppKey(it)
            } ?: generateKey()
            key?.let {
                preferencesManager.prefs.edit { putString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, encrypt(passwordKey, it).toString()) }
                preferencesManager.prefs.edit { putString(PREF_KEY_PASSWORD_CHECKSUM, generateCryptedChecksum(passwordKey)) }
            }

            key != null
        }
    }

    private fun generateKey(): ByteArray {
        val generatedPassword = ByteArray(32)
        secureRandom.nextBytes(generatedPassword)
        return keyStorage.store(generatedPassword)
    }

    override fun unlocked(): Boolean {
        return synchronized(keyLock) {
            key != null
        }
    }

    override fun unlockWithPassword(password: ByteArray): Boolean {
        return synchronized(keyLock) {
            // If we have no password set (no checksum stored, we cannot unlockWithPassword
            val checksum = preferencesManager.prefs.getString(PREF_KEY_PASSWORD_CHECKSUM, null) ?: return false
            val passwordKey = buildPasswordKeyIfValid(password, checksum) ?: return false
            key = decryptAppKey(passwordKey) ?: return false
            key != null
        }
    }

    private fun decryptAppKey(key: ByteArray): ByteArray? {
        val encryptedKey = preferencesManager.prefs.getString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, null) ?: return null
        return decrypt(key, CryptoData.fromString(encryptedKey))
    }

    override fun lock() {
        synchronized(keyLock) {
            key = null
        }
    }

    override fun decrypt(data: CryptoData): ByteArray {
        val key = synchronized(keyLock) {
            this.key?.let {
                nullOnThrow { keyStorage.retrieve(it) } ?: it // Fallback if app was setup before storage existed
            } ?: throw DeviceIsLockedException()
        }
        return decrypt(key, data)
    }

    override fun encrypt(data: ByteArray): CryptoData {
        val key = synchronized(keyLock) {
            this.key?.let {
                nullOnThrow { keyStorage.retrieve(it) } ?: it // Fallback if app was setup before storage existed
            } ?: throw DeviceIsLockedException()
        }
        return encrypt(key, data)
    }

    private fun keyChecksum(key: ByteArray) =
        Sha3Utils.sha3String(key).substring(0, 6).toByteArray()

    private fun buildPasswordKeyIfValid(key: ByteArray?, checksum: String): ByteArray? {
        key ?: return null
        val hashedKey = deriveKeyFromPassword(key)
        val decryptedChecksum = nullOnThrow { decrypt(hashedKey, CryptoData.fromString(checksum)).toHexString() }
        if (keyChecksum(hashedKey).toHexString() == decryptedChecksum) {
            return hashedKey
        }
        return null
    }

    private fun generateCryptedChecksum(key: ByteArray): String {
        return encrypt(key, keyChecksum(key)).toString()
    }

    private fun encrypt(key: ByteArray, data: ByteArray): CryptoData {
        return useCipher(true, key, CryptoData(data, randomIv()))
    }

    private fun decrypt(key: ByteArray, data: CryptoData): ByteArray {
        return useCipher(false, key, data).data
    }

    override fun canSetupFingerprint() =
        nullOnThrow { fingerprintHelper.systemHasFingerprintsEnrolled() } ?: false

    override suspend fun setupFingerprint(): Boolean =
        fingerprintHelper.authenticate()
            .let { result ->
                when (result) {
                    is AuthenticationResultSuccess -> {
                        key?.let { key ->
                            preferencesManager.prefs.edit {
                                val cryptoData = CryptoData(
                                    result.cipher.doFinal(key),
                                    result.cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv
                                )
                                putString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, cryptoData.toString())
                            }
                            true
                        } ?: false
                    }
                    else -> false
                }
            }

    override suspend fun unlockWithFingerprint(): FingerprintUnlockResult {
        val cryptedData = CryptoData.fromString(
            preferencesManager.prefs.getString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, null) ?: throw FingerprintUnlockError()
        )
        val authResult = fingerprintHelper.authenticate(cryptedData.iv)
        return when (authResult) {
            is AuthenticationResultSuccess -> {
                synchronized(keyLock) {
                    key = authResult.cipher.doFinal(cryptedData.data)
                }
                if (key != null) FingerprintUnlockSuccessful else throw FingerprintUnlockError()
            }
            is AuthenticationFailed -> FingerprintUnlockFailed
            is AuthenticationHelp -> FingerprintUnlockHelp(authResult.helpString)
        }
    }

    override fun isFingerPrintSet(): Boolean =
        nullOnThrow { fingerprintHelper.isKeySet() } == true &&
                preferencesManager.prefs.getString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, null) != null

    override fun clearFingerprintData() {
        preferencesManager.prefs.edit {
            remove(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY)
        }
        fingerprintHelper.removeKey()
    }

    private fun useCipher(encrypt: Boolean, key: ByteArray, wrapper: CryptoData): CryptoData {
        val padding = PKCS7Padding()
        val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()), padding)
        cipher.reset()

        val keyParam = KeyParameter(key)
        val params = ParametersWithIV(keyParam, wrapper.iv)
        cipher.init(encrypt, params)

        // create a temporary buffer to decode into (it'll include padding)
        val buf = ByteArray(cipher.getOutputSize(wrapper.data.size))
        var len = cipher.processBytes(wrapper.data, 0, wrapper.data.size, buf, 0)
        len += cipher.doFinal(buf, len)

        // remove padding
        val out = ByteArray(len)
        System.arraycopy(buf, 0, out, 0, len)

        return CryptoData(out, wrapper.iv)
    }

    companion object {
        private const val SCRYPT_ITERATIONS = 16384
        private const val SCRYPT_BLOCK_SIZE = 8
        private const val SCRYPT_PARALLELIZATION = 1
        private const val SCRYPT_KEY_LENGTH = 32
        private const val LOCK_DELAY_MS = 5 * 60 * 1000L
        private const val PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY = "encryption_manager.string.password_encrypted_app_key"
        private const val PREF_KEY_PASSWORD_CHECKSUM = "encryption_manager.string.password_checksum"
        private const val PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY = "encryption_manager.string.fingerprint_encrypted_app_key"
    }
}
