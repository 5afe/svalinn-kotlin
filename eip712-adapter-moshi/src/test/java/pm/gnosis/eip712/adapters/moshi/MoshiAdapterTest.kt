package pm.gnosis.eip712.adapters.moshi

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.eip712.EIP712JsonParser
import pm.gnosis.eip712.typedDataHash
import pm.gnosis.utils.toHexString

class MoshiAdapterTest {
    @Test
    fun testJsonParser() {
        val inputSource = javaClass.classLoader.getResourceAsStream(PAYLOAD_JSON_FILE_NAME)
            ?: throw IllegalStateException("Could not read file $PAYLOAD_JSON_FILE_NAME")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)

        assertEquals(
            "8b73c3c69bb8fe3d512ecc4cf759cc79239f7b179b0ffacaa9a75d522b39400f",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "f2cee375fa42b42143804025fc449deafd50cc031ca257e0b194a650a912090f",
            domainWithMessage.domain.hashStruct().toHexString()
        )

        assertEquals(
            "c52c0ee5d84264471806290a3f2c4cecfc5490626bf912d01f240d7a274b371e",
            domainWithMessage.message.hashStruct().toHexString()
        )
    }

    @Test
    fun testSafeTxPayload1() {
        val inputSource = javaClass.classLoader.getResourceAsStream("safe_tx_payload_1.json")
            ?: throw IllegalStateException("Could not read file")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
        assertEquals(
            "035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "14d461bc7412367e924637b363c7bf29b8f47e2f84869f4426e5633d8af47b20",
            domainWithMessage.message.typeHash.toHexString()
        )

        assertEquals(
            "c9d69a2350aede7978fdee58e702647e4bbdc82168577aa4a43b66ad815c6d1a",
            typedDataHash(domainWithMessage.message, domainWithMessage.domain).toHexString()
        )
    }

    @Test
    fun testSafeTxPayload2() {
        val inputSource = javaClass.classLoader.getResourceAsStream("safe_tx_payload_2.json")
            ?: throw IllegalStateException("Could not read file")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
        assertEquals(
            "035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "14d461bc7412367e924637b363c7bf29b8f47e2f84869f4426e5633d8af47b20",
            domainWithMessage.message.typeHash.toHexString()
        )

        assertEquals(
            "8ca8db91d72b379193f6e229eb2dff0d0621b6ef452d90638ee3206e9b7349b3",
            typedDataHash(domainWithMessage.message, domainWithMessage.domain).toHexString()
        )
    }

    @Test
    fun testSafeTxPayload3() {
        val inputSource = javaClass.classLoader.getResourceAsStream("safe_tx_payload_3.json")
            ?: throw IllegalStateException("Could not read file")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
        assertEquals(
            "035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "14d461bc7412367e924637b363c7bf29b8f47e2f84869f4426e5633d8af47b20",
            domainWithMessage.message.typeHash.toHexString()
        )

        assertEquals(
            "1863bbed0d8fdd3c5b132495bac41db35cf3a6190ccd02cd511199b9476d269e",
            typedDataHash(domainWithMessage.message, domainWithMessage.domain).toHexString()
        )
    }

    @Test
    fun testSafeTxPayload4() {
        // This file has ints bigger than Integer.MAX_VALUE. Should be a String in Json
        val inputSource = javaClass.classLoader.getResourceAsStream("safe_tx_payload_4.json")
            ?: throw IllegalStateException("Could not read file")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
        assertEquals(
            "035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "14d461bc7412367e924637b363c7bf29b8f47e2f84869f4426e5633d8af47b20",
            domainWithMessage.message.typeHash.toHexString()
        )

        assertEquals(
            "30ae451e4e933a6e7703221298a2baf5292b3a954b67e0ddef997b1754920db0",
            typedDataHash(domainWithMessage.message, domainWithMessage.domain).toHexString()
        )
    }

    @Test
    fun testSafeTxPayload5() {
        // This file has ints bigger than Integer.MAX_VALUE. Should be a String in Json
        val inputSource = javaClass.classLoader.getResourceAsStream("safe_tx_payload_5.json")
            ?: throw IllegalStateException("Could not read file")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
        assertEquals(
            "035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "14d461bc7412367e924637b363c7bf29b8f47e2f84869f4426e5633d8af47b20",
            domainWithMessage.message.typeHash.toHexString()
        )

        assertEquals(
            "d3bfdcdd807f4db717646a35f333ed90cab0f94f31a0d428f688429c41039fe4",
            typedDataHash(domainWithMessage.message, domainWithMessage.domain).toHexString()
        )
    }

    @Test
    fun testSafeTxPayload6() {
        // This file has ints bigger than Integer.MAX_VALUE. Should be a String in Json
        val inputSource = javaClass.classLoader.getResourceAsStream("safe_tx_payload_6.json")
            ?: throw IllegalStateException("Could not read file")

        val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
        assertEquals(
            "035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749",
            domainWithMessage.domain.typeHash.toHexString()
        )

        assertEquals(
            "14d461bc7412367e924637b363c7bf29b8f47e2f84869f4426e5633d8af47b20",
            domainWithMessage.message.typeHash.toHexString()
        )

        assertEquals(
            "e0c6798c794ee4f3a346f98b5be5225aec4b159e586271004b0a7b34653b0657",
            typedDataHash(domainWithMessage.message, domainWithMessage.domain).toHexString()
        )
    }

    companion object {
        const val PAYLOAD_JSON_FILE_NAME = "mail_json_payload.json"
    }
}
