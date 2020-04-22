package pm.gnosis.eip712.adapters.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.parseToBigInteger
import pm.gnosis.utils.toHexString
import java.math.BigInteger

object MoshiBuilderFactory {
    fun makeMoshiBuilder(): Moshi.Builder {
        return Moshi.Builder()
            .add(WeiAdapter())
            .add(HexNumberAdapter())
            .add(DecimalNumberAdapter())
            .add(DefaultNumberAdapter())
            .add(SolidityAddressAdapter())
    }
}

class WeiAdapter {
    @ToJson
    fun toJson(wei: Wei): String =
        wei.value.toHexString()

    @FromJson
    fun fromJson(wei: String): Wei {
        return Wei(wei.parseToBigInteger())
    }
}

class HexNumberAdapter {
    @ToJson
    fun toJson(@HexNumber hexNumber: BigInteger): String = hexNumber.toHexString()

    @FromJson
    @HexNumber
    fun fromJson(hexNumber: String): BigInteger = hexNumber.hexAsBigInteger()
}

class DecimalNumberAdapter {
    @ToJson
    fun toJson(@DecimalNumber bigInteger: BigInteger): String = bigInteger.toString()

    @FromJson
    @DecimalNumber
    fun fromJson(decimalNumber: String): BigInteger = decimalNumber.toBigInteger()
}

class DefaultNumberAdapter {
    @ToJson
    fun toJson(hexNumber: BigInteger): String = hexNumber.toHexString()

    @FromJson
    fun fromJson(hexNumber: String): BigInteger = hexNumber.hexAsBigInteger()
}

class SolidityAddressAdapter {
    @ToJson
    fun toJson(address: Solidity.Address): String = address.asEthereumAddressChecksumString()

    @FromJson
    fun fromJson(address: String): Solidity.Address = address.asEthereumAddress()!!
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class HexNumber

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class DecimalNumber
