package pm.gnosis.svalinn.accounts.repositories.impls

import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import pm.gnosis.crypto.ECDSASignature
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.svalinn.accounts.base.models.Signature
import pm.gnosis.svalinn.accounts.data.db.AccountDao
import pm.gnosis.svalinn.accounts.data.db.AccountsDatabase
import pm.gnosis.svalinn.accounts.repositories.impls.models.db.AccountDb
import pm.gnosis.svalinn.accounts.utils.hash
import pm.gnosis.svalinn.accounts.utils.rlp
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.security.EncryptionManager
import pm.gnosis.svalinn.security.db.EncryptedByteArray
import pm.gnosis.tests.utils.ImmediateSchedulersRule
import pm.gnosis.tests.utils.MockUtils
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.hexStringToByteArray
import pm.gnosis.utils.toHexString
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class KethereumAccountsRepositoryTest {
    @JvmField
    @Rule
    val rule = ImmediateSchedulersRule()

    @Mock
    private lateinit var accountsDatabase: AccountsDatabase

    @Mock
    private lateinit var encryptionManager: EncryptionManager

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var accountsDao: AccountDao

    private lateinit var repository: KethereumAccountsRepository

    @Before
    fun setup() {
        accountsDao = mock(AccountDao::class.java)
        given(accountsDatabase.accountsDao()).willReturn(accountsDao)
        given(encryptionManager.encrypt(MockUtils.any())).willAnswer {
            EncryptionManager.CryptoData(
                it.arguments.first() as ByteArray,
                "iv".toByteArray()
            )
        }
        given(encryptionManager.decrypt(MockUtils.any())).willAnswer { (it.arguments.first() as EncryptionManager.CryptoData).data }
        repository = KethereumAccountsRepository(accountsDatabase, encryptionManager, preferencesManager)
    }

    @Test
    fun signTransactionEIP155Example() {
        /* Node.js code:
        const Tx = require('ethereumjs-tx')
        var tra = {nonce: 9, gasPrice: web3.toHex(20000000000), gasLimit: web3.toHex(21000), data: '', to: '0x3535353535353535353535353535353535353535', chainId: 1, value: 1000000000000000000};
        var tx = new Tx(tra)
        var key = new Buffer('4646464646464646464646464646464646464646464646464646464646464646', 'hex')
        tx.sign(key)
        tx.serialize().toString('hex')
         */
        val privateKey = "0x4646464646464646464646464646464646464646464646464646464646464646"
        val encryptedKey = EncryptedByteArray.create(encryptionManager, privateKey.hexStringToByteArray())
        // TODO: maybe replace with the real address
        val account = AccountDb(encryptedKey, Solidity.Address(BigInteger.TEN))
        given(accountsDao.observeAccounts()).willReturn(Single.just(account))
        val testObserver = TestObserver<String>()
        val address = "0x3535353535353535353535353535353535353535".asEthereumAddress()!!
        val nonce = BigInteger("9")
        val value = Wei(BigInteger("1000000000000000000"))
        val gas = BigInteger("21000")
        val gasPrice = BigInteger("20000000000")
        val transaction = Transaction(address = address, nonce = nonce, value = value, gas = gas, gasPrice = gasPrice, chainId = BigInteger.ONE)

        repository.signTransaction(transaction).subscribe(testObserver)

        val expectedTx =
            "0xf86c098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a76400008025a028ef61340bd939bc2195fe537567866003e1a15d3c71ff63e1590620aa636276a067cbe9d8997f761aecb703304b3800ccf555c9f3dc64214b297fb1966a3b6d83"
        testObserver.assertResult(expectedTx)
    }

    @Test
    fun rlpTransactionExtension() {
        val address = "0x19fd8863ea1185d8ef7ab3f2a8f4d469dc35dd52".asEthereumAddress()!!
        val nonce = BigInteger("13")
        val gas = BigInteger("2034776")
        val gasPrice = BigInteger("20000000000")
        val transaction =
            Transaction(address = address, nonce = nonce, data = "0xe411526d", gas = gas, gasPrice = gasPrice, chainId = BigInteger.valueOf(28))
        val expectedString = "e90d8504a817c800831f0c589419fd8863ea1185d8ef7ab3f2a8f4d469dc35dd528084e411526d1c8080"
        assertEquals(expectedString, transaction.rlp().toHexString())
    }

    @Test
    fun transactionHashWithSignature() {
        val tx = Transaction(
            address = Solidity.Address(BigInteger.ZERO),
            gas = 411632.toBigInteger(),
            gasPrice = 4000000000.toBigInteger(),
            data = "0x608060405234801561001057600080fd5b50604051610502380380610502833981018060405281019080805190602001909291908051820192919060200180519060200190929190805190602001909291908051906020019092919050505084848160008173ffffffffffffffffffffffffffffffffffffffff1614151515610116576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260248152602001807f496e76616c6964206d617374657220636f707920616464726573732070726f7681526020017f696465640000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550506000815111156101a35773ffffffffffffffffffffffffffffffffffffffff60005416600080835160208501846127105a03f46040513d6000823e600082141561019f573d81fd5b5050505b5050600081111561038357600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16141561022f578273ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f19350505050158015610229573d6000803e3d6000fd5b50610382565b8173ffffffffffffffffffffffffffffffffffffffff1663a9059cbb84836040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050602060405180830381600087803b1580156102d257600080fd5b505af11580156102e6573d6000803e3d6000fd5b505050506040513d60208110156102fc57600080fd5b81019080805190602001909291905050501515610381576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601f8152602001807f436f756c64206e6f74206578656375746520746f6b656e207061796d656e740081525060200191505060405180910390fd5b5b5b505050505061016b806103976000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634555d5c91461008b5780635c60da1b146100b6575b73ffffffffffffffffffffffffffffffffffffffff600054163660008037600080366000845af43d6000803e6000811415610086573d6000fd5b3d6000f35b34801561009757600080fd5b506100a061010d565b6040518082815260200191505060405180910390f35b3480156100c257600080fd5b506100cb610116565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b60006002905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050905600a165627a7a723058207b6793b265137e04ae615face5a75e9297b0f75d2194a1fd45dde37e08af9e730029000000000000000000000000ec7c75c1548765ab51a165873b0b1b71663c126600000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000ab8c18e66135561676f0781555d05cf6b22024a300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005d9822c8d80000000000000000000000000000000000000000000000000000000000000000164a04222e1000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000012000000000000000000000000000000000000000000000000000000000000000040000000000000000000000007b35526cab9f5599de410160a0e51533cfafc33e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000d84ab641422351a85914657cdbd9ec28aaa286af00000000000000000000000048e3b0ff14c8913fd515578b7b7499bf2443dbb80000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
            nonce = BigInteger.ZERO
        )
        val hash = tx.hash(
            ECDSASignature(
                r = BigInteger("18791006335898280803252024165139053571562361633090590726732379820185947067"),
                s = BigInteger("2647276639964841513316066866950659665933281879584930681484385164847771956325")
            ).apply { v = 28.toByte() }
        ).toHexString()
        assertEquals("34aa96388d39b09285391e49c4ad03bbffc535cd94c27f2fa287fe68f83f584e", hash)
    }

    @Test
    fun signTransactionCreateSafe() {
        /* Node.js code:
        const Tx = require('ethereumjs-tx')
        var tra = {nonce: 13, gasPrice: web3.toHex(20000000000), gasLimit: web3.toHex(2034776), data: "0xe411526d000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001000000000000000000000000a5056c8efadb5d6a1a6eb0176615692b6e648313", to: '0x19fd8863ea1185d8ef7ab3f2a8f4d469dc35dd52', chainId: 0};
        var tx = new Tx(tra)
        var key = new Buffer('8678adf78db8d1c8a40028795077b3463ca06a743ca37dfd28a5b4442c27b457', 'hex')
        tx.sign(key)
        tx.serialize().toString('hex')
         */
        val privateKey = "0x8678adf78db8d1c8a40028795077b3463ca06a743ca37dfd28a5b4442c27b457"
        val encryptedKey = EncryptedByteArray.create(encryptionManager, privateKey.hexStringToByteArray())
        val account = AccountDb(encryptedKey, Solidity.Address(BigInteger.TEN))
        given(accountsDao.observeAccounts()).willReturn(Single.just(account))
        val testObserver = TestObserver<String>()
        val address = "0x19fd8863ea1185d8ef7ab3f2a8f4d469dc35dd52".asEthereumAddress()!!
        val nonce = BigInteger("13")
        val gas = BigInteger("2034776")
        val gasPrice = BigInteger("20000000000")
        val transaction = Transaction(address = address, nonce = nonce, data = CREATE_SAFE_DATA, gas = gas, gasPrice = gasPrice, chainId = BigInteger.ZERO)

        repository.signTransaction(transaction).subscribe(testObserver)

        val expectedTx =
            "0xf8ea0d8504a817c800831f0c589419fd8863ea1185d8ef7ab3f2a8f4d469dc35dd5280b884e411526d000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001000000000000000000000000a5056c8efadb5d6a1a6eb0176615692b6e6483131ba067f34dee111e9ab4a034eccb458403b1ee5b80ac801f161d8151f75faa5fbb71a05c88f85eaa2bd540c6d7778787016c27ac92e706dbb19e07c61433d61bf6925f"
        testObserver.assertResult(expectedTx)
    }

    @Test
    fun signAndRecover() {
        val privateKey = "0x8678adf78db8d1c8a40028795077b3463ca06a743ca37dfd28a5b4442c27b457"
        val encryptedKey = EncryptedByteArray.create(encryptionManager, privateKey.hexStringToByteArray())
        val account = AccountDb(encryptedKey, Solidity.Address(BigInteger.TEN))
        given(accountsDao.observeAccounts()).willReturn(Single.just(account))
        val testObserver = TestObserver<Signature>()

        val data = "9b8bc77908c0b0ebe93e897e43f594b811f5d7130d86a5708403ddb417dc111b".hexStringToByteArray()
        repository.sign(data).subscribe(testObserver)

        val expectedSignature = Signature(
            "6c65af8fabdf55b026300ccb4cf1c19f27592a81c78aba86abe83409563d9c13".hexAsBigInteger(),
            "256a9a9e87604e89f083983f7449f58a456ac7929265f7114d585538fe226e1f".hexAsBigInteger(), 27
        )
        testObserver.assertResult(expectedSignature)

        val recoverObserver = TestObserver<Solidity.Address>()
        repository.recover(data, expectedSignature).subscribe(recoverObserver)
        recoverObserver.assertResult("a5056c8efadb5d6a1a6eb0176615692b6e648313".asEthereumAddress())
    }

    @Test
    fun recoverEIP155() {
        // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#example
        val expectedData = "0xdaf5a779ae972f972197303d7b574746c7ef83eadac0f2791ad23db92e4c8e53".hexStringToByteArray()
        val testObserver = TestObserver.create<Solidity.Address>()

        repository.recover(
            expectedData,
            Signature.fromChainId(
                "18515461264373351373200002665853028612451056578545711640558177340181847433846".toBigInteger(),
                "46948507304638947509940763649030358759909902576025900602547168820602576006531".toBigInteger(),
                37.toByte(), chainId = 1
            )
        ).subscribe(testObserver)
        testObserver.assertResult("0x9d8a62f656a8d1615c1294fd71e9cfb3e4855a4f".asEthereumAddress())
    }

    companion object {

        const val CREATE_SAFE_DATA = "0x" +
                "e411526d" +
                "0000000000000000000000000000000000000000000000000000000000000040" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "000000000000000000000000a5056c8efadb5d6a1a6eb0176615692b6e648313"
    }
}
