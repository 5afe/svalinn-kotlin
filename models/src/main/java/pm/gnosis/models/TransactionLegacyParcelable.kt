package pm.gnosis.models

import android.os.Parcel
import android.os.Parcelable
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.nullOnThrow

data class TransactionLegacyParcelable(val transaction: Transaction.Legacy) : Parcelable {
    constructor(parcel: Parcel) : this(
        Transaction.Legacy(
            parcel.readString()!!.hexAsBigInteger(),
            parcel.readString()!!.asEthereumAddress()!!,
            parcel.readString()!!.asEthereumAddress(),
            nullOnThrow { Wei(parcel.readString()!!.hexAsBigInteger()) },
            parcel.readString(),
            nullOnThrow { parcel.readString()!!.hexAsBigInteger() },
            nullOnThrow { parcel.readString()!!.hexAsBigInteger() },
            nullOnThrow { parcel.readString()!!.hexAsBigInteger() }
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(transaction.chainId.toString(16))
        parcel.writeString(transaction.to.asEthereumAddressString())
        parcel.writeString(transaction.from?.asEthereumAddressString())
        parcel.writeString(transaction.value?.value?.toString(16))
        parcel.writeString(transaction.data)
        parcel.writeString(transaction.nonce?.toString(16))
        parcel.writeString(transaction.gas?.toString(16))
        parcel.writeString(transaction.gasPrice?.toString(16))
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TransactionLegacyParcelable> {
        override fun createFromParcel(parcel: Parcel) = TransactionLegacyParcelable(parcel)
        override fun newArray(size: Int): Array<TransactionLegacyParcelable?> = arrayOfNulls(size)
    }
}
