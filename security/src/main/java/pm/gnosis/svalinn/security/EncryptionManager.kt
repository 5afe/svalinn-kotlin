package pm.gnosis.svalinn.security

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import pm.gnosis.utils.hexStringToByteArrayOrNull
import pm.gnosis.utils.toHexString

interface EncryptionManager {
    fun decrypt(data: CryptoData): ByteArray
    fun encrypt(data: ByteArray): CryptoData
    fun unlocked(): Single<Boolean>
    fun unlockWithPassword(password: ByteArray): Single<Boolean>
    fun lock()
    fun setupPassword(newPassword: ByteArray, oldPassword: ByteArray? = null): Single<Boolean>
    fun initialized(): Single<Boolean>
    fun observeFingerprintForSetup(): Observable<Boolean>
    fun observeFingerprintForUnlock(): Observable<FingerprintUnlockResult>
    fun clearFingerprintData(): Completable
    fun isFingerPrintSet(): Single<Boolean>
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
