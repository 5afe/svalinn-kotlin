package pm.gnosis.ethereum.rpc.retrofit

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.JsonRpcRequest
import pm.gnosis.ethereum.rpc.models.JsonRpcResult
import pm.gnosis.ethereum.rpc.models.JsonRpcTransactionReceiptResult


class RetrofitEthereumRpcConnector(private val api: RetrofitEthereumRpcApi) : EthereumRpcConnector {
    override fun receipt(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult> =
        api.receipt(jsonRpcRequest)

    override fun post(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult> =
        api.post(jsonRpcRequest)

    override fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>> =
        api.post(jsonRpcRequest)
}
