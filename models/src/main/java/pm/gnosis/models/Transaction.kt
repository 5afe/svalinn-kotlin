package pm.gnosis.models

import pm.gnosis.model.Solidity
import java.math.BigInteger

data class Transaction(
    val address: Solidity.Address,
    val value: Wei? = null,
    var gas: BigInteger? = null,
    var gasPrice: BigInteger? = null,
    val data: String? = null,
    var nonce: BigInteger? = null,
    val chainId: BigInteger = CHAIN_ID_ANY
) {
    fun signable() = nonce != null && gas != null && gasPrice != null

    fun parcelable() = TransactionParcelable(this)

    companion object {
        val CHAIN_ID_ANY = BigInteger.ZERO
    }
}
