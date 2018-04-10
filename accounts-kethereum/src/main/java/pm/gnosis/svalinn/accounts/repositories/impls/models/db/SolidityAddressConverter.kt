package pm.gnosis.svalinn.accounts.repositories.impls.models.db

import android.arch.persistence.room.TypeConverter
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.hexAsBigIntegerOrNull
import java.math.BigInteger

// TODO: remove duplication (we have the same converter in the app module)
class SolidityAddressConverter {
    @TypeConverter
    fun fromHexString(address: String) = address.asEthereumAddress()!!

    @TypeConverter
    fun toHexString(address: Solidity.Address): String = address.asEthereumAddressString()
}

