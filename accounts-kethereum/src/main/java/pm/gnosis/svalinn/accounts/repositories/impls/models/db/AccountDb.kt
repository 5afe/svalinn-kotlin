package pm.gnosis.svalinn.accounts.repositories.impls.models.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.security.db.EncryptedByteArray

@Entity(tableName = AccountDb.TABLE_NAME)
@TypeConverters(BigIntegerConverter::class, SolidityAddressConverter::class)
data class AccountDb(
    @PrimaryKey
    @ColumnInfo(name = PRIVATE_KEY_COL)
    var privateKey: EncryptedByteArray,

    @ColumnInfo(name = ADDRESS_COL)
    var address: Solidity.Address
) {
    companion object {
        const val TABLE_NAME = "account"
        const val PRIVATE_KEY_COL = "private_key"
        const val ADDRESS_COL = "address"
    }
}
