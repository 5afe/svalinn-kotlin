package pm.gnosis.models

import pm.gnosis.model.Solidity
import pm.gnosis.utils.hexAsBigInteger
import java.math.BigInteger

sealed class Transaction {
    abstract val chainId: BigInteger
    abstract val to: Solidity.Address
    abstract val from: Solidity.Address?
    abstract val value: Wei?
    abstract val data: String?
    abstract var nonce: BigInteger?
    abstract var gas: BigInteger?

    data class Legacy(
        override val chainId: BigInteger = CHAIN_ID_ANY,
        override val to: Solidity.Address,
        override val from: Solidity.Address? = null,
        override val value: Wei? = null,
        override val data: String? = null,
        override var nonce: BigInteger? = null,
        override var gas: BigInteger? = null,
        var gasPrice: BigInteger? = null,
    ) : Transaction() {
        fun signable() = nonce != null && gas != null && gasPrice != null

        fun parcelable() = TransactionLegacyParcelable(this)
    }

    data class Eip1559(
        override val chainId: BigInteger = CHAIN_ID_ANY,
        override val to: Solidity.Address,
        override val from: Solidity.Address? = null,
        override val value: Wei? = null,
        override val data: String? = null,
        override var nonce: BigInteger? = null,
        override var gas: BigInteger? = null,
        var maxFeePerGas: BigInteger? = null,
        var maxPriorityFee: BigInteger? = null,
        val accessList: List<Pair<String, List<String>>> = emptyList()
    ) : Transaction() {
        // Type of a transaction with priority fee. Defined in EIP-1559
        val type: Byte = "0x02".hexAsBigInteger().toByte()
    }

    companion object {
        val CHAIN_ID_ANY = BigInteger.ZERO
    }
}
