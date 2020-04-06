package eth.catchall.ethereum_rpc_retrofit_co

import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*

class RetrofitEthereumRpcConnector(private val api: RetrofitEthereumRpcApi) : EthereumRpcConnector {

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
