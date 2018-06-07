package pm.gnosis.ethereum.rpc

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.ethereum.rpc.models.*
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.tests.utils.ImmediateSchedulersRule
import pm.gnosis.tests.utils.MockUtils
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.toHexString
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class RpcEthereumRepositoryTest {

    @JvmField
    @Rule
    val rule = ImmediateSchedulersRule()

    @Mock
    private lateinit var apiMock: EthereumRpcConnector

    private lateinit var repository: RpcEthereumRepository

    @Before
    fun setUp() {
        repository = RpcEthereumRepository(apiMock)
    }

    @Test
    fun bulk() {
        given(apiMock.post(MockUtils.any<Collection<JsonRpcRequest>>()))
            .willReturn(
                Observable.just(
                    listOf(
                        rpcResult("0x", 0),
                        rpcResult(Wei.ether("1").value.toHexString(), 1),
                        rpcResult(Wei.ether("0.000000001").value.toHexString(), 2),
                        rpcResult("0x0a", 3),
                        rpcResult(
                            "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1",
                            4
                        )
                    )
                )
            )

        val tx = Transaction(Solidity.Address(BigInteger.TEN), value = Wei.ether("0.001"))
        val bulk = BulkRequest(
            EthCall(Solidity.Address(BigInteger.ONE), tx, 0),
            EthBalance(Solidity.Address(BigInteger.ONE), 1),
            EthGasPrice(2),
            EthGetTransactionCount(Solidity.Address(BigInteger.ONE), 3),
            EthSendRawTransaction("some_signed_data", 4)
        )

        val testObserver = TestObserver<BulkRequest>()
        repository.request(bulk).subscribe(testObserver)

        testObserver.assertResult(bulk)

        assertEquals(EthRequest.Response.Success("0x"), bulk.requests[0].response)
        assertEquals(EthRequest.Response.Success(Wei.ether("1")), bulk.requests[1].response)
        assertEquals(
            EthRequest.Response.Success(Wei.ether("0.000000001").value),
            bulk.requests[2].response
        )
        assertEquals(
            EthRequest.Response.Success("0x0a".hexAsBigInteger()),
            bulk.requests[3].response
        )
        assertEquals(
            EthRequest.Response.Success("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"),
            bulk.requests[4].response
        )

        then(apiMock).should().post(bulk.requests.map { it.toRpcRequest().request() })
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun bulkSameId() {
        given(apiMock.post(MockUtils.any<Collection<JsonRpcRequest>>()))
            .willReturn(
                Observable.just(
                    listOf(
                        rpcResult(
                            "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1",
                            0
                        )
                    )
                )
            )

        val tx = Transaction(Solidity.Address(BigInteger.TEN), value = Wei.ether("0.001"))
        val bulk = BulkRequest(
            EthCall(Solidity.Address(BigInteger.ONE), tx, 0),
            EthSendRawTransaction("some_signed_data", 0)
        )

        val testObserver = TestObserver<BulkRequest>()
        repository.request(bulk).subscribe(testObserver)

        testObserver.assertResult(bulk)

        val requests = bulk.requests
        assertNull("First request should be overwritten by second request", requests[0].response)
        assertEquals(
            EthRequest.Response.Success("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"),
            requests[1].response
        )

        then(apiMock).should().post(listOf(requests[1].toRpcRequest().request()))
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun bulkWithFailure() {
        given(apiMock.post(MockUtils.any<Collection<JsonRpcRequest>>()))
            .willReturn(
                Observable.just(
                    listOf(
                        rpcResult("0x", 0),
                        rpcResult(Wei.ether("1").value.toHexString(), 1),
                        rpcResult("0x", 2, error = "revert; But I won't tell you why")
                    )
                )
            )

        val tx = Transaction(Solidity.Address(BigInteger.TEN), value = Wei.ether("0.001"))
        val bulk = BulkRequest(
            EthCall(Solidity.Address(BigInteger.ONE), tx, 0),
            EthBalance(Solidity.Address(BigInteger.ONE), 1),
            EthSendRawTransaction("some_signed_data", 2)
        )

        val testObserver = TestObserver<BulkRequest>()
        repository.request(bulk).subscribe(testObserver)

        testObserver.assertResult(bulk)

        val requests = bulk.requests
        assertEquals(EthRequest.Response.Success("0x"), requests[0].response)
        assertEquals(EthRequest.Response.Success(Wei.ether("1")), requests[1].response)
        assertEquals(
            EthRequest.Response.Failure<String>("revert; But I won't tell you why"),
            requests[2].response
        )

        then(apiMock).should().post(requests.map { it.toRpcRequest().request() })
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun request() {
        given(apiMock.post(MockUtils.any<JsonRpcRequest>()))
            .willReturn(
                Observable.just(
                    rpcResult(Wei.ether("1").value.toHexString(), 1)
                )
            )

        val request = EthBalance(Solidity.Address(BigInteger.ONE), 1)

        val testObserver = TestObserver<EthRequest<*>>()
        repository.request(request).subscribe(testObserver)

        testObserver.assertResult(request)

        assertEquals(
            EthRequest.Response.Success(Wei.ether("1")),
            request.response
        )

        then(apiMock).should().post(request.toRpcRequest().request())
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun requestFailure() {
        given(apiMock.post(MockUtils.any<JsonRpcRequest>()))
            .willReturn(
                Observable.just(
                    rpcResult("0x", 1, "eth_getBalance should never error")
                )
            )

        val request = EthBalance(Solidity.Address(BigInteger.ONE), 1)

        val testObserver = TestObserver<EthRequest<*>>()
        repository.request(request).subscribe(testObserver)

        testObserver.assertResult(request)

        assertEquals(
            EthRequest.Response.Failure<Wei>("eth_getBalance should never error"),
            request.response
        )

        then(apiMock).should().post(request.toRpcRequest().request())
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getBalance() {
        given(apiMock.post(MockUtils.any<JsonRpcRequest>()))
            .willReturn(
                Observable.just(
                    rpcResult(Wei.ether("1").value.toHexString())
                )
            )

        val testObserver = TestObserver<Wei>()
        repository.getBalance(Solidity.Address(BigInteger.ONE)).subscribe(testObserver)

        testObserver.assertResult(Wei.ether("1"))

        then(apiMock).should().post(EthBalance(Solidity.Address(BigInteger.ONE)).toRpcRequest().request())
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getBalanceFailure() {
        given(apiMock.post(MockUtils.any<JsonRpcRequest>()))
            .willReturn(
                Observable.just(
                    rpcResult("0x", 0, "eth_getBalance should never error")
                )
            )

        val testObserver = TestObserver<Wei>()
        repository.getBalance(Solidity.Address(BigInteger.ONE)).subscribe(testObserver)

        testObserver.assertError { it is RequestFailedException && it.message == "eth_getBalance should never error" }

        then(apiMock).should().post(EthBalance(Solidity.Address(BigInteger.ONE)).toRpcRequest().request())
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun sendRawTransaction() {
        given(apiMock.post(MockUtils.any<JsonRpcRequest>()))
            .willReturn(
                Observable.just(
                    rpcResult("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1")
                )
            )

        val testObserver = TestObserver<String>()
        repository.sendRawTransaction("0xSomeSignedManager").subscribe(testObserver)

        testObserver.assertResult("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1")

        then(apiMock).should()
            .post(EthSendRawTransaction("0xSomeSignedManager").toRpcRequest().request())
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun sendRawTransactionFailure() {
        given(apiMock.post(MockUtils.any<JsonRpcRequest>()))
            .willReturn(
                Observable.just(
                    rpcResult("0x", 0, "revert; But I won't tell you why")
                )
            )

        val testObserver = TestObserver<String>()
        repository.sendRawTransaction("0xSomeSignedManager").subscribe(testObserver)

        testObserver.assertError {
            it is RequestFailedException &&
                    it.message == "revert; But I won't tell you why (Could not send raw transaction)"
        }

        then(apiMock).should()
            .post(EthSendRawTransaction("0xSomeSignedManager").toRpcRequest().request())
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getTransactionReceipt() {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        val data = "0x0000000000000000000000009205b8f1a21a3cee0f6a629fd83cee0f6a629fd8"
        val topic0 = "0x4db17dd5e4732fb6da34a148104a592783ca119a1e7bb8829eba6cbadef0b511"
        given(apiMock.receipt(MockUtils.any()))
            .willReturn(
                Observable.just(
                    JsonRpcTransactionReceiptResult(
                        1, "2.0",
                        result = JsonRpcTransactionReceiptResult.TransactionReceipt(
                            BigInteger.ONE,
                            transactionHash,
                            BigInteger.valueOf(23),
                            "block-hash",
                            BigInteger.valueOf(31),
                            "0x32".asEthereumAddress()!!,
                            "0x55".asEthereumAddress()!!,
                            BigInteger.valueOf(115),
                            BigInteger.valueOf(11),
                            "0x31415925".asEthereumAddress(),
                            listOf(
                                JsonRpcTransactionReceiptResult.TransactionReceipt.Event(
                                    BigInteger.ZERO, data, listOf(topic0)
                                )
                            )
                        )
                    )
                )
            )

        val testObserver = TestObserver<TransactionReceipt>()
        repository.getTransactionReceipt(transactionHash).subscribe(testObserver)

        testObserver.assertResult(
            TransactionReceipt(
                BigInteger.ONE,
                transactionHash,
                BigInteger.valueOf(23),
                "block-hash",
                BigInteger.valueOf(31),
                "0x32".asEthereumAddress()!!,
                "0x55".asEthereumAddress()!!,
                BigInteger.valueOf(115),
                BigInteger.valueOf(11),
                "0x31415925".asEthereumAddress(),
                listOf(
                    TransactionReceipt.Event(
                        BigInteger.ZERO, data, listOf(topic0)
                    )
                )
            )
        )

        then(apiMock).should()
            .receipt(
                JsonRpcRequest(
                    method = "eth_getTransactionReceipt",
                    params = listOf(transactionHash)
                )
            )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getTransactionReceiptFailure() {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        given(apiMock.receipt(MockUtils.any()))
            .willReturn(
                Observable.just(
                    JsonRpcTransactionReceiptResult(
                        1, "2.0",
                        result = null
                    )
                )
            )

        val testObserver = TestObserver<TransactionReceipt>()
        repository.getTransactionReceipt(transactionHash).subscribe(testObserver)

        testObserver.assertError { it is TransactionReceiptNotFound }

        then(apiMock).should()
            .receipt(
                JsonRpcRequest(
                    method = "eth_getTransactionReceipt",
                    params = listOf(transactionHash)
                )
            )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getTransactionByHash() {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        val data = "0x0000000000000000000000009205b8f1a21a3cee0f6a629fd83cee0f6a629fd8"
        given(apiMock.transaction(MockUtils.any()))
            .willReturn(
                Observable.just(
                    JsonRpcTransactionResult(
                        1, "2.0",
                        result = JsonRpcTransactionResult.JsonTransaction(
                            transactionHash,
                            BigInteger.valueOf(23),
                            "block-hash",
                            BigInteger.valueOf(31),
                            BigInteger.valueOf(32),
                            "0x32".asEthereumAddress()!!,
                            "0x55".asEthereumAddress()!!,
                            BigInteger.ONE,
                            BigInteger.valueOf(11),
                            BigInteger.valueOf(115),
                            data
                        )
                    )
                )
            )

        val testObserver = TestObserver<TransactionData>()
        repository.getTransactionByHash(transactionHash).subscribe(testObserver)

        testObserver.assertResult(
            TransactionData(
                transactionHash,
                "0x32".asEthereumAddress()!!,
                Transaction(
                    "0x55".asEthereumAddress()!!,
                    Wei(BigInteger.ONE),
                    BigInteger.valueOf(115),
                    BigInteger.valueOf(11),
                    data,
                    BigInteger.valueOf(23)
                ),
                BigInteger.valueOf(32),
                "block-hash",
                BigInteger.valueOf(31)
            )
        )

        then(apiMock).should()
            .transaction(
                JsonRpcRequest(
                    method = "eth_getTransactionByHash",
                    params = listOf(transactionHash)
                )
            )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getTransactionByHashFailure() {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        given(apiMock.transaction(MockUtils.any()))
            .willReturn(
                Observable.just(
                    JsonRpcTransactionResult(
                        1, "2.0",
                        result = null
                    )
                )
            )

        val testObserver = TestObserver<TransactionData>()
        repository.getTransactionByHash(transactionHash).subscribe(testObserver)

        testObserver.assertError { it is TransactionNotFound }

        then(apiMock).should()
            .transaction(
                JsonRpcRequest(
                    method = "eth_getTransactionByHash",
                    params = listOf(transactionHash)
                )
            )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getBlockByHash() {
        val blockHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        given(apiMock.block(MockUtils.any()))
            .willReturn(
                Observable.just(
                    JsonRpcBlockResult(
                        1, "2.0",
                        result = JsonRpcBlockResult.JsonBlock(
                            BigInteger.ONE,
                            blockHash,
                            "parent-hash",
                            "weird-nonce",
                            "uncles-hash",
                            "logsBloom",
                            "transactionsRoot",
                            "stateRoot",
                            "receiptRoot",
                            "0x1234".asEthereumAddress()!!,
                            BigInteger.valueOf(31),
                            BigInteger.valueOf(1331),
                            "extra-data",
                            BigInteger.valueOf(1989),
                            BigInteger.valueOf(115),
                            BigInteger.valueOf(11),
                            BigInteger.valueOf(987654321)
                        )
                    )
                )
            )

        val testObserver = TestObserver<EthereumBlock>()
        repository.getBlockByHash(blockHash).subscribe(testObserver)

        testObserver.assertResult(
            EthereumBlock(
                BigInteger.ONE,
                blockHash,
                "parent-hash",
                "weird-nonce",
                "uncles-hash",
                "logsBloom",
                "transactionsRoot",
                "stateRoot",
                "receiptRoot",
                "0x1234".asEthereumAddress()!!,
                BigInteger.valueOf(31),
                BigInteger.valueOf(1331),
                "extra-data",
                BigInteger.valueOf(1989),
                BigInteger.valueOf(115),
                BigInteger.valueOf(11),
                BigInteger.valueOf(987654321)
            )
        )

        then(apiMock).should()
            .block(
                JsonRpcRequest(
                    method = "eth_getBlockByHash",
                    params = listOf(blockHash, false)
                )
            )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getBlockByHashFailure() {
        val blockHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        given(apiMock.block(MockUtils.any()))
            .willReturn(
                Observable.just(
                    JsonRpcBlockResult(
                        1, "2.0",
                        result = null
                    )
                )
            )

        val testObserver = TestObserver<EthereumBlock>()
        repository.getBlockByHash(blockHash).subscribe(testObserver)

        testObserver.assertError { it is BlockNotFound }

        then(apiMock).should()
            .block(
                JsonRpcRequest(
                    method = "eth_getBlockByHash",
                    params = listOf(blockHash, false)
                )
            )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getTransactionParameters() {
        given(apiMock.post(MockUtils.any<Collection<JsonRpcRequest>>()))
            .willReturn(
                Observable.just(
                    listOf(
                        rpcResult(BigInteger.valueOf(55000).toHexString(), 0),
                        rpcResult(Wei.ether("0.000000001").value.toHexString(), 1),
                        rpcResult("0x0a", 2)
                    )
                )
            )

        val transaction = Transaction(Solidity.Address(BigInteger.ONE), value = Wei.ether("1"), data = "0x42cde4e8")
        val testObserver = TestObserver<TransactionParameters>()
        repository.getTransactionParameters(
            Solidity.Address(BigInteger.TEN),
            transaction.address,
            transaction.value,
            transaction.data
        ).subscribe(testObserver)

        testObserver.assertResult(
            TransactionParameters(
                BigInteger.valueOf(77000),
                Wei.ether("0.000000001").value,
                BigInteger.valueOf(10)
            )
        )

        then(apiMock).should().post(
            listOf(
                EthEstimateGas(Solidity.Address(BigInteger.TEN), transaction, 0).toRpcRequest().request(),
                EthGasPrice(1).toRpcRequest().request(),
                EthGetTransactionCount(Solidity.Address(BigInteger.TEN), 2).toRpcRequest().request()
            )
        )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    private fun testTransactionParametersFailure(rpcResults: List<JsonRpcResult>) {
        given(apiMock.post(MockUtils.any<Collection<JsonRpcRequest>>()))
            .willReturn(Observable.just(rpcResults))

        val transaction = Transaction(Solidity.Address(BigInteger.ONE), value = Wei.ether("1"), data = "0x42cde4e8")
        val testObserver = TestObserver<TransactionParameters>()
        repository.getTransactionParameters(
            Solidity.Address(BigInteger.TEN),
            transaction.address,
            transaction.value,
            transaction.data
        ).subscribe(testObserver)

        testObserver.assertError { it is RequestFailedException }

        then(apiMock).should().post(
            listOf(
                EthEstimateGas(Solidity.Address(BigInteger.TEN), transaction, 0).toRpcRequest().request(),
                EthGasPrice(1).toRpcRequest().request(),
                EthGetTransactionCount(Solidity.Address(BigInteger.TEN), 2).toRpcRequest().request()
            )
        )
        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getTransactionParametersEstimateFailure() {
        testTransactionParametersFailure(
            listOf(
                rpcResult(error = "Something went wrong", id = 0),
                rpcResult(Wei.ether("0.000000001").value.toHexString(), 1),
                rpcResult("0x0a", 2)
            )
        )
    }

    @Test
    fun getTransactionParametersGasPriceFailure() {
        testTransactionParametersFailure(
            listOf(
                rpcResult(BigInteger.valueOf(55000).toHexString(), 0),
                rpcResult(error = "Something went wrong", id = 1),
                rpcResult("0x0a", 2)
            )
        )
    }

    @Test
    fun getTransactionParametersNonceFailure() {
        testTransactionParametersFailure(
            listOf(
                rpcResult(BigInteger.valueOf(55000).toHexString(), 0),
                rpcResult(Wei.ether("0.000000001").value.toHexString(), 1),
                rpcResult(error = "Something went wrong", id = 2)
            )
        )
    }

    private fun rpcResult(result: String = "0x", id: Int = 0, error: String? = null) =
        JsonRpcResult(id, "2.0", error?.let { JsonRpcError(23, it) }, result)

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
}
