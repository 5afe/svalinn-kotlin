package pm.gnosis.ethereum.models

import pm.gnosis.model.Solidity
import java.math.BigInteger


data class TransactionReceipt(
    val status: BigInteger,
    val transactionHash: String,
    val transactionIndex: BigInteger,
    val blockHash: String,
    val blockNumber: BigInteger,
    val from: Solidity.Address,
    val to: Solidity.Address,
    val cumulativeGasUsed: BigInteger,
    val gasUsed: BigInteger,
    val contractAddress: Solidity.Address?,
    val logs: List<Event>
) {
    data class Event(
        val logIndex: BigInteger,
        val data: String,
        val topics: List<String>
    )
}
