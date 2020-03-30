package pm.gnosis.ethereum

import io.reactivex.Observable
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigInteger

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

interface CoEthereumRepository {

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

open class BulkRequest(val requests: List<EthRequest<*>>) {
    constructor(vararg requests: EthRequest<*>) : this(requests.toList())
}

open class MappingBulkRequest<out T>(
    private val mappedRequests: List<MappedRequest<*, T>>
) : BulkRequest(mappedRequests.map { it.request }) {

    constructor(vararg requests: MappedRequest<*, T>) : this(requests.toList())

    fun mapped(): List<T> = mappedRequests.map { it.mapped() }
}

class MappedRequest<I, out T>(
    val request: EthRequest<I>,
    private val mapper: (I?) -> T
) {
    fun mapped(): T {
        return mapper(request.result())
    }
}

sealed class EthRequest<T>(val id: Int) {
    var response: Response<T>? = null

    fun result(): T? = (response as? Response.Success)?.data

    @Throws(RequestFailedException::class, RequestNotExecutedException::class)
    fun checkedResult(errorMsg: String? = null): T =
        response.let {
            when (it) {
                is EthRequest.Response.Success ->
                    it.data
                is EthRequest.Response.Failure -> {
                    val msg = it.error + (errorMsg?.let { " ($it)" } ?: "")
                    throw RequestFailedException(msg)
                }
                null ->
                    throw RequestNotExecutedException(errorMsg)
            }
        }

    sealed class Response<T> {
        data class Success<T>(val data: T) : Response<T>()
        data class Failure<T>(val error: String) : Response<T>()
    }
}

class EthCall(
    val from: Solidity.Address? = null,
    val transaction: Transaction? = null,
    id: Int = 0,
    val block: Block = Block.PENDING
) : EthRequest<String>(id)

class EthBalance(val address: Solidity.Address, id: Int = 0, val block: Block = Block.PENDING) :
    EthRequest<Wei>(id)

class EthGasPrice(id: Int = 0) : EthRequest<BigInteger>(id)

class EthEstimateGas(
    val from: Solidity.Address? = null,
    val transaction: Transaction? = null,
    id: Int = 0
) : EthRequest<BigInteger>(id)

class EthGetTransactionCount(val from: Solidity.Address, id: Int = 0, val block: Block = Block.PENDING) :
    EthRequest<BigInteger>(id)

class EthGetStorageAt(val from: Solidity.Address, val location: BigInteger, id: Int = 0, val block: Block = Block.PENDING) :
    EthRequest<String>(id)

class EthSendRawTransaction(val signedData: String, id: Int = 0) : EthRequest<String>(id)

class TransactionReceiptNotFound : NoSuchElementException()

class TransactionNotFound : NoSuchElementException()

class BlockNotFound : NoSuchElementException()

class RequestFailedException(msg: String? = null) : RuntimeException(msg)

class RequestNotExecutedException(msg: String? = null) : RuntimeException(msg)

sealed class Block {
    companion object {
        val PENDING = BlockPending()
        val LATEST = BlockLatest()
        val EARLIEST = BlockEarliest()
    }
}

class BlockNumber(val number: BigInteger) : Block()

class BlockEarliest internal constructor() : Block()

class BlockLatest internal constructor() : Block()

class BlockPending internal constructor() : Block()
