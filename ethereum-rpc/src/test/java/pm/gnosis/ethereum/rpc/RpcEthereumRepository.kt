package pm.gnosis.ethereum.rpc

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.models.EthereumBlock
import pm.gnosis.ethereum.models.TransactionData
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.ethereum.rpc.models.*
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.toHexString
import java.math.BigInteger

class RpcEthereumRepositoryTest {

    private val apiMock = mockk<EthereumRpcConnector>()

    private lateinit var repository: RpcEthereumRepository

    @Before
    fun setup() {
        repository = RpcEthereumRepository(apiMock, "")
    }

    @Test
    fun bulk() = runBlocking {
        coEvery { apiMock.post(any<Collection<JsonRpcRequest>>()) } returns
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


        val tx = Transaction(Solidity.Address(BigInteger.TEN), value = Wei.ether("0.001"))
        val bulk = BulkRequest(
            EthCall(Solidity.Address(BigInteger.ONE), tx, id = 0),
            EthBalance(Solidity.Address(BigInteger.ONE), id = 1),
            EthGasPrice(2),
            EthGetTransactionCount(Solidity.Address(BigInteger.ONE), id = 3),
            EthSendRawTransaction("some_signed_data", 4)
        )

        val expected = repository.request(bulk)

        assert(expected == bulk)

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

        coVerify(exactly = 1) { apiMock.post(bulk.requests.map { it.toRpcRequest().request() }) }
//        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun bulkSameId() = runBlocking {
        coEvery { apiMock.post(any<Collection<JsonRpcRequest>>()) } returns
                listOf(
                    rpcResult(
                        "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1",
                        0
                    )
                )

        val tx = Transaction(Solidity.Address(BigInteger.TEN), value = Wei.ether("0.001"))
        val bulk = BulkRequest(
            EthCall(Solidity.Address(BigInteger.ONE), tx, id = 0),
            EthSendRawTransaction("some_signed_data", 0)
        )

        val actual = repository.request(bulk)

        assert(actual == bulk)

        val requests = bulk.requests
        assertNull("First request should be overwritten by second request", requests[0].response)
        assertEquals(
            EthRequest.Response.Success("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"),
            requests[1].response
        )

        coVerify(exactly = 1) { apiMock.post(listOf(requests[1].toRpcRequest().request())) }
//        then(apiMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun bulkWithFailure() = runBlocking {
        coEvery { apiMock.post(any<Collection<JsonRpcRequest>>()) } returns
                listOf(
                    rpcResult("0x", 0),
                    rpcResult(Wei.ether("1").value.toHexString(), 1),
                    rpcResult("0x", 2, error = "revert; But I won't tell you why")
                )

        val tx = Transaction(Solidity.Address(BigInteger.TEN), value = Wei.ether("0.001"))
        val bulk = BulkRequest(
            EthCall(Solidity.Address(BigInteger.ONE), tx, id = 0),
            EthBalance(Solidity.Address(BigInteger.ONE), id = 1),
            EthSendRawTransaction("some_signed_data", 2)
        )

        val actual = repository.request(bulk)
        assertEquals(actual, bulk)

        val requests = bulk.requests
        assertEquals(EthRequest.Response.Success("0x"), requests[0].response)
        assertEquals(EthRequest.Response.Success(Wei.ether("1")), requests[1].response)
        assertEquals(
            EthRequest.Response.Failure<String>("revert; But I won't tell you why"),
            requests[2].response
        )

        coVerify(exactly = 1) { apiMock.post(requests.map { it.toRpcRequest().request() }) }
    }

    @Test
    fun request() = runBlocking {
        coEvery { apiMock.post(any<JsonRpcRequest>()) } returns
                rpcResult(Wei.ether("1").value.toHexString(), 1)

        val request = EthBalance(Solidity.Address(BigInteger.ONE), id = 1)

        val actual = repository.request(request)

        assertEquals(actual, request)

        assertEquals(
            EthRequest.Response.Success(Wei.ether("1")),
            request.response
        )

        coVerify(exactly = 1) { apiMock.post(request.toRpcRequest().request()) }
    }

    @Test
    fun requestFailure() = runBlocking {
        coEvery { apiMock.post(any<JsonRpcRequest>()) } returns
                rpcResult("0x", 1, "eth_getBalance should never error")

        val request = EthBalance(Solidity.Address(BigInteger.ONE), id = 1)

        val actual = repository.request(request)

        assertEquals(actual, request)

        assertEquals(
            EthRequest.Response.Failure<Wei>("eth_getBalance should never error"),
            request.response
        )

        coVerify(exactly = 1) { apiMock.post(request.toRpcRequest().request()) }
    }

