package pm.gnosis.ethereum.rpc.models

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.ethereum.*
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.asEthereumAddressStringOrNull
import pm.gnosis.utils.toHexString
import java.math.BigInteger

class RpcRequestTest {

    @Test
    fun testTypes() {
        TEST_CASES.forEach {
            assertEquals(
                JsonRpcRequest(method = it.method, params = it.params),
                it.request.request()
            )

            it.request.parse(it.rpcResult)

            assertEquals(
                it.response,
                it.request.raw.response
            )
        }
    }

    private data class TestCase(
        val request: RpcRequest<*>,
        val method: String,
        val params: List<Any>,
        val rpcResult: JsonRpcResult,
        val response: EthRequest.Response<*>
    )

    companion object {

        private fun rpcResult(result: String = "0x", id: Int = 0, error: String? = null) =
            JsonRpcResult(id, "2.0", error?.let { JsonRpcError(23, it) }, result)

        private val TEST_TX = Transaction(
            BigInteger.TEN,
            value = Wei.ether("1"),
            data = "0x42cde4e8",
            nonce = BigInteger.valueOf(42),
            gas = BigInteger.valueOf(133337),
            gasPrice = Wei.ether("0.000000001").value
        )

        private val TEST_CALL_PARAMS = TransactionCallParams(
            from = BigInteger.ONE.asEthereumAddressStringOrNull(),
            to = BigInteger.TEN.asEthereumAddressStringOrNull(),
            value = Wei.ether("1").value.toHexString(),
            data = "0x42cde4e8",
            nonce = BigInteger.valueOf(42).toHexString(),
            gas = BigInteger.valueOf(133337).toHexString(),
            gasPrice = Wei.ether("0.000000001").value.toHexString()
        )

        private val TEST_CASES = listOf(
            // Call
            TestCase(
                RpcCallRequest(EthCall(BigInteger.ONE, TEST_TX)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "latest"),
                rpcResult("0x01"),
                EthRequest.Response.Success("0x01")
            ),
            TestCase(
                RpcCallRequest(EthCall(BigInteger.ONE, TEST_TX)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "latest"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<String>("Some Error")
            ),

            // GetBalance
            TestCase(
                RpcBalanceRequest(EthBalance(BigInteger.ONE)),
                "eth_getBalance",
                listOf(BigInteger.ONE.asEthereumAddressString(), "latest"),
                rpcResult(Wei.ether("1").value.toHexString()),
                EthRequest.Response.Success(Wei.ether("1"))
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(BigInteger.ONE)),
                "eth_getBalance",
                listOf(BigInteger.ONE.asEthereumAddressString(), "latest"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<Wei>("Some Error")
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(BigInteger.ONE)),
                "eth_getBalance",
                listOf(BigInteger.ONE.asEthereumAddressString(), "latest"),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<Wei>("Invalid balance!")
            ),

            // EstimateGas
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(BigInteger.ONE, TEST_TX)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS, "latest"),
                rpcResult(Wei.ether("0.002").value.toHexString()),
                EthRequest.Response.Success(Wei.ether("0.002").value)
            ),
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(BigInteger.ONE, TEST_TX)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS, "latest"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<BigInteger>("Some Error")
            ),
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(BigInteger.ONE, TEST_TX)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS, "latest"),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<BigInteger>("Invalid estimate!")
            ),

            // GasPrice
            TestCase(
                RpcGasPriceRequest(EthGasPrice()),
                "eth_gasPrice",
                emptyList(),
                rpcResult(BigInteger.valueOf(20).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(20))
            ),
            TestCase(
                RpcGasPriceRequest(EthGasPrice()),
                "eth_gasPrice",
                emptyList(),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<Wei>("Some Error")
            ),
            TestCase(
                RpcGasPriceRequest(EthGasPrice()),
                "eth_gasPrice",
                emptyList(),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<BigInteger>("Invalid gas price!")
            ),

            // GetTransactionCount
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(BigInteger.TEN)),
                "eth_getTransactionCount",
                listOf(BigInteger.TEN.asEthereumAddressString(), "pending"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(23))
            ),
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(BigInteger.TEN)),
                "eth_getTransactionCount",
                listOf(BigInteger.TEN.asEthereumAddressString(), "pending"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<BigInteger>("Some Error")
            ),
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(BigInteger.TEN)),
                "eth_getTransactionCount",
                listOf(BigInteger.TEN.asEthereumAddressString(), "pending"),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<BigInteger>("Invalid transaction count!")
            ),

            // SendRawTransaction
            TestCase(
                RpcSendRawTransaction(EthSendRawTransaction("0x42cde4e8SomeSignedData")),
                "eth_sendRawTransaction",
                listOf("0x42cde4e8SomeSignedData"),
                rpcResult("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"),
                EthRequest.Response.Success("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1")
            ),
            TestCase(
                RpcSendRawTransaction(EthSendRawTransaction("0x42cde4e8SomeSignedData")),
                "eth_sendRawTransaction",
                listOf("0x42cde4e8SomeSignedData"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<String>("Some Error")
            )
        )
    }
}