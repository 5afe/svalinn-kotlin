package pm.gnosis.eip712

import pm.gnosis.model.Solidity
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.hexStringToByteArray
import pm.gnosis.utils.nullOnThrow
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger

data class DomainWithMessage(val domain: Struct712, val message: Struct712)

class EIP712JsonParser(private val jsonAdapter: EIP712JsonAdapter) {
    fun parseMessage(rawJson: String): DomainWithMessage = parseMessage(jsonAdapter.parse(rawJson))

    fun parseMessage(inputStream: InputStream): DomainWithMessage = parseMessage(jsonAdapter.parse(inputStream))

    private fun parseMessage(adapterResult: EIP712JsonAdapter.Result) =
        DomainWithMessage(
            domain = buildStruct712(
                typeName = EIP712_DOMAIN_TYPE,
                values = adapterResult.domain,
                typeSpec = adapterResult.types
            ),
            message = buildStruct712(
                typeName = adapterResult.primaryType,
                values = adapterResult.message,
                typeSpec = adapterResult.types
            )
        )

    private fun buildStruct712(
        typeName: String,
        values: Map<String, Any>,
        typeSpec: Map<String, List<EIP712JsonAdapter.Parameter>>
    ): Struct712 {
        val params = typeSpec[typeName] ?: throw IllegalArgumentException("TypedDate does not contain type $typeName")
        val innerParams = params.map { typeParam ->
            val type712 = if (typeSpec.contains(typeParam.type)) {
                // Struct
                buildStruct712(
                    typeName = typeParam.type,
                    values = values[typeParam.name] as Map<String, Any>,
                    typeSpec = typeSpec
                )
            } else {
                // Literal
                // TODO this doesn't check size constraints ie.: having uint8 with a value greater than 8 bits
                val rawValue = values[typeParam.name] ?: throw IllegalArgumentException("Could not get value for property ${typeParam.name}")
                if (!solidityTypes.contains(typeParam.type)) throw IllegalArgumentException("Property with name ${typeParam.name} has invalid Solidity type ${typeParam.type}")
                val bivrostType = when {
                    typeParam.type.startsWith(prefix = "uint") -> readNumber(rawNumber = rawValue, creator = { Solidity.UInt256(it) })
                    typeParam.type.startsWith(prefix = "int") -> readNumber(rawNumber = rawValue, creator = { Solidity.Int256(it) })
                    typeParam.type == "bytes" -> Solidity.Bytes(rawValue.toString().hexStringToByteArray())
                    typeParam.type == "string" -> Solidity.String(rawValue.toString())
                    typeParam.type.startsWith(prefix = "bytes") -> Solidity.Bytes32(rawValue.toString().hexStringToByteArray())
                    typeParam.type == "bool" -> readBool(rawBool = rawValue)
                    typeParam.type == "address" -> readNumber(rawNumber = rawValue, creator = { Solidity.Address(it) })
                    else -> throw IllegalArgumentException("Unknown literal type ${typeParam.type}")
                }
                Literal712(typeName = typeParam.type, value = bivrostType)
            }
            Struct712Parameter(name = typeParam.name, type = type712)
        }
        return Struct712(typeName = typeName, parameters = innerParams)
    }

    private fun <T> readNumber(rawNumber: Any, creator: (BigInteger) -> T): T =
        when (rawNumber) {
            is Number -> creator(BigDecimal(rawNumber.toString()).exactNumber())
            is String -> {
                if (rawNumber.startsWith(prefix = "0x")) creator(rawNumber.hexAsBigInteger())
                else creator(BigDecimal(rawNumber).exactNumber())
            }
            else -> throw IllegalArgumentException("Value $rawNumber is neither a Number nor String")
        }

    private fun readBool(rawBool: Any): Solidity.Bool =
        if (rawBool is Boolean) Solidity.Bool(rawBool)
        else if (rawBool.toString().equals("true", ignoreCase = true) || rawBool.toString().equals("false", ignoreCase = true))
            Solidity.Bool(rawBool.toString().equals("true", ignoreCase = true))
        else throw java.lang.IllegalArgumentException("Value $rawBool is not a Boolean")

    private fun BigDecimal.exactNumber() =
        nullOnThrow { toBigIntegerExact() } ?: throw IllegalArgumentException("Value ${toString()} is a decimal (not supported)")
}