    @Test
    fun getBalance() = runBlocking {
        coEvery { apiMock.post(any<JsonRpcRequest>()) } returns
                rpcResult(Wei.ether("1").value.toHexString())

        val actual = repository.getBalance(Solidity.Address(BigInteger.ONE))

        assertEquals(actual, Wei.ether("1"))

        coVerify(exactly = 1) { apiMock.post(EthBalance(Solidity.Address(BigInteger.ONE)).toRpcRequest().request()) }
    }

    @Test
    fun getBalanceFailure() = runBlocking {
        coEvery { apiMock.post(any<JsonRpcRequest>()) } returns
                rpcResult("0x", 0, "eth_getBalance should never error")

        val result = runCatching { repository.getBalance(Solidity.Address(BigInteger.ONE)) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is RequestFailedException && exceptionOrNull()?.message == "eth_getBalance should never error")
        }
        coVerify(exactly = 1) { apiMock.post(EthBalance(Solidity.Address(BigInteger.ONE)).toRpcRequest().request()) }
    }

    @Test
    fun sendRawTransaction() = runBlocking {
        coEvery { apiMock.post(any<JsonRpcRequest>()) } returns
                rpcResult("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1")


        val actual = repository.sendRawTransaction("0xSomeSignedManager")

        assertEquals(actual, "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1")

        coVerify(exactly = 1) { apiMock.post(EthSendRawTransaction("0xSomeSignedManager").toRpcRequest().request()) }
    }

    @Test
    fun sendRawTransactionFailure() = runBlocking {
        coEvery { apiMock.post(any<JsonRpcRequest>()) } returns
                rpcResult("0x", 0, "revert; But I won't tell you why")

        val result = runCatching { repository.sendRawTransaction("0xSomeSignedManager") }

        with(result) {
            assert(isFailure)
            assert(
                exceptionOrNull() is RequestFailedException &&
                        exceptionOrNull()?.message == "revert; But I won't tell you why (Could not send raw transaction)"
            )
        }
        coVerify(exactly = 1) {
            apiMock.post(EthSendRawTransaction("0xSomeSignedManager").toRpcRequest().request())
        }
    }

    @Test
    fun getTransactionReceipt() = runBlocking {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        val data = "0x0000000000000000000000009205b8f1a21a3cee0f6a629fd83cee0f6a629fd8"
        val topic0 = "0x4db17dd5e4732fb6da34a148104a592783ca119a1e7bb8829eba6cbadef0b511"
        coEvery { apiMock.receipt(any()) } returns
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

        val actual = repository.getTransactionReceipt(transactionHash)

        assertEquals(
            actual,
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

        coVerify(exactly = 1) {
            apiMock
                .receipt(
                    JsonRpcRequest(
                        method = "eth_getTransactionReceipt",
                        params = listOf(transactionHash)
                    )
                )
        }
    }

    @Test
    fun getTransactionReceiptFailure() = runBlocking {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        coEvery { apiMock.receipt(any()) } returns
                JsonRpcTransactionReceiptResult(
                    1, "2.0",
                    result = null
                )

        val result = runCatching { repository.getTransactionReceipt(transactionHash) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is TransactionReceiptNotFound)
        }

        coVerify(exactly = 1) {
            apiMock.receipt(
                JsonRpcRequest(
                    method = "eth_getTransactionReceipt",
                    params = listOf(transactionHash)
                )
            )
        }
    }

    @Test
    fun getTransactionByHash() = runBlocking {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        val data = "0x0000000000000000000000009205b8f1a21a3cee0f6a629fd83cee0f6a629fd8"
        coEvery { apiMock.transaction(any()) } returns
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

        val actual = repository.getTransactionByHash(transactionHash)

        assertEquals(
            actual,
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

        coVerify(exactly = 1) {
            apiMock.transaction(
                JsonRpcRequest(
                    method = "eth_getTransactionByHash",
                    params = listOf(transactionHash)
                )
            )
        }
    }

    @Test
    fun getTransactionByHashFailure() = runBlocking {
        val transactionHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        coEvery { apiMock.transaction(any()) } returns
                JsonRpcTransactionResult(
                    1, "2.0",
                    result = null
                )

        val result = runCatching { repository.getTransactionByHash(transactionHash) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is TransactionNotFound)
        }

        coVerify(exactly = 1) {
            apiMock.transaction(
                JsonRpcRequest(
                    method = "eth_getTransactionByHash",
                    params = listOf(transactionHash)
                )
            )
        }
    }

    @Test
    fun getBlockByHash() = runBlocking {
        val blockHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        coEvery { apiMock.block(any()) } returns
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

        val actual = repository.getBlockByHash(blockHash)

        assertEquals(
            actual,
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

        coVerify(exactly = 1) {
            apiMock.block(
                JsonRpcRequest(
                    method = "eth_getBlockByHash",
                    params = listOf(blockHash, false)
                )
            )
        }
    }

    @Test
    fun getBlockByHashFailure() = runBlocking {
        val blockHash = "0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"
        coEvery { apiMock.block(any()) } returns
                JsonRpcBlockResult(
                    1, "2.0",
                    result = null
                )

        val result = runCatching { repository.getBlockByHash(blockHash) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is BlockNotFound)
        }

        coVerify(exactly = 1) {
            apiMock.block(
                JsonRpcRequest(
                    method = "eth_getBlockByHash",
                    params = listOf(blockHash, false)
                )
            )
        }
    }

    @Test
    fun getTransactionParameters() = runBlocking {
        coEvery { apiMock.post(any<Collection<JsonRpcRequest>>()) } returns
                listOf(
                    rpcResult(BigInteger.valueOf(55000).toHexString(), 0),
                    rpcResult(Wei.ether("0.000000001").value.toHexString(), 1),
                    rpcResult("0x0a", 2)
                )

        val transaction = Transaction(Solidity.Address(BigInteger.ONE), value = Wei.ether("1"), data = "0x42cde4e8")
        val actual = repository.getTransactionParameters(
            Solidity.Address(BigInteger.TEN),
            transaction.address,
            transaction.value,
            transaction.data
        )

        assertEquals(
            actual,
            TransactionParameters(
                BigInteger.valueOf(77000),
                Wei.ether("0.000000001").value,
                BigInteger.valueOf(10)
            )
        )

        coVerify(exactly = 1) {
            apiMock.post(
                listOf(
                    EthEstimateGas(Solidity.Address(BigInteger.TEN), transaction, 0).toRpcRequest().request(),
                    EthGasPrice(1).toRpcRequest().request(),
                    EthGetTransactionCount(Solidity.Address(BigInteger.TEN), id = 2).toRpcRequest().request()
                )
            )
        }
    }

    @Test
    fun getTransactionParametersEstimateFailure() = runBlocking {
        testTransactionParametersFailure(
            listOf(
                rpcResult(error = "Something went wrong", id = 0),
                rpcResult(Wei.ether("0.000000001").value.toHexString(), 1),
                rpcResult("0x0a", 2)
            )
        )
    }

    @Test
    fun getTransactionParametersGasPriceFailure() = runBlocking {
        testTransactionParametersFailure(
            listOf(
                rpcResult(BigInteger.valueOf(55000).toHexString(), 0),
                rpcResult(error = "Something went wrong", id = 1),
                rpcResult("0x0a", 2)
            )
        )
    }

    @Test
    fun getTransactionParametersNonceFailure() = runBlocking {
        testTransactionParametersFailure(
            listOf(
                rpcResult(BigInteger.valueOf(55000).toHexString(), 0),
                rpcResult(Wei.ether("0.000000001").value.toHexString(), 1),
                rpcResult(error = "Something went wrong", id = 2)
            )
        )
    }

    private suspend fun testTransactionParametersFailure(rpcResults: List<JsonRpcResult>) {
        coEvery { apiMock.post(any<Collection<JsonRpcRequest>>()) } returns rpcResults

        val transaction = Transaction(Solidity.Address(BigInteger.ONE), value = Wei.ether("1"), data = "0x42cde4e8")
        val result = runCatching {
            repository.getTransactionParameters(
                Solidity.Address(BigInteger.TEN),
                transaction.address,
                transaction.value,
                transaction.data
            )
        }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is RequestFailedException)
        }

        coVerify(exactly = 1) {
            apiMock.post(
                listOf(
                    EthEstimateGas(Solidity.Address(BigInteger.TEN), transaction, 0).toRpcRequest().request(),
                    EthGasPrice(1).toRpcRequest().request(),
                    EthGetTransactionCount(Solidity.Address(BigInteger.TEN), id = 2).toRpcRequest().request()
                )
            )
        }
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
