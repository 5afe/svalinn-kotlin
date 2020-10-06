package pm.gnosis.svalinn.security.impls

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.security.*
import pm.gnosis.tests.utils.Asserts.assertThrow
import pm.gnosis.tests.utils.MainCoroutineScopeRule
import pm.gnosis.tests.utils.MockUtils
import pm.gnosis.tests.utils.TestLifecycleRule
import pm.gnosis.tests.utils.TestPreferences
import pm.gnosis.utils.toHexString
import pm.gnosis.utils.utf8String
import java.security.AlgorithmParameters
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import kotlin.coroutines.Continuation

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
    private lateinit var fingerprintHelperMock: FingerprintHelper

    @Mock
    private lateinit var keyStorage: KeyStorage

    private val preferences = TestPreferences()

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var manager: AesEncryptionManager

    @Before
    fun setup() {
        given(application.getSharedPreferences(anyString(), anyInt())).willReturn(preferences)
        preferencesManager = PreferencesManager(application)
        manager = AesEncryptionManager(application, preferencesManager, fingerprintHelperMock, keyStorage)
    }

    @Test
    fun initialized() {
        preferences.remove(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY)
        assertFalse(manager.initialized())

        preferences.putString(PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY, "TEST")
        assertTrue(manager.initialized())
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

    @Test
    fun fingerprintFlow() = runBlockingTest {
        given(keyStorage.store(MockUtils.any())).willAnswer { it.arguments.first() }
        val cipherMock = mock(Cipher::class.java)
        val algorithmParametersMock = mock(AlgorithmParameters::class.java)
        val ivParameterSpecMock = mock(IvParameterSpec::class.java)
        val authenticationResult = AuthenticationResultSuccess(cipherMock)

        given(fingerprintHelperMock.authenticate()).willReturn(authenticationResult)
        given(cipherMock.parameters).willReturn(algorithmParametersMock)
        given(algorithmParametersMock.getParameterSpec(IvParameterSpec::class.java)).willReturn(ivParameterSpecMock)
        given(ivParameterSpecMock.iv).willReturn(byteArrayOf(0x0))
        given(fingerprintHelperMock.authenticate(MockUtils.any())).willReturn(authenticationResult)

        // Setup password to generate app key
        val passwordInput = byteArrayOf(0x0)
        manager.setupPassword(passwordInput)

        // Encrypt data
        val data = "Merry Christmas".toByteArray()
        val encryptedData = manager.encrypt(data)

        // Setup encryption mock for fingerprint setup
        val encryptDoFinalAnswer = CachedAnswer<ByteArray, ByteArray>("ENCRYPTED_KEY".toByteArray())
        given(cipherMock.doFinal(MockUtils.any())).will(encryptDoFinalAnswer)

        // Setup fingerprint
        assertTrue(manager.setupFingerprint())
        assertNotNull("App key should not be null", encryptDoFinalAnswer.input)
        then(fingerprintHelperMock).should().authenticate()

        // Check that correct data is stored
        val cryptoDataString = preferences.getString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, null)
        val cryptoDataExpected = EncryptionManager.CryptoData(encryptDoFinalAnswer.output, ivParameterSpecMock.iv)
        assertEquals(cryptoDataExpected.toString(), cryptoDataString)

        // Lock device
        manager.lock()

        // Setup encryption mock for fingerprint unlock
        val decryptDoFinalAnswer = CachedAnswer<ByteArray, ByteArray>(encryptDoFinalAnswer.input!!)
        given(cipherMock.doFinal(MockUtils.any())).will(decryptDoFinalAnswer)

        // Unlock with fingerprint
        assertTrue(manager.unlockWithFingerprint() is FingerprintUnlockSuccessful)
        assertEquals(
            "Encrypted app key should the same that has be returned on setup",
            encryptDoFinalAnswer.output.toHexString(),
            decryptDoFinalAnswer.input?.toHexString()
        )
        then(fingerprintHelperMock).should().authenticate(ivParameterSpecMock.iv)

        // Check that decrypted data is correct
        val decryptedData = manager.decrypt(encryptedData)
        assertEquals("Data was not correctly decryted", data.utf8String(), decryptedData.utf8String())

        // Check that the fingerprint can be removed
        manager.clearFingerprintData()
        then(fingerprintHelperMock).should().removeKey()
        assertNull("Fingerprint encrypted app key should have been removed.", preferences.getString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, null))


        // Check that we don't make unnecessary call to the fingerprint manager
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun clearFingerprintData() {
        preferences.putString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, "testKey")

        manager.clearFingerprintData()

        then(fingerprintHelperMock).should().removeKey()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
        assertNull(preferences.getString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, null))
    }

    @Test
    fun clearFingerprintDataError() {
        val exception = RuntimeException()
        given(fingerprintHelperMock.removeKey()).willThrow(exception)
        preferences.putString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, "testKey")

        assertThrow({
            manager.clearFingerprintData()
        }, throwablePredicate = { it == exception })

        then(fingerprintHelperMock).should().removeKey()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
        assertNull(preferences.getString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, null))
    }

    @Test
    fun isFingerprintSet() {
        preferences.putString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, "testKey")
        given(fingerprintHelperMock.isKeySet()).willReturn(true)

        assertTrue(manager.isFingerPrintSet())

        then(fingerprintHelperMock).should().isKeySet()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun isFingerprintNotSet() {
        preferences.putString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, "testKey")
        given(fingerprintHelperMock.isKeySet()).willReturn(false)

        assertFalse(manager.isFingerPrintSet())

        then(fingerprintHelperMock).should().isKeySet()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun isFingerprintSetError() {
        val exception = RuntimeException()
        preferences.putString(PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY, "testKey")
        given(fingerprintHelperMock.isKeySet()).willThrow(exception)

        assertFalse(manager.isFingerPrintSet())

        then(fingerprintHelperMock).should().isKeySet()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun canSetupFingerprint() {
        given(fingerprintHelperMock.systemHasFingerprintsEnrolled()).willReturn(true)

        val result = manager.canSetupFingerprint()

        then(fingerprintHelperMock).should().systemHasFingerprintsEnrolled()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
        assertTrue(result)
    }

    @Test
    fun cannotSetupFingerprint() {
        given(fingerprintHelperMock.systemHasFingerprintsEnrolled()).willReturn(false)

        val result = manager.canSetupFingerprint()

        then(fingerprintHelperMock).should().systemHasFingerprintsEnrolled()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
        assertFalse(result)
    }

    @Test
    fun testObserveFingerprintForSetupPasswordNotSet() = runBlocking {
        val cipherMock = mock(Cipher::class.java)
        val authenticationResult = AuthenticationResultSuccess(cipherMock)
        given(fingerprintHelperMock.authenticate()).willReturn(authenticationResult)

        assertFalse(manager.setupFingerprint())

        then(fingerprintHelperMock).should().authenticate()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun testObserveFingerprintForSetupFingerprintError() = runBlocking {
        val exception = RuntimeException()
        given(fingerprintHelperMock.authenticate()).willThrow(exception)
        assertThrow({
            manager.setupFingerprint()
        }, throwablePredicate = { it == exception})

        then(fingerprintHelperMock).should().authenticate()
        then(fingerprintHelperMock).shouldHaveNoMoreInteractions()
    }

    private class CachedAnswer<I, O>(val output: O) : Answer<O> {
        var input: I? = null

        override fun answer(invocation: InvocationOnMock): O {
            // It should only be set once (the mock call will invoke it too .... )
            input = input ?: invocation.getArgument<I>(0)
            return output
        }
    }

    companion object {
        private const val PREF_KEY_PASSWORD_ENCRYPTED_APP_KEY = "encryption_manager.string.password_encrypted_app_key"
        private const val PREF_KEY_PASSWORD_CHECKSUM = "encryption_manager.string.password_checksum"
        private const val PREF_KEY_FINGERPRINT_ENCRYPTED_APP_KEY = "encryption_manager.string.fingerprint_encrypted_app_key"
    }
}
