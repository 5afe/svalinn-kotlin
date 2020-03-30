package pm.gnosis.ethereum.rpc

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.rpc.models.*
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.toHexString
import java.math.BigInteger

class CoRpcEthereumRepositoryTest {

    private val apiMock = mockk<CoEthereumRpcConnector>()

    private lateinit var repository: CoRpcEthereumRepository

    @Before
    fun setup() {
        repository = CoRpcEthereumRepository(apiMock)
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
            EthCall(Solidity.Address(BigInteger.ONE), tx, 0),
            EthBalance(Solidity.Address(BigInteger.ONE), 1),
            EthGasPrice(2),
            EthGetTransactionCount(Solidity.Address(BigInteger.ONE), 3),
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
