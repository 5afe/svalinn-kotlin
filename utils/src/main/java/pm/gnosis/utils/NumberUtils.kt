package pm.gnosis.utils

import pm.gnosis.model.Solidity
import pm.gnosis.utils.exceptions.InvalidTransactionHashException
import java.math.BigDecimal
import java.math.BigInteger

fun String.asEthereumAddress() = nullOnThrow { Solidity.Address(hexAsBigInteger()) }

fun String.hexAsBigInteger() = BigInteger(this.removePrefix("0x"), 16)
fun String.hexAsBigIntegerOrNull() = nullOnThrow { this.hexAsBigInteger() }
fun String.decimalAsBigInteger() = BigInteger(this, 10)
fun String.decimalAsBigIntegerOrNull() = nullOnThrow { this.decimalAsBigInteger() }

fun ByteArray.asBigInteger() = BigInteger(1, this)

fun BigInteger.toHexString() = this.toString(16).addHexPrefix()


fun BigInteger.isValidEthereumAddress() = this <= BigInteger.valueOf(2).pow(160).minus(BigInteger.ONE)

fun BigInteger.asTransactionHash(): String {
    if (!isValidTransactionHash()) throw InvalidTransactionHashException(this)
    return "0x${this.toString(16).padStart(64, '0')}"
}

fun BigInteger.isValidTransactionHash() = this <= BigInteger.valueOf(2).pow(256).minus(BigInteger.ONE)

fun BigInteger.asDecimalString(): String = this.toString(10)

//Issue: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6480539
fun BigDecimal.stringWithNoTrailingZeroes(): String =
    if (this.unscaledValue() == BigInteger.ZERO) "0"
    else this.stripTrailingZeros().toPlainString()

fun String.isValidEthereumAddress() = asEthereumAddress() != null
