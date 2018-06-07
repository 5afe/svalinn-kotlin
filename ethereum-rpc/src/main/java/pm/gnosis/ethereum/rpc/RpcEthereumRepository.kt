package pm.gnosis.ethereum.rpc

import io.reactivex.Observable
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.ethereum.rpc.models.*
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigDecimal

class RpcEthereumRepository(private val ethereumRpcApi: EthereumRpcConnector) : EthereumRepository {

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

    override fun getTransactionReceipt(transactionHash: String): Observable<TransactionReceipt> =
        ethereumRpcApi.receipt(
            JsonRpcRequest(
                method = "eth_getTransactionReceipt",
                params = listOf(transactionHash)
            )
        ).map {
            it.result?.let {
                TransactionReceipt(
                    it.status,
                    it.transactionHash,
                    it.transactionIndex,
                    it.blockHash,
                    it.blockNumber,
                    it.from,
                    it.to,
                    it.cumulativeGasUsed,
                    it.gasUsed,
                    it.contractAddress,
                    it.logs.map { TransactionReceipt.Event(it.logIndex, it.data, it.topics) }
                )
            } ?: throw TransactionReceiptNotFound()

        }

    override fun getBlockByHash(blockHash: String): Observable<EthereumBlock> =
        ethereumRpcApi.block(
            JsonRpcRequest(
                method = "eth_getBlockByHash",
                params = listOf(blockHash, false)
            )
        ).map {
            it.result?.let {
                EthereumBlock(
                    it.number,
                    it.hash,
                    it.parentHash,
                    it.nonce,
                    it.sha3Uncles,
                    it.logsBloom,
                    it.transactionsRoot,
                    it.stateRoot,
                    it.receiptsRoot,
                    it.miner,
                    it.difficulty,
                    it.totalDifficulty,
                    it.extraData,
                    it.size,
                    it.gasLimit,
                    it.gasUsed,
                    it.timestamp
                )
            } ?: throw BlockNotFound()

        }

    override fun getTransactionByHash(transactionHash: String): Observable<TransactionData> =
        ethereumRpcApi.transaction(
            JsonRpcRequest(
                method = "eth_getTransactionByHash",
                params = listOf(transactionHash)
            )
        ).map {
            it.result?.let {
                TransactionData(
                    hash = transactionHash,
                    from = it.from,
                    transaction = Transaction(
                        it.to,
                        value = Wei(it.value),
                        data = it.data,
                        gas = it.gas,
                        gasPrice = it.gasPrice,
                        nonce = it.nonce
                    ),
                    blockHash = it.blockHash,
                    blockNumber = it.blockNumber,
                    transactionIndex = it.transactionIndex
                )
            } ?: throw TransactionNotFound()

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
