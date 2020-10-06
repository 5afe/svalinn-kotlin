package pm.gnosis.svalinn.security.impls

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import pm.gnosis.svalinn.security.*
import pm.gnosis.utils.nullOnThrow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AndroidFingerprintHelper(private val context: Context) : FingerprintHelper {
    private val keyStore by lazy { KeyStore.getInstance(ANDROID_KEY_STORE) }
    private val keyGenerator by lazy { KeyGenerator.getInstance(AES, ANDROID_KEY_STORE) }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createKey(): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            FINGERPRINT_KEY,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        keyGenerator.init(builder.setKeySize(256).build())
        return keyGenerator.generateKey()
    }

    override fun removeKey() {
        keyStore.load(null)
        keyStore.deleteEntry(FINGERPRINT_KEY)
    }

    @SuppressLint("MissingPermission")
    override fun systemHasFingerprintsEnrolled() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                nullOnThrow { context.getSystemService(KeyguardManager::class.java).isKeyguardSecure } ?: false &&
                nullOnThrow { FingerprintManagerCompat.from(context).hasEnrolledFingerprints() } ?: false

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateKey(): SecretKey {
        keyStore.load(null)
        return keyStore.getKey(FINGERPRINT_KEY, null) as? SecretKey ?: createKey()
    }

    override fun isKeySet(): Boolean {
        keyStore.load(null)
        return keyStore.getKey(FINGERPRINT_KEY, null) != null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createCipher(iv: ByteArray?) =
        Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
            .apply {
                if (iv == null) init(Cipher.ENCRYPT_MODE, getOrCreateKey())
                else init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))
            }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun authenticate(iv: ByteArray?): AuthenticationResult {
        val cryptoObject = FingerprintManagerCompat.CryptoObject(createCipher(iv))
        val fingerprintManager = FingerprintManagerCompat.from(context)
        val signal = CancellationSignal()
        return suspendCoroutine { cont ->
            val callback = object:  FingerprintManagerCompat.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                    result?.cryptoObject?.cipher?.let {
                        cont.resume(AuthenticationResultSuccess(it))
                    } ?: run {
                        cont.resumeWithException(IllegalStateException("Cipher is null"))
                    }
                }

                override fun onAuthenticationFailed() {
                    cont.resume(AuthenticationFailed())
                }

                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                    cont.resumeWithException(AuthenticationError(errMsgId, errString))
                }

                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                    cont.resume(AuthenticationHelp(helpMsgId, helpString))
                }
            }
            fingerprintManager.authenticate(cryptoObject, 0, signal, callback, null)
        }
    }

    companion object {
        private const val AES = "AES"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val FINGERPRINT_KEY = "GnosisFingerprintKey"
    }
}
