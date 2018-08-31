package pm.gnosis.crypto

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException


object KeyGenerator {

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun masterNode(seed: ByteString): HDNode {
        val hash = seed.hmacSha512(MASTER_SECRET.encodeUtf8())
        return HDNode(KeyPair.fromPrivate(hash.substring(0, 32).toByteArray()), hash.substring(32), 0, 0, ByteString.of(0, 0, 0, 0))
    }

    const val BIP44_PATH_ETHEREUM = "m/44'/60'/0'/0"

    private const val MASTER_SECRET = "Bitcoin seed"

}
