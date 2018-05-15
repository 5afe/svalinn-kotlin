package pm.gnosis.svalinn.accounts.base.models

import java.math.BigInteger


data class Signature(val r: BigInteger, val s: BigInteger, val v: Byte) {
    init {
        if (v !in 27..34) throw IllegalStateException("v not in valid range")
    }

    override fun toString(): String {
        return r.toString(16).padStart(64, '0').substring(0, 64) +
                s.toString(16).padStart(64, '0').substring(0, 64) +
                v.toString(16).padStart(2, '0')
    }

    companion object {
        fun from(encoded: String, chainId: Int = 0): Signature {
            if (encoded.length != 130) throw IllegalArgumentException()
            val r = BigInteger(encoded.substring(0, 64), 16)
            val s = BigInteger(encoded.substring(64, 128), 16)
            val v = encoded.substring(128, 130).toByte(16).adjustV(chainId)

            return Signature(r, s, v)
        }

        fun fromChainId(r: BigInteger, s: BigInteger, v: Byte, chainId: Int) = Signature(r, s, v.adjustV(chainId))

        private fun Byte.adjustV(chainId: Int) = this.toInt().let {
            if (it >= 35) it - 8 - 2 * chainId
            else it
        }.toByte()
    }
}
