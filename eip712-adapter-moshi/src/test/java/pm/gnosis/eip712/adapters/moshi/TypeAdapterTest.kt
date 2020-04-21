package pm.gnosis.eip712.adapters.moshi

import org.junit.Assert.assertEquals
import org.junit.Test
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
}
