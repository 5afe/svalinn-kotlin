package pm.gnosis.crypto.utils

import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.removeHexPrefix
import pm.gnosis.utils.toHexString

fun Solidity.Address.asEthereumAddressChecksumString(): String {
    val address = asEthereumAddressString().removeHexPrefix()
    val keccak = Sha3Utils.keccak(address.toByteArray()).toHexString()

    val stringBuilder = StringBuilder("0x")
    address.forEachIndexed { index, c ->
        stringBuilder.append(
            when {
                c in '0'..'9' -> c
                java.lang.Integer.parseInt(keccak[index].toString(), 16) >= 8 -> c.toUpperCase()
                else -> c.toLowerCase()
            }
        )
    }

    return stringBuilder.toString()
}
