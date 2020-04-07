package pm.gnosis.ethereum.rpc.rx

import io.reactivex.Observable
import pm.gnosis.ethereum.BulkRequest
import pm.gnosis.ethereum.EthRequest
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei

interface EthereumRepository {

    fun <R : BulkRequest> request(bulk: R): Observable<R>

    fun <R : EthRequest<*>> request(request: R): Observable<R>

    fun getBalance(address: Solidity.Address): Observable<Wei>

    fun sendRawTransaction(signedTransactionData: String): Observable<String>

    fun getTransactionReceipt(transactionHash: String): Observable<TransactionReceipt>

    fun getTransactionByHash(transactionHash: String): Observable<TransactionData>

    fun getBlockByHash(blockHash: String): Observable<EthereumBlock>

    fun getTransactionParameters(
        from: Solidity.Address,
        to: Solidity.Address,
        value: Wei? = null,
        data: String? = null
    ): Observable<TransactionParameters>
}
