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

class CoRetrofitEthereumRpcConnector(private val api: CoRetrofitEthereumRpcApi) : EthereumRpcConnector {

    override suspend fun receipt(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult =
        api.receipt(jsonRpcRequest)


    override suspend fun block(jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult =
        api.block(jsonRpcRequest)

    override suspend fun transaction(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult =
        api.transaction(jsonRpcRequest)

    override suspend fun post(jsonRpcRequest: JsonRpcRequest): JsonRpcResult =
        api.post(jsonRpcRequest)

    override suspend fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult> =
        api.post(jsonRpcRequest)
}
