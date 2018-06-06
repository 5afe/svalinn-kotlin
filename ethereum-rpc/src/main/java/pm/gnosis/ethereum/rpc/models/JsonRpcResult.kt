package pm.gnosis.ethereum.rpc.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
    @Json(name = "result") val result: String
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
        @Json(name = "status") val status: BigInteger?,
        @Json(name = "transactionHash") val transactionHash: String,
        @Json(name = "contractAddress") val contractAddress: String?,
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
