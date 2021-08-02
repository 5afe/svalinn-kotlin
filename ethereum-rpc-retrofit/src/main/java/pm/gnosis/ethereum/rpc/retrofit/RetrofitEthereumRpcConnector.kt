package pm.gnosis.ethereum.rpc.retrofit

import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*

class RetrofitEthereumRpcConnector(private val api: RetrofitEthereumRpcApi, override var rpcUrl: String) : EthereumRpcConnector {

    override suspend fun receipt(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult {
        return api.receipt(rpcUrl,jsonRpcRequest)
    }

    override suspend fun block(jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult {
        return api.block(rpcUrl, jsonRpcRequest)
    }

    override suspend fun transaction(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult {
        return api.transaction(rpcUrl, jsonRpcRequest)
    }

    override suspend fun post(jsonRpcRequest: JsonRpcRequest): JsonRpcResult {
        return api.post(rpcUrl, jsonRpcRequest)
    }

    override suspend fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult> {
        return api.post(rpcUrl, jsonRpcRequest)
    }

}
