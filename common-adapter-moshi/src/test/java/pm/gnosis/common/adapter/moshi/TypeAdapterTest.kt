package pm.gnosis.common.adapters.moshi

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei
import java.math.BigInteger

class TypeAdapterTest {

    @Test
    fun testWeiAdapterFromJson() {
        val weiValue = WeiAdapter().fromJson("0xde0b6b3a7640000")
        assertEquals(BigInteger("1000000000000000000"), weiValue.value)
    }

    @Test
    fun testWeiAdapterToJson() {
        val weiString = WeiAdapter().toJson(Wei(BigInteger("1000000000000000000")))
        assertEquals("0xde0b6b3a7640000", weiString)
    }

    @Test
    fun testHexNumberAdapterFromJson() {
        val value = HexNumberAdapter().fromJson("0xde0b6b3a7640000")
        assertEquals(BigInteger("1000000000000000000"), value)
    }

    @Test
    fun testHexNumberAdapterToJson() {
        val hexNumberString = HexNumberAdapter().toJson(BigInteger("1000000000000000000"))
        assertEquals("0xde0b6b3a7640000", hexNumberString)
    }

    @Test
    fun testDecimalNumberAdapterFromJson() {
        val value = DecimalNumberAdapter().fromJson("1000000000000000000")
        assertEquals(BigInteger("1000000000000000000"), value)
    }

    @Test
    fun testDecimalNumberAdapterToJson() {
        val decimalString = DecimalNumberAdapter().toJson(BigInteger("1000000000000000000"))
        assertEquals("1000000000000000000", decimalString)
    }

    @Test
    fun testDefaultNumberAdapterFromJson() {
        val value = DefaultNumberAdapter().fromJson("0xde0b6b3a7640000")
        assertEquals(BigInteger("1000000000000000000"), value)
    }

    @Test
    fun testDefaultNumberAdapterToJson() {
        val numberString = DefaultNumberAdapter().toJson(BigInteger("1000000000000000000"))
        assertEquals("0xde0b6b3a7640000", numberString)
    }

    @Test
    fun testSolidityAddressAdapterFromJson() {
        val addressValue = SolidityAddressAdapter().fromJson("0x0000000000000000000000000de0b6B3a7640000")
        assertEquals(BigInteger("1000000000000000000"), addressValue.value)
    }

    @Test
    fun testSolidityAddressAdapterToJson() {
        val addressString = SolidityAddressAdapter().toJson(Solidity.Address(BigInteger("1000000000000000000")))
        assertEquals("0x0000000000000000000000000de0b6B3a7640000", addressString)
    }
}
