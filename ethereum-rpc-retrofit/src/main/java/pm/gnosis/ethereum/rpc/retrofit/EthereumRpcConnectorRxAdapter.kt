package pm.gnosis.ethereum.rpc.retrofit

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*

abstract class EthereumRpcConnectorRxAdapter : EthereumRpcConnector {

    override suspend fun receipt(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult {
        return receiptRx(jsonRpcRequest).blockingFirst()
    }

    override suspend fun block(jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult {
        return blockRx(jsonRpcRequest).blockingFirst()
    }

    override suspend fun transaction(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult {
        return transactionRx(jsonRpcRequest).blockingFirst()
    }

    override suspend fun post(jsonRpcRequest: JsonRpcRequest): JsonRpcResult {
        return postRx(jsonRpcRequest).blockingFirst()
    }

    override suspend fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult> {
        return postRx(jsonRpcRequest).blockingFirst()
    }

    abstract fun receiptRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult>

    abstract fun blockRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcBlockResult>

    abstract fun transactionRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionResult>

    abstract fun postRx(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult>

    abstract fun postRx(jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>>
}

