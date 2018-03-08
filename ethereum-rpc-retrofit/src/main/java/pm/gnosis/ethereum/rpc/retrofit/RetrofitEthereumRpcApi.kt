package pm.gnosis.ethereum.rpc.retrofit

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.JsonRpcRequest
import pm.gnosis.ethereum.rpc.models.JsonRpcResult
import pm.gnosis.ethereum.rpc.models.JsonRpcTransactionReceiptResult
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitEthereumRpcApi {
    @POST("/")
    fun receipt(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult>

    @POST("/")
    fun post(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult>

    @POST("/")
    fun post(@Body jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>>
}
