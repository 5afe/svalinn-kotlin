package pm.gnosis.ethereum.rpc.co

import pm.gnosis.ethereum.BulkRequest
import pm.gnosis.ethereum.EthRequest
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei

interface EthereumRepository {

    suspend fun <R : BulkRequest> request(bulk: R): R

    suspend fun <R : EthRequest<*>> request(request: R): R

    suspend fun getBalance(address: Solidity.Address): Wei

    suspend fun sendRawTransaction(signedTransactionData: String): String

    suspend fun getTransactionReceipt(transactionHash: String): TransactionReceipt

    suspend fun getTransactionByHash(transactionHash: String): TransactionData

    suspend fun getBlockByHash(blockHash: String): EthereumBlock

    suspend fun getTransactionParameters(
        from: Solidity.Address,
        to: Solidity.Address,
        value: Wei? = null,
        data: String? = null
    ): TransactionParameters
}
