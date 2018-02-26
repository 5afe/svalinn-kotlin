package pm.gnosis.svalinn.security.di

import dagger.Binds
import dagger.Module
import pm.gnosis.svalinn.security.EncryptionManager
import pm.gnosis.svalinn.security.FingerprintHelper
import pm.gnosis.svalinn.security.impls.AesEncryptionManager
import pm.gnosis.svalinn.security.impls.DefaultFingerprintHelper
import javax.inject.Singleton

@Module
abstract class SecurityBindingsModule {
    @Binds
    @Singleton
    abstract fun bindsEncryptionManager(manager: AesEncryptionManager): EncryptionManager

    @Binds
    @Singleton
    abstract fun bindsFingerprintHelper(fingerprintHelper: DefaultFingerprintHelper): FingerprintHelper
}
