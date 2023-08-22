package pm.gnosis.svalinn.utils.ethereum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import java.math.BigInteger

class ERC67ParserTest {
    @Test
    fun validErc67() {
        VALID_ERC67.forEach { (input, expected) ->
            assertEquals(expected, ERC67Parser.parse(input))
        }
    }

    @Test
    fun invalidErc67() {
        INVALID_ERC67.forEach {
            assertNull(ERC67Parser.parse(it))
        }
    }

    @Test
    fun toERC67String() {
        assertEquals("ethereum:0x0000000000000000000000000000000000000000", Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO)).erc67String())
        assertEquals(
            "ethereum:0x0000000000000000000000000000000000000000?value=1",
            Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), value = Wei(BigInteger.ONE)).erc67String()
        )
        assertEquals(
            "ethereum:0x0000000000000000000000000000000000000000?value=1&gas=1",
            Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), value = Wei(BigInteger.ONE), gas = BigInteger.ONE).erc67String()
        )
        assertEquals(
            "ethereum:0x0000000000000000000000000000000000000000?value=1&gas=1&gasPrice=10",
            Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), value = Wei(BigInteger.ONE), gas = BigInteger.ONE, gasPrice = BigInteger.TEN).erc67String()
        )
        assertEquals(
            "ethereum:0x0000000000000000000000000000000000000000?value=1&gas=1&gasPrice=10&data=0x10",
            Transaction.Legacy(
                to = Solidity.Address(BigInteger.ZERO),
                value = Wei(BigInteger.ONE),
                gas = BigInteger.ONE,
                gasPrice = BigInteger.TEN,
                data = "0x10"
            ).erc67String()
        )
    }

    companion object {
        val VALID_ERC67 = listOf(
            "ethereum:0x0" to Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO)),
            "ethereum:0x0?" to Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO)),
            "ethereum:0x0?value=1" to Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), value = Wei(BigInteger.ONE)),
            "ethereum:0x1?gas=1" to Transaction.Legacy(to = Solidity.Address(BigInteger.ONE), gas = BigInteger.ONE),
            "ethereum:0x0?data=0x10" to Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), data = "0x10"),
            "ethereum:0x0?gasPrice=10" to Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), gasPrice = BigInteger.TEN),
            "ethereum:0x0?value=1&gas=2" to Transaction.Legacy(to = Solidity.Address(BigInteger.ZERO), value = Wei(BigInteger.ONE), gas = BigInteger("2")),
            "ethereum:0x0?gasPrice=10&value=2&data=0x10" to Transaction.Legacy(
                to = Solidity.Address(BigInteger.ZERO),
                gasPrice = BigInteger.TEN,
                value = Wei(BigInteger("2")),
                data = "0x10"
            )
        )

        val INVALID_ERC67 = listOf(
            "ethereum:",
            "ethereum",
            "ethereum0x0?value=1",
            "0x1?gas=1",
            ":0x0?data=0x10"
        )
    }
}
