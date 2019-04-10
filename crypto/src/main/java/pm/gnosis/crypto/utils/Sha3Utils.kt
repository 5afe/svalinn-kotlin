package pm.gnosis.crypto.utils

import org.bouncycastle.crypto.digests.KeccakDigest
import org.bouncycastle.crypto.digests.SHA3Digest
import org.bouncycastle.util.encoders.Hex


object Sha3Utils {
    private val DEFAULT_SIZE = 256

    fun sha3String(message: ByteArray): String {
        return hashToString(message, SHA3Digest(DEFAULT_SIZE))
    }

    fun sha3(message: ByteArray): ByteArray {
        return doDigest(message, SHA3Digest(DEFAULT_SIZE))
    }

    fun keccak(message: ByteArray): ByteArray {
        return doDigest(message, KeccakDigest(DEFAULT_SIZE))
    }

    private fun hashToString(message: ByteArray, digest: KeccakDigest): String {
        val hash = doDigest(message, digest)
        return Hex.toHexString(hash)
    }

    private fun doDigest(message: ByteArray, digest: KeccakDigest): ByteArray {
        val hash = ByteArray(digest.digestSize)

        if (message.isNotEmpty()) {
            digest.update(message, 0, message.size)
        }
        digest.doFinal(hash, 0)
        return hash
    }
}
