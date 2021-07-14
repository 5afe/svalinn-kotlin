package pm.gnosis.ethereum.rpc

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

class RpcEthereumRepository(
    private val ethereumRpcApi: EthereumRpcConnector,
    rpcUrl: String
) : EthereumRepository {

    override var rpcUrl: String = rpcUrl
        set(value) {
            ethereumRpcApi.rpcUrl = value
            field = value
        }

    override suspend fun <R : BulkRequest> request(bulk: R): R =
        bulk.requests.associate { it.id to it.toRpcRequest() }
            .let { rpcRequests ->
                ethereumRpcApi.post(rpcRequests.values.map { it.request() })
                    .map { rpcRequests[it.id]?.parse(it) }
                    .let { bulk }
            }

    override suspend fun <R : EthRequest<*>> request(request: R): R =
        request.toRpcRequest().let { rpcRequest ->
            ethereumRpcApi.post(rpcRequest.request())
                .let {
                    rpcRequest.parse(it)
                    request
                }
        }

    override suspend fun getBalance(address: Solidity.Address): Wei =
        request(EthBalance(address)).checkedResult()

    override suspend fun sendRawTransaction(signedTransactionData: String): String =
        request(EthSendRawTransaction(signedTransactionData))
            .checkedResult("Could not send raw transaction")

    override suspend fun getTransactionReceipt(transactionHash: String): TransactionReceipt =
        ethereumRpcApi.receipt(
            JsonRpcRequest(
                method = "eth_getTransactionReceipt",
                params = listOf(transactionHash)
            )
        ).result?.let {
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


    override suspend fun getTransactionByHash(transactionHash: String): TransactionData =
        ethereumRpcApi.transaction(
            JsonRpcRequest(
                method = "eth_getTransactionByHash",
                params = listOf(transactionHash)
            )
        ).result?.let { result ->
            TransactionData(
                hash = transactionHash,
                from = result.from,
                transaction = Transaction(
                    result.to,
                    value = Wei(result.value),
                    data = result.data,
                    gas = result.gas,
                    gasPrice = result.gasPrice,
                    nonce = result.nonce
                ),
                blockHash = result.blockHash,
                blockNumber = result.blockNumber,
                transactionIndex = result.transactionIndex
            )
        } ?: throw TransactionNotFound()

    override suspend fun getBlockByHash(blockHash: String): EthereumBlock =
        ethereumRpcApi.block(
            JsonRpcRequest(
                method = "eth_getBlockByHash",
                params = listOf(blockHash, false)
            )
        ).result?.let {
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

    override suspend fun getTransactionParameters(from: Solidity.Address, to: Solidity.Address, value: Wei?, data: String?): TransactionParameters {
        val tx = Transaction(address = to, value = value, data = data)
        val estimateRequest = EthEstimateGas(from, tx, 0)
        val gasPriceRequest = EthGasPrice(1)
        val nonceRequest = EthGetTransactionCount(from, id = 2)
        return request(BulkRequest(estimateRequest, gasPriceRequest, nonceRequest)).let {
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
