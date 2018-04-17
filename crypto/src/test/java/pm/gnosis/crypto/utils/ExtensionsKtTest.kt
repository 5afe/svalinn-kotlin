package pm.gnosis.crypto.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.utils.asEthereumAddress

class ExtensionsKtTest {
    // From https://github.com/ethereum/EIPs/blob/master/EIPS/eip-55.md#test-cases
    @Test
    fun asEthereumAddressChecksumString() {
        assertEquals(
            "0x52908400098527886E0F7030069857D2E4169EE7",
            "0x52908400098527886e0f7030069857d2e4169ee7".toChecksum()
        )

        assertEquals(
            "0x8617E340B3D01FA5F11F306F4090FD50E238070D",
            "0x8617e340b3d01fA5f11f306f4090fd50E238070d".toChecksum()
        )

        assertEquals(
            "0xde709f2102306220921060314715629080e2fb77",
            "0xde709f2102306220921060314715629080e2fb77".toChecksum()
        )

        assertEquals(
            "0x27b1fdb04752bbc536007a920d24acb045561c26",
            "0x27b1fdb04752bbc536007a920d24acb045561c26".toChecksum()
        )

        assertEquals(
            "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed",
            "0x5aaeb6053f3e94c9b9a09f33669435e7ef1beaed".toChecksum()
        )

        assertEquals(
            "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359",
            "0xfb6916095ca1df60bb79ce92ce3ea74c37c5d359".toChecksum()
        )

        assertEquals(
            "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB",
            "0xdbf03b407c01e7cd3cbea99509d93f8dddc8c6fb".toChecksum()
        )

        assertEquals(
            "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb",
            "0xd1220a0cf47c7b9be7a2e6ba89f429762e7b9adb".toChecksum()
        )
    }

    private fun String.toChecksum() = asEthereumAddress()!!.asEthereumAddressChecksumString()
}
