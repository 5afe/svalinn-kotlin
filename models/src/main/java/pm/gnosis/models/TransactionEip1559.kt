package pm.gnosis.models

import pm.gnosis.model.Solidity
import java.math.BigInteger

data class TransactionEip1559(
    val type: BigInteger = BigInteger.valueOf(2),
    val chainId: BigInteger,
    val from: Solidity.Address? = null,
    val to: Solidity.Address,
    val value: Wei? = null,
    val data: String? = null,
    val nonce: BigInteger? = null,
    var fee: Fee1559 = Fee1559(),
    val hash: Solidity.Bytes32? = null,
    var signature: Solidity.Bytes? = null
)

data class Fee1559(
    val gas: BigInteger? = null,
    val maxFeePerGas: BigInteger? = null,
    val maxPriorityFee: BigInteger? = null
)
