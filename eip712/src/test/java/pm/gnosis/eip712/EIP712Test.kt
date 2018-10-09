package pm.gnosis.eip712

import org.junit.Assert.assertEquals
import org.junit.Test
import pm.gnosis.crypto.KeyPair
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.model.Solidity
import pm.gnosis.utils.*

class EIP712Test {
    @Test
    fun eip712() {
        val mail = Struct712(
            typeName = "Mail",
            parameters = listOf(
                "from" asParameterNameFor FROM_PERSON,
                "to" asParameterNameFor TO_PERSON,
                "contents" asParameterNameFor CONTENTS
            )
        )

        assertEquals(
            "8b73c3c69bb8fe3d512ecc4cf759cc79239f7b179b0ffacaa9a75d522b39400f",
            TEST_DOMAIN.typeHash.toHexString()
        )

        assertEquals(
            "a0cedeb2dc280ba39b857546d74f5549c3a1d7bdc2dd96bf881f76108e23dac2",
            mail.typeHash.toHexString()
        )

        assertEquals(
            "b9d8c78acf9b987311de6c7b45bb6a9c8e1bf361fa7fd3467a2163f994c79500",
            TO_PERSON.typeHash.toHexString()
        )

        assertEquals(
            "c52c0ee5d84264471806290a3f2c4cecfc5490626bf912d01f240d7a274b371e",
            mail.hashStruct().toHexString()
        )

        assertEquals(
            "f2cee375fa42b42143804025fc449deafd50cc031ca257e0b194a650a912090f",
            TEST_DOMAIN.hashStruct().toHexString()
        )
    }

    @Test
    fun eip712Bytes() {
        val mail = Struct712(
            typeName = "Mail",
            parameters = listOf(
                "from" asParameterNameFor FROM_PERSON,
                "to" asParameterNameFor TO_PERSON,
                "contents" asParameterNameFor CONTENTS,
                "payload" asParameterNameFor PAYLOAD
            )
        )

        assertEquals(
            "43999c52db673245777eb64b0330105de064e52179581a340a9856c32372528e",
            mail.typeHash.toHexString()
        )

        assertEquals(
            "e004bdc1ca57ba9ad5ea8c81e54dcbdb3bfce2d1d5ad92113f0871fb2a6eb052",
            mail.hashStruct().toHexString()
        )
    }

    @Test
    fun testSignature() {
        val mail = Struct712(
            typeName = "Mail",
            parameters = listOf(
                "from" asParameterNameFor FROM_PERSON,
                "to" asParameterNameFor TO_PERSON,
                "contents" asParameterNameFor CONTENTS
            )
        )

        val payload = typedDataHash(message = mail, domain = TEST_DOMAIN)

        assertEquals(
            "be609aee343fb3c4b28e1df9e632fca64fcfaede20f02e86244efddf30957bd2",
            payload.toHexString()
        )

        val privateKey = Sha3Utils.keccak("cow".toByteArray(Charsets.UTF_8))
        val keyPair = KeyPair.fromPrivate(privateKey)
        val address = Solidity.Address(keyPair.address.asBigInteger())

        assertEquals(
            "0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826",
            address.asEthereumAddressString()
        )

        val signature = keyPair.sign(payload)

        assertEquals(
            28.toByte(),
            signature.v
        )

        assertEquals(
            "0x4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d".hexAsBigInteger(),
            signature.r
        )

        assertEquals(
            "0x07299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b91562".hexAsBigInteger(),
            signature.s
        )
    }

    companion object {
        private val FROM_PERSON = Struct712(
            typeName = "Person", parameters = listOf(
                "name" asParameterNameFor Literal712(typeName = "string", value = Solidity.String("Cow")),
                "wallet" asParameterNameFor Literal712(
                    typeName = "address",
                    value = "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826".asEthereumAddress()!!
                )
            )
        )

        private val TO_PERSON = Struct712(
            typeName = "Person", parameters = listOf(
                "name" asParameterNameFor Literal712(typeName = "string", value = Solidity.String("Bob")),
                "wallet" asParameterNameFor Literal712(
                    typeName = "address",
                    value = "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB".asEthereumAddress()!!
                )
            )
        )

        private val CONTENTS = Literal712(typeName = "string", value = Solidity.String("Hello, Bob!"))

        private val PAYLOAD =
            Literal712(
                typeName = "bytes",
                value = Solidity.Bytes("0x25192142931f380985072cdd991e37f65cf8253ba7a0e675b54163a1d133b8ca".hexStringToByteArray())
            )

        private val TEST_DOMAIN = Struct712(
            typeName = EIP712_DOMAIN_TYPE,
            parameters = listOf(
                "name" asParameterNameFor Literal712(typeName = "string", value = Solidity.String("Ether Mail")),
                "version" asParameterNameFor Literal712(typeName = "string", value = Solidity.String("1")),
                "chainId" asParameterNameFor Literal712(typeName = "uint256", value = Solidity.UInt256(1.toBigInteger())),
                "verifyingContract" asParameterNameFor Literal712(
                    typeName = "address",
                    value = "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC".asEthereumAddress()!!
                )
            )
        )
    }
}
