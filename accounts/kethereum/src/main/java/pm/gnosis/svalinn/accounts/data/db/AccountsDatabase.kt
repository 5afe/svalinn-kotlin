package pm.gnosis.svalinn.accounts.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
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
