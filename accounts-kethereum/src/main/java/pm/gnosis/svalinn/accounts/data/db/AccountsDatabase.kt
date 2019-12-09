package pm.gnosis.svalinn.accounts.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pm.gnosis.svalinn.accounts.repositories.impls.models.db.AccountDb
import pm.gnosis.svalinn.security.db.EncryptedByteArray

@Database(entities = [AccountDb::class], version = 1)
@TypeConverters(EncryptedByteArray.Converter::class)
abstract class AccountsDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "gnosis-accounts-db"
    }

    abstract fun accountsDao(): AccountDao
}
