package pm.gnosis.ethereum.rpc.models

import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.rpc.EthereumRpcConnector
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_CALL
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_ESTIMATE_GAS
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GAS_PRICE
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GET_BALANCE
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GET_STORAGE_AT
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_GET_TRANSACTION_COUNT
import pm.gnosis.ethereum.rpc.EthereumRpcConnector.Companion.FUNCTION_SEND_RAW_TRANSACTION
import pm.gnosis.models.Transaction
import pm.gnosis.models.TransactionEip1559
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.hexAsBigIntegerOrNull
import pm.gnosis.utils.toHexString
import java.math.BigInteger

sealed class RpcRequest<out T : EthRequest<*>>(val raw: T) {
    abstract fun request(): JsonRpcRequest
    abstract fun parse(response: JsonRpcResult)
}

class RpcCallRequest(raw: EthCall) : RpcRequest<EthCall>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_CALL,
            params = listOf(
                raw.transaction.toCallParams(raw.from?.asEthereumAddressString()),
                raw.block.asString()
            ),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<String>(it.message) }
                ?: response.result?.let { EthRequest.Response.Success(response.result) }
                ?: EthRequest.Response.Failure("Missing result")
    }
}

class RpcCallEip1559Request(raw: EthCallEip1559) : RpcRequest<EthCallEip1559>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_CALL,
            params = listOf(
                raw.transaction.toCallParams(raw.from?.asEthereumAddressString()),
                raw.block.asString()
            ),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<String>(it.message) }
            ?: response.result?.let { EthRequest.Response.Success(response.result) }
                    ?: EthRequest.Response.Failure("Missing result")
    }
}

class RpcBalanceRequest(raw: EthBalance) : RpcRequest<EthBalance>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_GET_BALANCE,
            params = listOf(raw.address.asEthereumAddressString(), raw.block.asString()),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<Wei>(it.message) }
                ?:
                response.result?.hexAsBigIntegerOrNull()?.let { EthRequest.Response.Success(Wei(it)) }
                ?: EthRequest.Response.Failure("Invalid balance!")
    }
}

class RpcEstimateGasRequest(raw: EthEstimateGas) : RpcRequest<EthEstimateGas>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_ESTIMATE_GAS,
            params = listOf(
                raw.transaction.toCallParams(raw.from?.asEthereumAddressString())
            ),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) }
                ?: response.result?.hexAsBigIntegerOrNull()?.let { EthRequest.Response.Success(it) }
                ?: EthRequest.Response.Failure("Invalid estimate!")
    }
}

class RpcEstimateGasEip1559Request(raw: EthEstimateGasEip1559) : RpcRequest<EthEstimateGasEip1559>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_ESTIMATE_GAS,
            params = listOf(
                raw.transaction.toCallParams(raw.from?.asEthereumAddressString())
            ),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) }
            ?: response.result?.hexAsBigIntegerOrNull()?.let { EthRequest.Response.Success(it) }
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
        raw.response = response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) }
                ?: response.result?.hexAsBigIntegerOrNull()?.let { EthRequest.Response.Success(it) }
                ?: EthRequest.Response.Failure("Invalid gas price!")
    }
}

class RpcTransactionCountRequest(raw: EthGetTransactionCount) :
    RpcRequest<EthGetTransactionCount>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_GET_TRANSACTION_COUNT,
            params = arrayListOf(raw.from.asEthereumAddressString(), raw.block.asString()),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<BigInteger>(it.message) }
                ?: response.result?.hexAsBigIntegerOrNull()?.let { EthRequest.Response.Success(it) }
                ?: EthRequest.Response.Failure("Invalid transaction count!")
    }
}

class RpcGetStorageAt(raw: EthGetStorageAt) :
    RpcRequest<EthGetStorageAt>(raw) {
    override fun request() =
        JsonRpcRequest(
            method = FUNCTION_GET_STORAGE_AT,
            params = arrayListOf(raw.from.asEthereumAddressString(), raw.location.toHexString(), raw.block.asString()),
            id = raw.id
        )

    override fun parse(response: JsonRpcResult) {
        raw.response = response.error?.let { EthRequest.Response.Failure<String>(it.message) }
            ?: response.result?.let { EthRequest.Response.Success(response.result) }
                    ?: EthRequest.Response.Failure("Missing result")
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
                ?: response.result?.let { EthRequest.Response.Success(response.result) }
                ?: EthRequest.Response.Failure("Missing result")
    }
}

private fun Transaction?.toCallParams(from: String?) =
    TransactionCallParams(
        from = from,
        to = this?.address?.asEthereumAddressString(),
        value = this?.value?.value?.toHexString(),
        data = this?.data,
        nonce = this?.nonce?.toHexString(),
        gas = this?.gas?.toHexString(),
        gasPrice = this?.gasPrice?.toHexString()
    )

private fun TransactionEip1559?.toCallParams(from: String?) =
    TransactionCallParams(
        type = this?.type?.toHexString(),
        chainId = this?.chainId?.toHexString(),
        from = from,
        to = this?.to?.asEthereumAddressString(),
        value = this?.value?.value?.toHexString() ?: "0x0",
        data = this?.data,
        nonce = this?.nonce?.toHexString(),
        gas = this?.fee?.gas?.toHexString(),
        maxPriorityFeePerGas = this?.fee?.maxPriorityFee?.toHexString(),
        maxFeePerGas = this?.fee?.maxFeePerGas?.toHexString()
    )

private fun Block.asString() =
    when (this) {
        is BlockNumber -> number.toHexString()
        is BlockEarliest -> EthereumRpcConnector.BLOCK_EARLIEST
        is BlockLatest -> EthereumRpcConnector.BLOCK_LATEST
        is BlockPending -> EthereumRpcConnector.BLOCK_PENDING
    }
