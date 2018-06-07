package pm.gnosis.svalinn.utils.ethereum

import org.junit.Assert
import org.junit.Test
import pm.gnosis.utils.asEthereumAddress
import java.math.BigInteger

class EthereumUtilsTest {
    @Test
    fun getDeployAddressFromNonce() {
        val address = "0x6ac7ea33f8831ea9dcc53393aaa88b25a785dbf0".asEthereumAddress()!!
        Assert.assertEquals("0xcd234a471b72ba2f1ccf0a70fcaba648a5eecd8d".asEthereumAddress(), getDeployAddressFromNonce(address, BigInteger("0")))
        Assert.assertEquals("0x343c43a37d37dff08ae8c4a11544c718abb4fcf8".asEthereumAddress(), getDeployAddressFromNonce(address, BigInteger("1")))
        Assert.assertEquals("0xf778b86fa74e846c4f0a1fbd1335fe81c00a0c91".asEthereumAddress(), getDeployAddressFromNonce(address, BigInteger("2")))
        Assert.assertEquals("0xfffd933a0bc612844eaf0c6fe3e5b8e9b6c1d19c".asEthereumAddress(), getDeployAddressFromNonce(address, BigInteger("3")))
    }
}
