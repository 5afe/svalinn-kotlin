package pm.gnosis.ethereum.rpc.retrofit

import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import pm.gnosis.ethereum.rpc.models.*
import java.math.BigInteger

class RetrofitEthereumRpcConnectorTest {

    private lateinit var connector: RetrofitEthereumRpcConnector

    private val api = object : RetrofitEthereumRpcApi {
        override suspend fun receipt(rpcUrl: String, jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionReceiptResult {
            TODO("Not yet implemented")
        }

        override suspend fun block(rpcUrl: String, jsonRpcRequest: JsonRpcRequest): JsonRpcBlockResult {
            TODO("Not yet implemented")
        }

        override suspend fun transaction(rpcUrl: String, jsonRpcRequest: JsonRpcRequest): JsonRpcTransactionResult {
            TODO("Not yet implemented")
        }

        override suspend fun post(rpcUrl: String, jsonRpcRequest: JsonRpcRequest): JsonRpcResult {
            TODO("Not yet implemented")
        }

        override suspend fun post(rpcUrl: String, jsonRpcRequest: Collection<JsonRpcRequest>): Collection<JsonRpcResult> {
            TODO("Not yet implemented")
        }
    }

    private val apiSpy = spyk(api)

    private val request = JsonRpcRequest("2.0", "eth_getBalance", listOf(BigInteger.ONE, "latest"), 1)

    @Before
    fun setUp() {
        connector = RetrofitEthereumRpcConnector(apiSpy, "")
    }

    @Test
    fun receipt() = runBlocking {
        val result = runCatching { connector.receipt(request) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is NotImplementedError)
        }
        coVerify(exactly = 1) { apiSpy.receipt(any(), request) }
    }

    @Test
    fun block() = runBlocking {
        val result = runCatching { connector.block(request) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is NotImplementedError)
        }
        coVerify(exactly = 1) { apiSpy.block(any(), request) }
    }

    @Test
    fun transaction() = runBlocking {
        val result = runCatching { connector.transaction(request) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is NotImplementedError)
        }
        coVerify(exactly = 1) { apiSpy.transaction(any(), request) }
    }

    @Test
    fun post() = runBlocking {
        val result = runCatching { connector.post(request) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is NotImplementedError)
        }
        coVerify(exactly = 1) { apiSpy.post(any(), request) }
    }

    @Test
    fun bulk() = runBlocking {
        val result = runCatching { connector.post(listOf(request)) }

        with(result) {
            assert(isFailure)
            assert(exceptionOrNull() is NotImplementedError)
        }
        coVerify(exactly = 1) { apiSpy.post(any(), listOf(request)) }
    }
}
