package pm.gnosis.ethereum.rpc.retrofit

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*

class RetrofitEthereumRpcConnector(private val api: RetrofitEthereumRpcApi) : EthereumRpcConnectorRxAdapter() {

    override fun receiptRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult> =
        api.receipt(jsonRpcRequest)

    override fun blockRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcBlockResult> =
        api.block(jsonRpcRequest)

    override fun transactionRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionResult> =
        api.transaction(jsonRpcRequest)

    override fun postRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult> =
        api.post(jsonRpcRequest)

    override fun postRx(jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>> =
        api.post(jsonRpcRequest)
}
