package pm.gnosis.ethereum.rpc.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import pm.gnosis.model.Solidity
import java.math.BigInteger

@JsonClass(generateAdapter = true)
data class JsonRpcRequest(
    @Json(name = "jsonrpc") val jsonRpc: String = "2.0",
    @Json(name = "method") val method: String,
    @Json(name = "params") val params: List<Any> = emptyList(),
    @Json(name = "id") val id: Int = 1
)

@JsonClass(generateAdapter = true)
data class JsonRpcResult(
    @Json(name = "id") val id: Int,
    @Json(name = "jsonrpc") val jsonRpc: String,
    @Json(name = "error") val error: JsonRpcError? = null,
    @Json(name = "result") val result: String?
)

@JsonClass(generateAdapter = true)
data class JsonRpcError(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class JsonRpcTransactionReceiptResult(
    @Json(name = "id") val id: Int,
    @Json(name = "jsonrpc") val jsonRpc: String,
    @Json(name = "error") val error: JsonRpcError? = null,
    @Json(name = "result") val result: TransactionReceipt?
) {
    @JsonClass(generateAdapter = true)
    data class TransactionReceipt(
        @Json(name = "status") val status: BigInteger,
        @Json(name = "transactionHash") val transactionHash: String,
        @Json(name = "transactionIndex") val transactionIndex: BigInteger,
        @Json(name = "blockHash") val blockHash: String,
        @Json(name = "blockNumber") val blockNumber: BigInteger,
        @Json(name = "from") val from: Solidity.Address,
        @Json(name = "to") val to: Solidity.Address,
        @Json(name = "cumulativeGasUsed") val cumulativeGasUsed: BigInteger,
        @Json(name = "gasUsed") val gasUsed: BigInteger,
        @Json(name = "contractAddress") val contractAddress: Solidity.Address?,
        @Json(name = "logs") val logs: List<Event>
    ) {
        @JsonClass(generateAdapter = true)
        data class Event(
            @Json(name = "logIndex") val logIndex: BigInteger,
            @Json(name = "data") val data: String,
            @Json(name = "topics") val topics: List<String>
        )
    }
}

@JsonClass(generateAdapter = true)
data class JsonRpcTransactionResult(
    @Json(name = "id") val id: Int,
    @Json(name = "jsonrpc") val jsonRpc: String,
    @Json(name = "error") val error: JsonRpcError? = null,
    @Json(name = "result") val result: JsonTransaction?
) {
    @JsonClass(generateAdapter = true)
    data class JsonTransaction(
        @Json(name = "hash") val hash: String,
        @Json(name = "nonce") val nonce: BigInteger,
        @Json(name = "blockHash") val blockHash: String?,
        @Json(name = "blockNumber") val blockNumber: BigInteger?,
        @Json(name = "transactionIndex") val transactionIndex: BigInteger?,
        @Json(name = "from") val from: Solidity.Address,
        @Json(name = "to") val to: Solidity.Address,
        @Json(name = "value") val value: BigInteger,
        @Json(name = "gasPrice") val gasPrice: BigInteger,
        @Json(name = "gas") val gas: BigInteger,
        @Json(name = "input") val data: String?
    )
}

@JsonClass(generateAdapter = true)
data class JsonRpcBlockResult(
    @Json(name = "id") val id: Int,
    @Json(name = "jsonrpc") val jsonRpc: String,
    @Json(name = "error") val error: JsonRpcError? = null,
    @Json(name = "result") val result: JsonBlock?
) {
    @JsonClass(generateAdapter = true)
    data class JsonBlock(
        @Json(name = "number") val number: BigInteger?,
        @Json(name = "hash") val hash: String?,
        @Json(name = "parentHash") val parentHash: String?,
        @Json(name = "nonce") val nonce: String?,
        @Json(name = "sha3Uncles") val sha3Uncles: String?,
        @Json(name = "logsBloom") val logsBloom: String?,
        @Json(name = "transactionsRoot") val transactionsRoot: String,
        @Json(name = "stateRoot") val stateRoot: String,
        @Json(name = "receiptsRoot") val receiptsRoot: String,
        @Json(name = "miner") val miner: Solidity.Address,
        @Json(name = "difficulty") val difficulty: BigInteger,
        @Json(name = "totalDifficulty") val totalDifficulty: BigInteger,
        @Json(name = "extraData") val extraData: String?,
        @Json(name = "size") val size: BigInteger,
        @Json(name = "gasLimit") val gasLimit: BigInteger,
        @Json(name = "gasUsed") val gasUsed: BigInteger,
        @Json(name = "timestamp") val timestamp: BigInteger
    )
}
