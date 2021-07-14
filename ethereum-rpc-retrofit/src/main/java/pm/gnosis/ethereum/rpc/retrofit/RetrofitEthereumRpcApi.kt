package pm.gnosis.ethereum.rpc.retrofit

import pm.gnosis.ethereum.rpc.models.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RetrofitEthereumRpcApi {
    @POST
    suspend fun receipt(@Url rpcUrl: String, @Body jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult

    @POST
    suspend fun block(@Url rpcUrl: String, @Body jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult

    @POST
    suspend fun transaction(@Url rpcUrl: String, @Body jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult

    @POST
    suspend fun post(@Url rpcUrl: String, @Body jsonRpcRequest: JsonRpcRequest): JsonRpcResult

    @POST
    suspend fun post(@Url rpcUrl: String, @Body jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult>
}
