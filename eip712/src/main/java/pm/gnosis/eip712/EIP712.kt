package pm.gnosis.eip712

import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.model.Solidity
import pm.gnosis.model.SolidityBase
import pm.gnosis.utils.hexToByteArray

internal val solidityTypes = Solidity.types.keys
const val EIP712_DOMAIN_TYPE = "EIP712Domain"

data class Struct712Parameter(val name: String, val type: Type712)

internal infix fun String.asParameterNameFor(type: Type712) = Struct712Parameter(this, type)

sealed class Type712 {
    abstract val typeName: String
}

class Literal712(override val typeName: String, val value: SolidityBase.Type) : Type712()

class Struct712(override val typeName: String, val parameters: List<Struct712Parameter>) : Type712() {
    fun hashStruct() = Sha3Utils.keccak(typeHash + encodeParameters())

    val typeHash by lazy { Sha3Utils.keccak(encodeType().joinToString(separator = "").toByteArray(charset = Charsets.UTF_8)) }

    fun encodeParameters(): ByteArray =
        parameters.map { (_, type) ->
            when (type) {
                is Struct712 -> type.hashStruct()
                is Literal712 -> encodeSolidityType(type.value)
            }
        }.reduce { acc, bytes -> acc + bytes }

    private fun encodeType(): List<String> {
        val encodedStruct = parameters.joinToString(separator = ",", prefix = "$typeName(", postfix = ")",
            transform = { (name, type) -> "${type.typeName} $name" })
        val structParams = parameters.filter { (_, type) -> !solidityTypes.contains(type.typeName) }
            .mapNotNull { (_, type) -> (type as? Struct712)?.encodeType() }
            .flatten().distinct().sorted()
        return listOf(encodedStruct) + structParams
    }
}

fun encodeSolidityType(value: SolidityBase.Type): ByteArray = when (value) {
    // Solidity.String is also Solidity.Bytes
    is Solidity.Bytes -> Sha3Utils.keccak(value.items)
    is SolidityBase.Array<*> -> Sha3Utils.keccak(value.items.map { encodeSolidityType(value = it) }.reduce { acc, bytes -> acc + bytes })
    is SolidityBase.Vector<*> -> Sha3Utils.keccak(value.items.map { encodeSolidityType(value = it) }.reduce { acc, bytes -> acc + bytes })
    else -> value.encode().hexToByteArray()
}

fun typedDataHash(message: Struct712, domain: Struct712): ByteArray =
    Sha3Utils.keccak(byteArrayOf(0x19, 0x1) + domain.hashStruct() + message.hashStruct())
