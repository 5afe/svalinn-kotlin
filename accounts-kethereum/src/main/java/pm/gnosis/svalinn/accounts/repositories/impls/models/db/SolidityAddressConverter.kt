package pm.gnosis.svalinn.accounts.repositories.impls.models.db

import androidx.room.TypeConverter
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

// TODO: remove duplication (we have the same converter in the app module)
class SolidityAddressConverter {
    @TypeConverter
    fun fromHexString(address: String) = address.asEthereumAddress()!!

    @TypeConverter
    fun toHexString(address: Solidity.Address): String = address.asEthereumAddressString()
}

