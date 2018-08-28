package pm.gnosis.svalinn.security.impls

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import pm.gnosis.svalinn.security.KeyStorage
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

class AndroidKeyStorage(
    private val context: Context
): KeyStorage {

    private val keyStore by lazy { KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) } }

    init {
        // If we don't have a key we will generate one on setup.
        getKey() ?: kotlin.run {
            generateKey()
        }
    }

    private fun KeyPairGenerator.initGeneratorWithKeyPairGeneratorSpec() {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 20)

        val builder = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setSerialNumber(BigInteger.ONE)
            .setSubject(X500Principal("CN=$KEY_ALIAS CA Certificate"))
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)

        initialize(builder.build())
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun KeyPairGenerator.initGeneratorWithKeyGenParameterSpec() {
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        initialize(builder.build())
    }

    private fun generateKey() {
        KeyPairGenerator.getInstance(RSA, ANDROID_KEY_STORE).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                initGeneratorWithKeyPairGeneratorSpec()
            else
                initGeneratorWithKeyGenParameterSpec()
            genKeyPair()
        }
    }

    private fun getKey(): KeyPair? {
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey ?: return null
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey ?: return null

        return KeyPair(publicKey, privateKey)
    }

    override fun store(key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)
        cipher.init(Cipher.ENCRYPT_MODE, getKey()?.public ?: return key)
        return cipher.doFinal(key)
    }

    override fun retrieve(id: ByteArray): ByteArray? {
        val cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)
        cipher.init(Cipher.DECRYPT_MODE, getKey()?.private ?: return null)
        return cipher.doFinal(id)
    }

    companion object {
        private const val RSA = "RSA"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "AndroidKeyStorageKey"
        private const val TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"
    }
}
