package pm.gnosis.models

import pm.gnosis.model.Solidity
import java.math.BigInteger

data class TransactionEip1559(
    val chainId: BigInteger,
    val from: Solidity.Address? = null,
    val to: Solidity.Address,
    val value: Wei? = null,
    val data: String? = null,
    val nonce: BigInteger? = null,
    var fee: Fee1559 = Fee1559(),
    val accessList: List<Pair<String, List<String>>> = emptyList()
) {
    val type: BigInteger = BigInteger.valueOf(2)
}

data class Fee1559(
    val gas: BigInteger? = null,
    val maxFeePerGas: BigInteger? = null,
    val maxPriorityFee: BigInteger? = null
)
