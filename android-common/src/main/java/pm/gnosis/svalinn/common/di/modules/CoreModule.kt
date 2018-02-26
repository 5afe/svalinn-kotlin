package pm.gnosis.svalinn.common.di.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import pm.gnosis.svalinn.common.di.ApplicationContext
import javax.inject.Singleton

@Module
class CoreModule(val application: Application) {

    @Provides
    @Singleton
    @ApplicationContext
    fun providesContext(): Context = application

    @Provides
    @Singleton
    fun providesApplication(): Application = application
}