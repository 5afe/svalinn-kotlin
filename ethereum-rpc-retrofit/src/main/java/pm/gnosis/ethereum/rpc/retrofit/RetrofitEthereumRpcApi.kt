package pm.gnosis.ethereum.rpc.retrofit

import pm.gnosis.ethereum.rpc.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitEthereumRpcApi {
    @POST(".")
    fun receipt(@Body jsonRpcRequest: JsonRpcRequest): Response<JsonRpcTransactionReceiptResult>

    @POST(".")
    fun block(@Body jsonRpcRequest: JsonRpcRequest): Response<JsonRpcBlockResult>

    @POST(".")
    fun transaction(@Body jsonRpcRequest: JsonRpcRequest): Response<JsonRpcTransactionResult>

    @POST(".")
    fun post(@Body jsonRpcRequest: JsonRpcRequest): Response<JsonRpcResult>

    @POST(".")
    fun post(@Body jsonRpcRequest: Collection<JsonRpcRequest>): Response<Collection<JsonRpcResult>>
}
