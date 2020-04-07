package pm.gnosis.ethereum.rpc.retrofit

import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.models.*
import retrofit2.HttpException
import retrofit2.Response

class RetrofitEthereumRpcConnector(private val api: RetrofitEthereumRpcApi) : EthereumRpcConnector {

    override fun receipt(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult {
        return api.receipt(jsonRpcRequest).getOrThrow()
    }

    override fun block(jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult {
        return api.block(jsonRpcRequest).getOrThrow()
    }

    override fun transaction(jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult {
        return api.transaction(jsonRpcRequest).getOrThrow()
    }

    override fun post(jsonRpcRequest: JsonRpcRequest): JsonRpcResult {
        return api.post(jsonRpcRequest).getOrThrow()
    }

    override fun post(jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult> {
        return api.post(jsonRpcRequest).getOrThrow()
    }

}


fun <T> Response<T>.getOrThrow() = this.body() ?: throw HttpException(this)
