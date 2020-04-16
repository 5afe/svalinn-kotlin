package pm.gnosis.ethereum.rpc.retrofit

import pm.gnosis.ethereum.rpc.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitEthereumRpcApi {
    @POST(".")
    suspend fun receipt(@Body jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult

    @POST(".")
    suspend fun block(@Body jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult

    @POST(".")
    suspend fun transaction(@Body jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult

    @POST(".")
    suspend fun post(@Body jsonRpcRequest: JsonRpcRequest): JsonRpcResult

    @POST(".")
    suspend fun post(@Body jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult>
}
