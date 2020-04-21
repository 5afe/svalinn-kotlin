package pm.gnosis.eip712.adapters.moshi

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.models.Wei
import java.math.BigInteger

class TypeAdapterTest {

    @Test
    fun testWeiAdapterFromJson() {
        val weiValue = WeiAdapter().fromJson("0x10")
        assertEquals(BigInteger("16"), weiValue.value)
    }

    @Test
    fun testWeiAdapterToJson() {
        val weiString = WeiAdapter().toJson(Wei(BigInteger("16")))
        assertEquals("0x10", weiString)
    }
}
