package pm.gnosis.svalinn.security.impls

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.security.KeyStorage
import pm.gnosis.tests.utils.MainCoroutineScopeRule
import pm.gnosis.tests.utils.MockUtils
import pm.gnosis.tests.utils.TestLifecycleRule
import pm.gnosis.tests.utils.TestPreferences

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AesEncryptionManagerTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val instantExecutorRule = TestLifecycleRule()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var keyStorage: KeyStorage

    private val preferences = TestPreferences()

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var manager: AesEncryptionManager

    @Before
    fun setup() {
        given(application.getSharedPreferences(anyString(), anyInt())).willReturn(preferences)
        preferencesManager = PreferencesManager(application)
        manager = AesEncryptionManager(application, preferencesManager, keyStorage)
    }

    @Test
    fun initialized() {
        preferences.remove(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY)
        assertFalse(manager.initialized())

        preferences.putString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, "TEST")
        assertTrue(manager.initialized())
    }

    @Test
    fun removePassword() {
        preferences.putString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, "TEST")
        preferences.putString(PREF_KEY_PASSWORD_CHECKSUM, "TEST")

        manager.removePassword()

        assertFalse(manager.initialized())
        assertEquals(null, preferences.getString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, null))
        assertEquals(null, preferences.getString(PREF_KEY_PASSWORD_CHECKSUM, null))
    }

    @Test
    fun passwordFlow() {
        given(keyStorage.store(MockUtils.any())).willAnswer { it.arguments.first() }
        preferences.remove(PREF_KEY_PASSWORD_CHECKSUM)

        // Setup with "test"
        assertTrue(manager.setupPassword("test".toByteArray()))
        then(keyStorage).should().store(MockUtils.any())

        // Check that it is unlocked
        assertTrue(manager.unlocked())

        // Check that data can be en- and decrypted
        val encryptedData = manager.encrypt("Hello World".toByteArray())
        assertEquals("Hello World", String(manager.decrypt(encryptedData)))

        // Check that password cannot be changed if one is already set
        assertFalse(manager.setupPassword("test2".toByteArray()))

        // Check that password can be changed with old password
        assertTrue(manager.setupPassword("test2".toByteArray(), "test".toByteArray()))

        // Check that device can be locked
        manager.lock()
        assertFalse(manager.unlocked())

        // Check that device cannot be unlocked with wrong password
        assertFalse(manager.unlockWithPassword(("invalid".toByteArray())))

        // Check that device can be unlocked with old password
        assertTrue(manager.unlockWithPassword(("test2".toByteArray())))

        // Check that data encrypted with old password can still be decrypted
        assertEquals("Hello World", String(manager.decrypt(encryptedData)))
    }

    companion object {
        private const val PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY = "encryption_manager.string.password_encrypted_app_key"
        private const val PREF_KEY_PASSWORD_CHECKSUM = "encryption_manager.string.password_checksum"
    }
}
