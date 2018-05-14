package pm.gnosis.svalinn.accounts.base.repositories

import io.reactivex.Completable
import io.reactivex.Single
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.svalinn.accounts.base.models.Account
import pm.gnosis.svalinn.accounts.base.models.Signature

interface AccountsRepository {
    fun loadActiveAccount(): Single<Account>

    fun signTransaction(transaction: Transaction): Single<String>

    fun sign(data: ByteArray): Single<Signature>

    fun recover(data: ByteArray, signature: Signature): Single<Solidity.Address>

    fun accountFromMnemonicSeed(mnemonicSeed: ByteArray, accountIndex: Long = 0): Single<Pair<Solidity.Address, ByteArray>>

    fun saveAccountFromMnemonicSeed(mnemonicSeed: ByteArray, accountIndex: Long = 0): Completable

    fun saveMnemonic(mnemonic: String): Completable

    fun loadMnemonic(): Single<String>
}
