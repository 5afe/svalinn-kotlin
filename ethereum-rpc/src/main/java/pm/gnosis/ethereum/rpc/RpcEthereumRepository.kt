package pm.gnosis.ethereum.rpc

import io.reactivex.Observable
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.ethereum.rpc.models.*
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigDecimal

class RpcEthereumRepository(
    private val ethereumRpcApi: EthereumRpcConnector
) : EthereumRepository {

    override fun <R : BulkRequest> request(bulk: R): Observable<R> =
        Observable.fromCallable { bulk.requests.associate { it.id to it.toRpcRequest() } }
            .flatMap { rpcRequests ->
                ethereumRpcApi.post(rpcRequests.values.map { it.request() })
                    .map { it.forEach { rpcRequests[it.id]?.parse(it) } }
                    .map { bulk }
            }

    override fun <R : EthRequest<*>> request(request: R): Observable<R> =
        Observable.fromCallable { request.toRpcRequest() }
            .flatMap { rpcRequest ->
                ethereumRpcApi.post(rpcRequest.request())
                    .map { rpcRequest.parse(it) }
                    .map { request }
            }

    override fun getBalance(address: Solidity.Address): Observable<Wei> =
        request(EthBalance(address))
            .map { it.checkedResult() }

    override fun sendRawTransaction(signedTransactionData: String): Observable<String> =
        request(EthSendRawTransaction(signedTransactionData))
            .map {
                it.checkedResult("Could not send raw transaction")
            }

    override fun getTransactionReceipt(receiptHash: String): Observable<TransactionReceipt> =
        ethereumRpcApi.receipt(
            JsonRpcRequest(
                method = "eth_getTransactionReceipt",
                params = listOf(receiptHash)
            )
        ).map {
            it.result?.let {
                TransactionReceipt(
                    it.status,
                    it.transactionHash,
                    it.contractAddress,
                    it.logs.map { TransactionReceipt.Event(it.logIndex, it.data, it.topics) }
                )
            } ?: throw TransactionReceiptNotFound()

        }

    override fun getTransactionParameters(
        from: Solidity.Address,
        to: Solidity.Address,
        value: Wei?,
        data: String?
    ): Observable<TransactionParameters> {
        val tx = Transaction(address = to, value = value, data = data)
        val estimateRequest = EthEstimateGas(from, tx, 0)
        val gasPriceRequest = EthGasPrice(1)
        val nonceRequest = EthGetTransactionCount(from, 2)
        return request(BulkRequest(estimateRequest, gasPriceRequest, nonceRequest)).map {
            val estimate = estimateRequest.checkedResult("Could not retrieve estimate")
            val price = gasPriceRequest.checkedResult("Could not retrieve gas price")
            val nonce = nonceRequest.checkedResult("Could not retrieve nonce")
            val adjustedGas = BigDecimal.valueOf(1.4)
                .multiply(BigDecimal(estimate)).setScale(0, BigDecimal.ROUND_UP)
                .unscaledValue()
            TransactionParameters(adjustedGas, price, nonce)
        }
    }
}

private fun <T> EthRequest<T>.toRpcRequest() =
    when (this) {
        is EthCall -> RpcCallRequest(this)
        is EthBalance -> RpcBalanceRequest(this)
        is EthEstimateGas -> RpcEstimateGasRequest(this)
        is EthGasPrice -> RpcGasPriceRequest(this)
        is EthGetTransactionCount -> RpcTransactionCountRequest(this)
        is EthSendRawTransaction -> RpcSendRawTransaction(this)
        else -> throw IllegalArgumentException()
    }
