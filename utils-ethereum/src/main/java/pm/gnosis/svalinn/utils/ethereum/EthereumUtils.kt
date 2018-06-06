package pm.gnosis.svalinn.utils.ethereum

import org.kethereum.functions.rlp.RLPList
import org.kethereum.functions.rlp.encode
import org.kethereum.functions.rlp.toRLP
import pm.gnosis.crypto.utils.HashUtils
import pm.gnosis.model.Solidity
import java.math.BigInteger

fun getDeployAddressFromNonce(sender: Solidity.Address, nonce: BigInteger) =
    HashUtils.sha3lower20(RLPList(arrayListOf(sender.value.toRLP(), nonce.toRLP())).encode()).let {
        Solidity.Address(BigInteger(1, it))
    }
