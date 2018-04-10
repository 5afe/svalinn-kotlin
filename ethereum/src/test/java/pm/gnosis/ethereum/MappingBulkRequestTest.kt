package pm.gnosis.ethereum

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigInteger

class MappingBulkRequestTest {
    @Test
    fun testSuccess() {
        val etherBalance = EthBalance(Solidity.Address(BigInteger.TEN))
        val tokenBalance = EthCall(
            transaction = Transaction(Solidity.Address(BigInteger.ONE), data = "tokenBalanceData")
        )
        val request = MappingBulkRequest(
            MappedRequest(etherBalance, { it?.value }),
            MappedRequest(tokenBalance, {
                it?.let { BigInteger(it.removePrefix("0x"), 16) }
            })
        )

        etherBalance.response = EthRequest.Response.Success(Wei(BigInteger.valueOf(11)))
        tokenBalance.response = EthRequest.Response.Success("0x05")

        assertEquals(
            request.mapped(), listOf(
                BigInteger.valueOf(11),
                BigInteger.valueOf(5)
            )
        )
    }

    @Test
    fun testPartlyFailure() {
        val etherBalance = EthBalance(Solidity.Address(BigInteger.TEN))
        val tokenBalance = EthCall(
            transaction = Transaction(Solidity.Address(BigInteger.ONE), data = "tokenBalanceData")
        )
        val token2Balance = EthCall(
            transaction = Transaction(Solidity.Address(BigInteger.valueOf(5)), data = "token2BalanceData")
        )
        val request = MappingBulkRequest(
            MappedRequest(etherBalance, { it?.value }),
            MappedRequest(tokenBalance, {
                it?.let { BigInteger(it.removePrefix("0x"), 16) }
            }),
            MappedRequest(token2Balance, {
                it?.let { BigInteger(it.removePrefix("0x"), 16) }
            })
        )

        etherBalance.response = EthRequest.Response.Success(Wei(BigInteger.valueOf(11)))
        tokenBalance.response = EthRequest.Response.Failure("revert")
        token2Balance.response = EthRequest.Response.Success("0x05")

        assertEquals(
            request.mapped(), listOf(
                BigInteger.valueOf(11),
                null,
                BigInteger.valueOf(5)
            )
        )
    }
}
