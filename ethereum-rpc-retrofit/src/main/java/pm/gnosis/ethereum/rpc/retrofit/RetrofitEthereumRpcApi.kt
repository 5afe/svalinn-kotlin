package pm.gnosis.ethereum.rpc.retrofit

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitEthereumRpcApi {
    @POST(".")
    fun receipt(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult>

    @POST(".")
    fun block(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcBlockResult>

    @POST(".")
    fun transaction(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionResult>

    @POST(".")
    fun post(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult>

    @POST(".")
    fun post(@Body jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>>
}


interface CoRetrofitEthereumRpcApi {
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
