package pm.gnosis.svalinn.security

import javax.crypto.Cipher

interface FingerprintHelper {
    fun removeKey()
    fun systemHasFingerprintsEnrolled(): Boolean
    fun isKeySet(): Boolean
    suspend fun authenticate(iv: ByteArray? = null): AuthenticationResult
}

sealed class AuthenticationResult
class AuthenticationFailed : AuthenticationResult()
data class AuthenticationError(val errMsgId: Int, val errString: CharSequence?) : IllegalArgumentException()
data class AuthenticationHelp(val helpMsgId: Int, val helpString: CharSequence?) : AuthenticationResult()
data class AuthenticationResultSuccess(val cipher: Cipher) : AuthenticationResult()
class FingerprintNotAvailable(message: String? = null) : Exception(message)
