package pm.gnosis.ethereum.rpc.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionCallParams(
    @Json(name = "chainId") val chainId: String? = null,
    @Json(name = "from") val from: String? = null,
    @Json(name = "to") val to: String? = null,
    @Json(name = "gas") val gas: String? = null,
    @Json(name = "gasPrice") val gasPrice: String? = null,
    @Json(name = "value") val value: String? = null,
    @Json(name = "data") val data: String? = null,
    @Json(name = "nonce") val nonce: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "maxPriorityFeePerGas") val maxPriorityFeePerGas: String? = null,
    @Json(name = "maxFeePerGas") val maxFeePerGas: String? = null,
    @Json(name = "accessList") val accessList: List<String> = listOf()
) {
    fun callRequest(id: Int, block: String = "latest"): JsonRpcRequest {
        return JsonRpcRequest(
            id = id, method = "eth_call",
            params = arrayListOf(this, block)
        )
    }
}
