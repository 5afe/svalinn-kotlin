package pm.gnosis.ethereum.rpc.retrofit

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*


class RetrofitEthereumRpcConnector(private val api: RetrofitEthereumRpcApi) : EthereumRpcConnector {

    override fun receipt(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult> =
        api.receipt(jsonRpcRequest)

    override fun block(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcBlockResult> =
        api.block(jsonRpcRequest)

    override fun transaction(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionResult> =
        api.transaction(jsonRpcRequest)

    override fun post(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult> =
        api.post(jsonRpcRequest)

    override fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>> =
        api.post(jsonRpcRequest)
}
