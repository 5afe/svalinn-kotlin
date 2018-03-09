package pm.gnosis.erc67

import android.net.Uri
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.decimalAsBigInteger
import pm.gnosis.utils.hexAsEthereumAddressOrNull
import pm.gnosis.utils.nullOnThrow

import java.math.BigInteger

/*
 * See https://github.com/ethereum/EIPs/issues/67
 */
class ERC67Parser {
    companion object {
        const val SCHEMA = "ethereum:"
        const val VALUE_KEY = "value="
        const val GAS_KEY = "gas="
        const val GAS_PRICE_KEY = "gasPrice="
        const val DATA_KEY = "data="
        const val SEPARATOR = "?"
        const val QUERY_PARAM_SEPARATOR = "&"

        fun parse(string: String): Transaction? {
            if (!string.startsWith(SCHEMA)) return null
            val parts = string.split(SEPARATOR)
            val address = parts[0].removePrefix(SCHEMA).hexAsEthereumAddressOrNull() ?: return null

            var value: Wei? = null
            var gas: BigInteger? = null
            var gasPrice: BigInteger? = null
            var data: String? = null

            if (parts.size == 2) { // we have query params
                parts[1].split(QUERY_PARAM_SEPARATOR).forEach {
                    when {
                        it.startsWith(VALUE_KEY) -> value = nullOnThrow { Wei(it.removePrefix(VALUE_KEY).decimalAsBigInteger()) }
                        it.startsWith(GAS_KEY) -> gas = nullOnThrow { it.removePrefix(GAS_KEY).decimalAsBigInteger() }
                        it.startsWith(GAS_PRICE_KEY) -> gasPrice = nullOnThrow { it.removePrefix(GAS_PRICE_KEY).decimalAsBigInteger() }
                        it.startsWith(DATA_KEY) -> data = it.removePrefix(DATA_KEY)
                    }
                }
            }
            return Transaction(address, value, gas, gasPrice, data)
        }
    }
}

fun Transaction.erc67String(): String {
    val stringBuilder = StringBuilder("${ERC67Parser.SCHEMA}${address.asEthereumAddressString()}")
    val queryParams = kotlin.collections.mutableListOf<String>()
    value?.let { queryParams.add("${ERC67Parser.VALUE_KEY}${it.value}") }
    gas?.let { queryParams.add("${ERC67Parser.GAS_KEY}$it") }
    gasPrice?.let { queryParams.add("${ERC67Parser.GAS_PRICE_KEY}$it") }
    data?.let { queryParams.add("${ERC67Parser.DATA_KEY}$it") }

    if (queryParams.isNotEmpty()) {
        stringBuilder.append(ERC67Parser.SEPARATOR)
        stringBuilder.append(queryParams.reduce { q1, q2 -> "$q1${ERC67Parser.QUERY_PARAM_SEPARATOR}$q2" })
    }

    return stringBuilder.toString()
}

fun Transaction.erc67Uri(): Uri = android.net.Uri.fromParts("ethereum", erc67String().removePrefix(ERC67Parser.SCHEMA), null)
