package pm.gnosis.svalinn.accounts.utils

import org.kethereum.functions.rlp.RLPElement
import org.kethereum.functions.rlp.RLPList
import org.kethereum.functions.rlp.RLPType
import org.kethereum.functions.rlp.encode
import org.kethereum.functions.rlp.toRLP
import pm.gnosis.crypto.ECDSASignature
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.models.Transaction
import pm.gnosis.utils.hexStringToByteArray
import java.math.BigInteger

fun Transaction.Eip1559.rlp(signature: ECDSASignature? = null): ByteArray {
    val items = ArrayList<RLPType>()
    items.add(chainId.toRLP())
    items.add(nonce!!.toRLP())
    items.add((maxPriorityFee ?: BigInteger.ZERO).toRLP())
    items.add((maxFeePerGas ?: BigInteger.ZERO).toRLP())
    items.add((gas ?: BigInteger.ZERO).toRLP())
    items.add(to.value.toRLP())
    items.add((value?.value ?: BigInteger.ZERO).toRLP())
    items.add((data?.hexStringToByteArray() ?: ByteArray(0)).toRLP())
    items.add(encodeAccessList())
    if (signature != null) {
        items.add(adjustV(signature.v).toRLP())
        items.add(signature.r.toRLP())
        items.add(signature.s.toRLP())
    }
    return RLPList(items).encode()
}

fun Transaction.Eip1559.encodeAccessList(): RLPType {
    val rlpAccessList = accessList.map { (address, storageKeys) ->
        val rlpAddress = address.hexStringToByteArray().toRLP()
        val rlpStorageKeys = storageKeys.map { it.hexStringToByteArray().toRLP() }
        val rlpStorageKeysList = RLPList(rlpStorageKeys)
        RLPList(listOf(rlpAddress, rlpStorageKeysList))
    }
    val rlpEncoded = RLPList(rlpAccessList)
    return rlpEncoded
}

private fun Transaction.Eip1559.adjustV(v: Byte): BigInteger {
    return BigInteger.valueOf(v.toLong() - 27)
}

fun Transaction.Eip1559.hash(ecdsaSignature: ECDSASignature? = null) = byteArrayOf(type, *rlp(ecdsaSignature)).let { Sha3Utils.keccak(it) }

fun Transaction.Legacy.rlp(signature: ECDSASignature? = null): ByteArray {
    val items = ArrayList<RLPElement>()
    items.add(nonce!!.toRLP())
    items.add(gasPrice!!.toRLP())
    items.add(gas!!.toRLP())
    items.add(to.value.toRLP())
    items.add((value?.value ?: BigInteger.ZERO).toRLP())
    items.add((data?.hexStringToByteArray() ?: ByteArray(0)).toRLP())
    if (signature != null) {
        items.add(adjustV(signature.v).toRLP())
        items.add(signature.r.toRLP())
        items.add(signature.s.toRLP())
    } else {
        items.add(chainId.toRLP())
        items.add(0.toRLP())
        items.add(0.toRLP())
    }

    return RLPList(items).encode()
}

fun Transaction.Legacy.hash(ecdsaSignature: ECDSASignature? = null) = rlp(ecdsaSignature).let { Sha3Utils.keccak(it) }

private fun Transaction.Legacy.adjustV(v: Byte): BigInteger {
    // requires v = {0, 1} or v = {27, 28}
    if (chainId > BigInteger.ZERO) {
        // EIP-155
        // If you do, then the v of the signature MUST be set to {0,1} + CHAIN_ID * 2 + 35
        // otherwise then v continues to be set to {0,1} + 27 as previously.
        if (v in 0..1) {
            return BigInteger.valueOf(v.toLong()).plus(chainId.multiply(BigInteger.valueOf(2)).plus(BigInteger.valueOf(35)))
        } else if (v in 27..28) {
            // KeyPair signature is always 27 or 28
            return BigInteger.valueOf(v.toLong() - 27).plus(chainId.multiply(BigInteger.valueOf(2)).plus(BigInteger.valueOf(35)))
        }
    }
    return BigInteger.valueOf(v.toLong())
}
