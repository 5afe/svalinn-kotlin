package pm.gnosis.svalinn.accounts.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.ethereum.geth.Geth
import org.ethereum.geth.KeyStore
import pm.gnosis.svalinn.common.di.ApplicationContext
import javax.inject.Singleton

@Module
class AccountsModule {
    @Provides
    @Singleton
    fun providesGethKeyStore(@ApplicationContext context: Context) = KeyStore("${context.filesDir}/keystore", Geth.LightScryptN, Geth.LightScryptP)
}