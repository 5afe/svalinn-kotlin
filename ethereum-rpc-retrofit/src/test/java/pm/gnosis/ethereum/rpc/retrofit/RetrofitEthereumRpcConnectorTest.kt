package pm.gnosis.ethereum.rpc.retrofit

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import pm.gnosis.ethereum.rpc.models.*
import pm.gnosis.tests.utils.MockUtils
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class RetrofitEthereumRpcConnectorTest {

    private lateinit var connector: RetrofitEthereumRpcConnector

    @Mock
    private lateinit var api: RetrofitEthereumRpcApi


    private val request = JsonRpcRequest("2.0", "eth_getBalance", listOf(BigInteger.ONE, "latest"), 1)

    @Before
    fun setUp() {
        connector = RetrofitEthereumRpcConnector(api)
    }

    @Test()
    fun receipt() {
        val expected = NotImplementedError()

        given(api.receipt(MockUtils.any())).willCallRealMethod()

        assertEquals(expected, connector.receipt(request))
        then(api).should().receipt(request)
        then(api).shouldHaveNoMoreInteractions()
    }

    @Test(expected = NotImplementedError::class)
    fun block() {
        val expected = NotImplementedError()

        given(api.block(MockUtils.any())).willThrow(expected)

        assertEquals(expected, connector.block(request))
        then(api).should().block(request)
        then(api).shouldHaveNoMoreInteractions()
    }

    @Test(expected = NotImplementedError::class)
    fun transaction() {
        val expected = NotImplementedError()

        given(api.transaction(MockUtils.any())).willThrow(expected)

        assertEquals(expected, connector.transaction(request))
        then(api).should().transaction(request)
        then(api).shouldHaveNoMoreInteractions()
    }

    @Test(expected = NotImplementedError::class)
    fun post() {
        val expected = NotImplementedError()

        given(api.post(MockUtils.any<JsonRpcRequest>())).willThrow(expected)

        assertEquals(expected, connector.post(request))
        then(api).should().post(request)
        then(api).shouldHaveNoMoreInteractions()
    }

    @Test(expected = NotImplementedError::class)
    fun bulk() {
        val expected = NotImplementedError()

        given(api.post(MockUtils.any<Collection<JsonRpcRequest>>())).willThrow(expected)

        assertEquals(expected, connector.post(listOf(request)))
        then(api).should().post(listOf(request))
        then(api).shouldHaveNoMoreInteractions()
    }
}
