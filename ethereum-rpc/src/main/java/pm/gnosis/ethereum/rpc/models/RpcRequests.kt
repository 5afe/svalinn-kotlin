package pm.gnosis.ethereum.rpc.models

import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.BLOCK_LATEST
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.BLOCK_PENDING
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_CALL
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_ESTIMATE_GAS
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GAS_PRICE
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GET_BALANCE
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GET_TRANSACTION_COUNT
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_SEND_RAW_TRANSACTION
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.asEthereumAddressStringOrNull
import pm.gnosis.utils.hexAsBigIntegerOrNull
import pm.gnosis.utils.toHexString
import java.math.BigInteger

sealed class RpcRequest<out T: EthRequest<*>>(val raw: T) {
    abstract fun request(): JsonRpcRequest
    abstract fun parse(response: JsonRpcResult)
}

class RpcCallRequest(raw: EthCall) : RpcRequest<EthCall>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_CALL,
            params = listOf(
                raw.transaction.toCallParams(raw.from?.asEthereumAddressStringOrNull()),
                BLOCK_LATEST
            ),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response =
                response.error?.let { EthRequest.Response.Failure<String>(it.message) }
                ?: EthRequest.Response.Success(response.result)
    }
}

class RpcBalanceRequest(raw: EthBalance) : RpcRequest<EthBalance>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_GET_BALANCE,
            params = listOf(raw.address.asEthereumAddressString(), BLOCK_LATEST),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response =
                response.error?.let { EthRequest.Response.Failure<Wei>(it.message) } ?:
                response.result.hexAsBigIntegerOrNull()
                    ?.let { EthRequest.Response.Success(Wei(it)) }
                ?: EthRequest.Response.Failure("Invalid balance!")
    }
}

class RpcEstimateGasRequest(raw: EthEstimateGas) : RpcRequest<EthEstimateGas>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_ESTIMATE_GAS,
            params = listOf(
                raw.transaction.toCallParams(raw.from?.asEthereumAddressStringOrNull()),
                BLOCK_LATEST
            ),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response =
                response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) } ?:
                response.result.hexAsBigIntegerOrNull()
                    ?.let { EthRequest.Response.Success(it) }
                ?: EthRequest.Response.Failure("Invalid estimate!")
    }
}

class RpcGasPriceRequest(raw: EthGasPrice) : RpcRequest<EthGasPrice>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_GAS_PRICE,
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response =
                response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) } ?:
                response.result.hexAsBigIntegerOrNull()
                    ?.let { EthRequest.Response.Success(it) }
                ?: EthRequest.Response.Failure("Invalid gas price!")
    }
}

class RpcTransactionCountRequest(raw: EthGetTransactionCount) :
    RpcRequest<EthGetTransactionCount>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_GET_TRANSACTION_COUNT,
            params = arrayListOf(raw.from.asEthereumAddressString(), BLOCK_PENDING),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response =
                response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) } ?:
                response.result.hexAsBigIntegerOrNull()
                    ?.let { EthRequest.Response.Success(it) }
                ?: EthRequest.Response.Failure("Invalid transaction count!")
    }
}

class RpcSendRawTransaction(raw: EthSendRawTransaction) : RpcRequest<EthSendRawTransaction>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_SEND_RAW_TRANSACTION,
            params = listOf(raw.signedData),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response =
                response.error?.let { EthRequest.Response.Failure<String>(it.message) }
                ?: EthRequest.Response.Success(response.result)
    }
}

private fun Transaction?.toCallParams(from: String?) =
    TransactionCallParams(
        from = from,
        to = this?.address?.asEthereumAddressStringOrNull(),
        value = this?.value?.value?.toHexString(),
        data = this?.data,
        nonce = this?.nonce?.toHexString(),
        gas = this?.gas?.toHexString(),
        gasPrice = this?.gasPrice?.toHexString()
    )
