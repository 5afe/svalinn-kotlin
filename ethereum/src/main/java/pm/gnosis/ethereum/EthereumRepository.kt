package pm.gnosis.ethereum

import io.reactivex.Observable
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigInteger

interface EthereumRepository {

    fun <R : BulkRequest> request(bulk: R): Observable<R>

    fun <R : EthRequest<*>> request(request: R): Observable<R>

    fun getBalance(address: BigInteger): Observable<Wei>

    fun sendRawTransaction(signedTransactionData: String): Observable<String>

    fun getTransactionReceipt(receiptHash: String): Observable<TransactionReceipt>

    fun getTransactionParameters(
        from: BigInteger,
        to: BigInteger,
        value: Wei? = null,
        data: String? = null
    ): Observable<TransactionParameters>
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
            when(it) {
                is EthRequest.Response.Success ->
                    it.data
                is EthRequest.Response.Failure -> {
                    val msg = it.error + (errorMsg?.let {" ($it)"} ?: "")
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
    val from: BigInteger? = null,
    val transaction: Transaction? = null,
    id: Int = 0
) : EthRequest<String>(id)

class EthBalance(val address: BigInteger, id: Int = 0) : EthRequest<Wei>(id)

class EthGasPrice(id: Int = 0) : EthRequest<BigInteger>(id)

class EthEstimateGas(
    val from: BigInteger? = null,
    val transaction: Transaction? = null,
    id: Int = 0
) : EthRequest<BigInteger>(id)

class EthGetTransactionCount(val from: BigInteger, id: Int = 0) : EthRequest<BigInteger>(id)

class EthSendRawTransaction(val signedData: String, id: Int = 0) : EthRequest<String>(id)

class TransactionReceiptNotFound : NoSuchElementException()

class RequestFailedException(msg: String? = null): RuntimeException(msg)

class RequestNotExecutedException(msg: String? = null): RuntimeException(msg)
