package pm.gnosis.ethereum.rpc

import pm.gnosis.ethereum.BulkRequest
import pm.gnosis.ethereum.EthRequest
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei

interface EthereumRepository {

    fun <R : BulkRequest> request(bulk: R): R

    fun <R : EthRequest<*>> request(request: R): R

    fun getBalance(address: Solidity.Address): Wei

    fun sendRawTransaction(signedTransactionData: String): String

    fun getTransactionReceipt(transactionHash: String): TransactionReceipt

    fun getTransactionByHash(transactionHash: String): TransactionData

    fun getBlockByHash(blockHash: String): EthereumBlock

    fun getTransactionParameters(
        from: Solidity.Address,
        to: Solidity.Address,
        value: Wei? = null,
        data: String? = null
    ): TransactionParameters
}
