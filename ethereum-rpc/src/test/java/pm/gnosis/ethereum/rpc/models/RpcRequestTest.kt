package pm.gnosis.ethereum.rpc.models

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.ethereum.*
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.toHexString
import java.math.BigInteger

class RpcRequestTest {

    @Test
    fun testTypes() {
        TEST_CASES.forEach {
            assertEquals(
                JsonRpcRequest(method = it.method, params = it.params, id = it.id),
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
        val response: EthRequest.Response<*>,
        val id: Int = 0
    )

    companion object {

        private fun rpcResult(result: String? = null, id: Int = 0, error: String? = null) =
            JsonRpcResult(id, "2.0", error?.let { JsonRpcError(23, it) }, result)

        private val TEST_TX = Transaction.Legacy(
            to = Solidity.Address(BigInteger.TEN),
            value = Wei.ether("1"),
            data = "0x42cde4e8",
            nonce = BigInteger.valueOf(42),
            gas = BigInteger.valueOf(133337),
            gasPrice = Wei.ether("0.000000001").value
        )

        private val TEST_CALL_PARAMS = TransactionCallParams(
            from = Solidity.Address(BigInteger.ONE).asEthereumAddressString(),
            to = Solidity.Address(BigInteger.TEN).asEthereumAddressString(),
            value = Wei.ether("1").value.toHexString(),
            data = "0x42cde4e8",
            nonce = BigInteger.valueOf(42).toHexString(),
            gas = BigInteger.valueOf(133337).toHexString(),
            gasPrice = Wei.ether("0.000000001").value.toHexString()
        )

        private val TEST_CASES = listOf(
            // Call
            TestCase(
                RpcCallRequest(EthCall(Solidity.Address(BigInteger.ONE), TEST_TX, id = 10)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "pending"),
                rpcResult("0x01", id = 10),
                EthRequest.Response.Success("0x01"),
                10
            ),
            TestCase(
                RpcCallRequest(EthCall(Solidity.Address(BigInteger.ONE), TEST_TX, id = 10, block = Block.PENDING)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "pending"),
                rpcResult("0x01", id = 10),
                EthRequest.Response.Success("0x01"),
                10
            ),
            TestCase(
                RpcCallRequest(EthCall(Solidity.Address(BigInteger.ONE), TEST_TX, id = 10, block = Block.LATEST)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "latest"),
                rpcResult("0x01", id = 10),
                EthRequest.Response.Success("0x01"),
                10
            ),
            TestCase(
                RpcCallRequest(EthCall(Solidity.Address(BigInteger.ONE), TEST_TX, id = 10, block = Block.EARLIEST)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "earliest"),
                rpcResult("0x01", id = 10),
                EthRequest.Response.Success("0x01"),
                10
            ),
            TestCase(
                RpcCallRequest(
                    EthCall(
                        Solidity.Address(BigInteger.ONE), TEST_TX, id = 10, block = BlockNumber(
                            BigInteger.TEN
                        )
                    )
                ),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "0xa"),
                rpcResult("0x01", id = 10),
                EthRequest.Response.Success("0x01"),
                10
            ),
            TestCase(
                RpcCallRequest(EthCall(Solidity.Address(BigInteger.ONE), TEST_TX)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "pending"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<String>("Some Error")
            ),
            TestCase(
                RpcCallRequest(EthCall(Solidity.Address(BigInteger.ONE), TEST_TX)),
                "eth_call",
                listOf(TEST_CALL_PARAMS, "pending"),
                rpcResult(),
                EthRequest.Response.Failure<String>("Missing result")
            ),

            // GetBalance
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE), id = 1)),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "pending"),
                rpcResult(Wei.ether("1").value.toHexString(), id = 1),
                EthRequest.Response.Success(Wei.ether("1")),
                1
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE), id = 1, block = Block.PENDING)),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "pending"),
                rpcResult(Wei.ether("1").value.toHexString(), id = 1),
                EthRequest.Response.Success(Wei.ether("1")),
                1
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE), id = 1, block = Block.LATEST)),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "latest"),
                rpcResult(Wei.ether("1").value.toHexString(), id = 1),
                EthRequest.Response.Success(Wei.ether("1")),
                1
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE), id = 1, block = Block.EARLIEST)),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "earliest"),
                rpcResult(Wei.ether("1").value.toHexString(), id = 1),
                EthRequest.Response.Success(Wei.ether("1")),
                1
            ),
            TestCase(
                RpcBalanceRequest(
                    EthBalance(
                        Solidity.Address(BigInteger.ONE),
                        id = 1,
                        block = BlockNumber(BigInteger.ONE)
                    )
                ),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "0x1"),
                rpcResult(Wei.ether("1").value.toHexString(), id = 1),
                EthRequest.Response.Success(Wei.ether("1")),
                1
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE))),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "pending"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<Wei>("Some Error")
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE))),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "pending"),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<Wei>("Invalid balance!")
            ),
            TestCase(
                RpcBalanceRequest(EthBalance(Solidity.Address(BigInteger.ONE))),
                "eth_getBalance",
                listOf(Solidity.Address(BigInteger.ONE).asEthereumAddressString(), "pending"),
                rpcResult(),
                EthRequest.Response.Failure<Wei>("Invalid balance!")
            ),

            // EstimateGas
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(Solidity.Address(BigInteger.ONE), TEST_TX, id = 1)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS),
                rpcResult(Wei.ether("0.002").value.toHexString(), id = 1),
                EthRequest.Response.Success(Wei.ether("0.002").value),
                1
            ),
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(Solidity.Address(BigInteger.ONE), TEST_TX)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<BigInteger>("Some Error")
            ),
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(Solidity.Address(BigInteger.ONE), TEST_TX)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<BigInteger>("Invalid estimate!")
            ),
            TestCase(
                RpcEstimateGasRequest(EthEstimateGas(Solidity.Address(BigInteger.ONE), TEST_TX)),
                "eth_estimateGas",
                listOf(TEST_CALL_PARAMS),
                rpcResult(),
                EthRequest.Response.Failure<BigInteger>("Invalid estimate!")
            ),

            // GasPrice
            TestCase(
                RpcGasPriceRequest(EthGasPrice(id = 2)),
                "eth_gasPrice",
                emptyList(),
                rpcResult(BigInteger.valueOf(20).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(20)),
                2
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
            TestCase(
                RpcGasPriceRequest(EthGasPrice()),
                "eth_gasPrice",
                emptyList(),
                rpcResult(),
                EthRequest.Response.Failure<BigInteger>("Invalid gas price!")
            ),

            // GetTransactionCount
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(Solidity.Address(BigInteger.TEN), id = 12)),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "pending"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(23)),
                12
            ),
            TestCase(
                RpcTransactionCountRequest(
                    EthGetTransactionCount(
                        Solidity.Address(BigInteger.TEN),
                        id = 12,
                        block = Block.PENDING
                    )
                ),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "pending"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(23)),
                12
            ),
            TestCase(
                RpcTransactionCountRequest(
                    EthGetTransactionCount(
                        Solidity.Address(BigInteger.TEN),
                        id = 12,
                        block = Block.LATEST
                    )
                ),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "latest"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(23)),
                12
            ),
            TestCase(
                RpcTransactionCountRequest(
                    EthGetTransactionCount(
                        Solidity.Address(BigInteger.TEN),
                        id = 12,
                        block = Block.EARLIEST
                    )
                ),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "earliest"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(23)),
                12
            ),
            TestCase(
                RpcTransactionCountRequest(
                    EthGetTransactionCount(
                        Solidity.Address(BigInteger.TEN), id = 12, block = BlockNumber(
                            BigInteger.ZERO
                        )
                    )
                ),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "0x0"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success(BigInteger.valueOf(23)),
                12
            ),
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(Solidity.Address(BigInteger.TEN))),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "pending"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<BigInteger>("Some Error")
            ),
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(Solidity.Address(BigInteger.TEN))),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "pending"),
                rpcResult("Invalid Number"),
                EthRequest.Response.Failure<BigInteger>("Invalid transaction count!")
            ),
            TestCase(
                RpcTransactionCountRequest(EthGetTransactionCount(Solidity.Address(BigInteger.TEN))),
                "eth_getTransactionCount",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), "pending"),
                rpcResult(),
                EthRequest.Response.Failure<BigInteger>("Invalid transaction count!")
            ),

            // GetStorageAt
            TestCase(
                RpcGetStorageAt(EthGetStorageAt(Solidity.Address(BigInteger.TEN), BigInteger.valueOf(12345), id = 15)),
                "eth_getStorageAt",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), BigInteger.valueOf(12345).toHexString(), "pending"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success("0x17"),
                15
            ),
            TestCase(
                RpcGetStorageAt(
                    EthGetStorageAt(
                        Solidity.Address(BigInteger.TEN),
                        BigInteger.valueOf(1234),
                        id = 15,
                        block = Block.PENDING
                    )
                ),
                "eth_getStorageAt",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), BigInteger.valueOf(1234).toHexString(), "pending"),
                rpcResult(BigInteger.valueOf(23).toHexString()),
                EthRequest.Response.Success("0x17"),
                15
            ),
            TestCase(
                RpcGetStorageAt(
                    EthGetStorageAt(
                        Solidity.Address(BigInteger.TEN),
                        BigInteger.valueOf(12346),
                        id = 15,
                        block = Block.LATEST
                    )
                ),
                "eth_getStorageAt",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), BigInteger.valueOf(12346).toHexString(), "latest"),
                rpcResult("some data"),
                EthRequest.Response.Success("some data"),
                15
            ),
            TestCase(
                RpcGetStorageAt(
                    EthGetStorageAt(
                        Solidity.Address(BigInteger.TEN),
                        BigInteger.valueOf(1236),
                        id = 15,
                        block = Block.EARLIEST
                    )
                ),
                "eth_getStorageAt",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), BigInteger.valueOf(1236).toHexString(), "earliest"),
                rpcResult(""),
                EthRequest.Response.Success(""),
                15
            ),
            TestCase(
                RpcGetStorageAt(
                    EthGetStorageAt(
                        Solidity.Address(BigInteger.TEN), BigInteger.valueOf(123), id = 15, block = BlockNumber(
                            BigInteger.ONE
                        )
                    )
                ),
                "eth_getStorageAt",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), BigInteger.valueOf(123).toHexString(), "0x1"),
                rpcResult(BigInteger.valueOf(22).toHexString()),
                EthRequest.Response.Success("0x16"),
                15
            ),
            TestCase(
                RpcGetStorageAt(EthGetStorageAt(Solidity.Address(BigInteger.TEN), BigInteger.valueOf(123))),
                "eth_getStorageAt",
                listOf(Solidity.Address(BigInteger.TEN).asEthereumAddressString(), BigInteger.valueOf(123).toHexString(), "pending"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<BigInteger>("Some Error")
            ),

            // SendRawTransaction
            TestCase(
                RpcSendRawTransaction(EthSendRawTransaction("0x42cde4e8SomeSignedData", id = 13)),
                "eth_sendRawTransaction",
                listOf("0x42cde4e8SomeSignedData"),
                rpcResult("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"),
                EthRequest.Response.Success("0x2709205b8f1a21a3cee0f6a629fd8dcfee589733741a877aba873cb379e97fa1"),
                13
            ),
            TestCase(
                RpcSendRawTransaction(EthSendRawTransaction("0x42cde4e8SomeSignedData")),
                "eth_sendRawTransaction",
                listOf("0x42cde4e8SomeSignedData"),
                rpcResult(error = "Some Error"),
                EthRequest.Response.Failure<String>("Some Error")
            ),
            TestCase(
                RpcSendRawTransaction(EthSendRawTransaction("0x42cde4e8SomeSignedData")),
                "eth_sendRawTransaction",
                listOf("0x42cde4e8SomeSignedData"),
                rpcResult(),
                EthRequest.Response.Failure<String>("Missing result")
            )
        )
    }
}
