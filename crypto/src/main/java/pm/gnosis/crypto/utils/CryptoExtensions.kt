package pm.gnosis.crypto.utils

import okio.ByteString
import org.spongycastle.jcajce.provider.digest.RIPEMD160
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.removeHexPrefix
import java.lang.AssertionError
import java.security.NoSuchAlgorithmException


fun ByteString.hash160(): ByteString {
    try {
        val digest = RIPEMD160.Digest().digest(sha256().toByteArray())
        return ByteString.of(digest, 0, digest.size)
    } catch (e: NoSuchAlgorithmException) {
        throw AssertionError(e)
    }
}

fun Solidity.Address.asEthereumAddressChecksumString() =
    asEthereumAddressString().removeHexPrefix().run {
        val checksum = Sha3Utils.keccak(toByteArray())
        foldIndexed(StringBuilder("0x"), { index, stringBuilder, char ->
            stringBuilder.append(
                when {
                    char in '0'..'9' -> char
                    checksum.hexCharValue(index) >= 8 -> char.toUpperCase()
                    else -> char.toLowerCase()
                }
            )
        }).toString()
    }

private fun ByteArray.hexCharValue(position: Int) = (get(position / 2).toInt() ushr (4 * ((position + 1) % 2))) and 0x0F
