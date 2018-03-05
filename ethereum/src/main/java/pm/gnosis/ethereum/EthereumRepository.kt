package pm.gnosis.ethereum

import io.reactivex.Observable
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigInteger

interface EthereumRepository {

    fun <R : EthRequest<*>> bulk(requests: List<R>): Observable<List<R>>

    fun <R : EthRequest<*>> request(request: R): Observable<R>

    fun getBalance(address: BigInteger): Observable<Wei>

    fun sendRawTransaction(signedTransactionData: String): Observable<String>

    fun getTransactionReceipt(receiptHash: String): Observable<TransactionReceipt>

    fun getTransactionParameters(from: BigInteger, to: BigInteger, value: Wei? = null, data: String? = null): Observable<TransactionParameters>
}

abstract class EthRequest<T>(val id: Int) {
    var response: Response<T>? = null

    fun result(): T? = (response as? Response.Success)?.data

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
