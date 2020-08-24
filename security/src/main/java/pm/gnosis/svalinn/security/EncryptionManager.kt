package pm.gnosis.svalinn.security

import pm.gnosis.utils.hexStringToByteArrayOrNull
import pm.gnosis.utils.toHexString

interface EncryptionManager {
    fun decrypt(data: CryptoData): ByteArray
    fun encrypt(data: ByteArray): CryptoData
    fun unlocked(): Boolean
    fun unlockWithPassword(password: ByteArray): Boolean
    fun lock()
    fun setupPassword(newPassword: ByteArray, oldPassword: ByteArray? = null): Boolean
    fun initialized(): Boolean
    suspend fun setupFingerprint(): Boolean
    suspend fun unlockWithFingerprint(): FingerprintUnlockResult
    fun clearFingerprintData()
    fun isFingerPrintSet(): Boolean
    fun canSetupFingerprint(): Boolean

    class CryptoData(val data: ByteArray, val iv: ByteArray) {
        override fun toString(): String {
            return "${data.toHexString()}$SEPARATOR${iv.toHexString()}"
        }

        companion object {
            const val SEPARATOR = "####"
            fun fromString(encoded: String) =
                encoded.split(SEPARATOR).let {
                    if (it.size != 2) throw IllegalArgumentException("Not correctly encoded!")
                    val data = it[0].hexStringToByteArrayOrNull() ?: throw IllegalArgumentException("Could not decode data!")
                    val iv = it[1].hexStringToByteArrayOrNull() ?: throw IllegalArgumentException("Could not decode iv!")
                    CryptoData(data, iv)
                }
        }
    }
}

interface KeyStorage {
    /**
     * Stores a key and returns an id to retrieve the key again
     * @param key to store
     * @return id to retrieve key
     */
    fun store(key: ByteArray): ByteArray

    /**
     * Retrieves a key by id from the storage
     * @param id of the key to retrieve
     * @return key or null if no key for this id was stored
     */
    fun retrieve(id: ByteArray): ByteArray?
}

sealed class FingerprintUnlockResult
class FingerprintUnlockSuccessful : FingerprintUnlockResult()
class FingerprintUnlockFailed : FingerprintUnlockResult()
class FingerprintUnlockError : IllegalArgumentException()
class FingerprintUnlockHelp(val message: CharSequence?) : FingerprintUnlockResult()
