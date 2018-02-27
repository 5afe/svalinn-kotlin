package pm.gnosis.svalinn.accounts.di

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import pm.gnosis.svalinn.accounts.data.db.AccountsDatabase
import pm.gnosis.svalinn.common.di.ApplicationContext
import javax.inject.Singleton

@Module
class AccountsModule {
    @Provides
    @Singleton
    fun providesAccountsDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AccountsDatabase::class.java, AccountsDatabase.DB_NAME).build()
}
