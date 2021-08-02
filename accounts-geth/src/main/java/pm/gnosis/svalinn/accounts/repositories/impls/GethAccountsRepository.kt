package pm.gnosis.svalinn.accounts.repositories.impls

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okio.ByteString
import org.ethereum.geth.Address
import org.ethereum.geth.BigInt
import org.ethereum.geth.Geth
import org.ethereum.geth.KeyStore
import pm.gnosis.crypto.KeyGenerator
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.svalinn.accounts.base.exceptions.InvalidTransactionParams
import pm.gnosis.svalinn.accounts.base.models.Account
import pm.gnosis.svalinn.accounts.base.models.Signature
import pm.gnosis.svalinn.accounts.base.repositories.AccountsRepository
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.svalinn.security.EncryptionManager
import pm.gnosis.svalinn.security.db.EncryptedString
import pm.gnosis.utils.*
import java.math.BigInteger

class GethAccountsRepository(
    private val encryptionManager: EncryptionManager,
    private val gethAccountManager: GethAccountManager,
    private val gethKeyStore: KeyStore,
    private val preferencesManager: PreferencesManager
) : AccountsRepository {
    private val encryptedStringConverter = EncryptedString.Converter()

    override fun loadActiveAccount(): Single<Account> {
        return Single.fromCallable {
            gethAccountManager.getActiveAccount()
        }.map { Account(it.address.hex.asEthereumAddress()!!) }
    }

    override fun signTransaction(transaction: Transaction): Single<String> {
        return Single.fromCallable {
            val account = gethAccountManager.getActiveAccount()

            if (!transaction.signable()) {
                throw InvalidTransactionParams()
            }

            val tx = Geth.newTransaction(
                transaction.nonce!!.toLong(),
                Address(transaction.address.asEthereumAddressString()),
                BigInt(transaction.value?.toLong() ?: 0),
                BigInt(transaction.gas!!.toLong()),
                BigInt(transaction.gasPrice!!.toLong()),
                transaction.data?.hexStringToByteArray() ?: ByteArray(0)
            )

            val signed = gethKeyStore.signTxPassphrase(
                account, gethAccountManager.getAccountPassphrase(), tx, BigInt(transaction.chainId.toLong())
            )

            signed.encodeRLP().toHexString()
        }
    }

    override fun sign(data: ByteArray): Single<Signature> {
        return Single.fromCallable {
            val account = gethAccountManager.getActiveAccount()

            val signature = gethKeyStore.signHashPassphrase(account, gethAccountManager.getAccountPassphrase(), data).toHexString()

            val r = signature.substring(0, 64).hexAsBigInteger()
            val s = signature.substring(64, 128).hexAsBigInteger()
            val v = signature.substring(128, 130).hexAsBigInteger().toByte()
            Signature(r, s, v)
        }
    }

    override fun recover(data: ByteArray, signature: Signature): Single<Solidity.Address> = Single.error(UnsupportedOperationException())

    override fun accountFromMnemonicSeed(mnemonicSeed: ByteArray, accountIndex: Long): Single<Pair<Solidity.Address, ByteArray>> =
        Single.fromCallable {
            val hdNode = KeyGenerator.masterNode(ByteString.of(*mnemonicSeed))
            val key = hdNode.derive(KeyGenerator.BIP44_PATH_ETHEREUM).deriveChild(accountIndex).keyPair
            val privateKey = key.privKeyBytes ?: throw IllegalStateException("Private key must not be null")
            Solidity.Address(BigInteger(key.address)) to privateKey
        }

    override fun saveAccountFromMnemonicSeed(mnemonicSeed: ByteArray, accountIndex: Long): Completable =
        accountFromMnemonicSeed(mnemonicSeed, accountIndex)
            .map { (_, privateKey) ->
                gethKeyStore.importECDSAKey(
                    privateKey,
                    gethAccountManager.getAccountPassphrase()
                )
            }.toCompletable()

    override fun saveMnemonic(mnemonic: String): Completable = Completable.fromCallable {
        preferencesManager.prefs.edit {
            val encryptedString = EncryptedString.create(encryptionManager, mnemonic)
            putString(PreferencesManager.MNEMONIC_KEY, encryptedStringConverter.toStorage(encryptedString))
        }
    }.subscribeOn(Schedulers.computation())

    override fun loadMnemonic(): Single<String> = Single.fromCallable {
        val encryptedMnemonic = preferencesManager.prefs.getString(PreferencesManager.MNEMONIC_KEY, "")!!
        encryptedStringConverter.fromStorage(encryptedMnemonic).value(encryptionManager)
    }.subscribeOn(Schedulers.computation())
}
