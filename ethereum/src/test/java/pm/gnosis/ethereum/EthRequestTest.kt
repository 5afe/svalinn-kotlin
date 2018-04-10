package pm.gnosis.ethereum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei
import pm.gnosis.tests.utils.Asserts.assertThrow
import java.math.BigInteger

class EthRequestTest {

    @Test
    fun success() {
        val request = EthBalance(Solidity.Address(BigInteger.ONE), 10)
        val result = Wei.ether("1")
        request.response = EthRequest.Response.Success(result)
        assertEquals(result, request.result())
        assertEquals(result, request.checkedResult())
    }

    @Test()
    fun failure() {
        val request = EthBalance(Solidity.Address(BigInteger.ONE), 10)
        val errorMsg = "Revert ... because we can"
        request.response = EthRequest.Response.Failure(errorMsg)
        assertNull("Should not return result on failure", request.result())
        assertThrow({
            request.checkedResult()
        }, "Should throw with failure result", {
            it is RequestFailedException && it.message == "Revert ... because we can"
        })
        assertThrow({
            request.checkedResult("Some custom message")
        }, "Should throw with custom message with failure result", {
            it is RequestFailedException && it.message == "Revert ... because we can (Some custom message)"
        })
    }

    @Test()
    fun notExecuted() {
        val request = EthBalance(Solidity.Address(BigInteger.ONE), 10)
        assertNull("Should not return result if not executed", request.result())
        assertThrow({
            request.checkedResult()
        }, "Should throw without result", {
            it is RequestNotExecutedException && it.message == null
        })
        assertThrow({
            request.checkedResult("Some custom message")
        }, "Should throw with custom message without result", {
            it is RequestNotExecutedException && it.message == "Some custom message"
        })
    }
}
