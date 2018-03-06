package pm.gnosis.ethereum.rpc

import io.reactivex.Observable
import pm.gnosis.ethereum.rpc.models.JsonRpcRequest
import pm.gnosis.ethereum.rpc.models.JsonRpcResult
import pm.gnosis.ethereum.rpc.models.JsonRpcTransactionReceiptResult

interface EthereumRpcApi {

    companion object {
        const val BLOCK_EARLIEST = "earliest"
        const val BLOCK_LATEST = "latest"
        const val BLOCK_PENDING = "pending"

        const val FUNCTION_GET_BALANCE = "eth_getBalance"
        const val FUNCTION_CALL = "eth_call"
        const val FUNCTION_ESTIMATE_GAS = "eth_estimateGas"
        const val FUNCTION_GAS_PRICE = "eth_gasPrice"
        const val FUNCTION_GET_TRANSACTION_COUNT = "eth_getTransactionCount"
        const val FUNCTION_SEND_RAW_TRANSACTION = "eth_sendRawTransaction"
    }

    fun receipt(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult>

    fun post(jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult>

    fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>>
}
