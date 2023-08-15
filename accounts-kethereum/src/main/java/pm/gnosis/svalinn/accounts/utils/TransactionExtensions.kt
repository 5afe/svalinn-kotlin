package pm.gnosis.svalinn.accounts.utils

import org.kethereum.functions.rlp.RLPElement
import org.kethereum.functions.rlp.RLPList
import org.kethereum.functions.rlp.encode
import org.kethereum.functions.rlp.toRLP
import pm.gnosis.crypto.ECDSASignature
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.models.Transaction
import pm.gnosis.models.TransactionEip1559
import pm.gnosis.utils.hexStringToByteArray
import java.math.BigInteger

fun TransactionEip1559.rlp(signature: ECDSASignature? = null): ByteArray {
    val items = ArrayList<RLPElement>()
    items.add(chainId.toRLP())
    items.add(nonce!!.toRLP())
    items.add((fee.maxPriorityFee ?: BigInteger.ZERO).toRLP())
    items.add((fee.maxFeePerGas ?: BigInteger.ZERO).toRLP())
    items.add((fee.gas ?: BigInteger.ZERO).toRLP())
    items.add(to.value.toRLP())
    items.add((value?.value ?: BigInteger.ZERO).toRLP())
    items.add((data?.hexStringToByteArray() ?: ByteArray(0)).toRLP())
    if (signature != null) {
        items.add(adjustV(signature.v).toRLP())
        items.add(signature.r.toRLP())
        items.add(signature.s.toRLP())
    } else if (chainId > BigInteger.ZERO) {
        items.add(chainId.toRLP())
        items.add(0.toRLP())
        items.add(0.toRLP())
    }

    return RLPList(items).encode()
}

private fun TransactionEip1559.adjustV(v: Byte): Byte {
    if (chainId > BigInteger.ZERO) {
        return chainId
            .multiply(
                BigInteger.valueOf(2)
            )
            .plus(
                BigInteger.valueOf(v.toLong() + 8)
            )
            .toByte()
    }
    return v
}

fun TransactionEip1559.hash(ecdsaSignature: ECDSASignature? = null) = rlp(ecdsaSignature).let { Sha3Utils.keccak(it) }

fun Transaction.rlp(signature: ECDSASignature? = null): ByteArray {
    val items = ArrayList<RLPElement>()
    items.add(nonce!!.toRLP())
    items.add(gasPrice!!.toRLP())
    items.add(gas!!.toRLP())
    items.add(address.value.toRLP())
    items.add((value?.value ?: BigInteger.ZERO).toRLP())
    items.add((data?.hexStringToByteArray() ?: ByteArray(0)).toRLP())

    if (signature != null) {
        items.add(adjustV(signature.v).toRLP())
        items.add(signature.r.toRLP())
        items.add(signature.s.toRLP())
    } else if (chainId > BigInteger.ZERO) {
        items.add(chainId.toRLP())
        items.add(0.toRLP())
        items.add(0.toRLP())
    }

    return RLPList(items).encode()
}

fun Transaction.hash(ecdsaSignature: ECDSASignature? = null) = rlp(ecdsaSignature).let { Sha3Utils.keccak(it) }

private fun Transaction.adjustV(v: Byte): Byte {
    if (chainId > BigInteger.ZERO) {
        return chainId
            .multiply(
                BigInteger.valueOf(2)
            )
            .plus(
                BigInteger.valueOf(v.toLong() + 8)
            )
            .toByte()
    }
    return v
}
