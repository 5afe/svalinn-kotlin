package pm.gnosis.models

import pm.gnosis.model.Solidity

data class AddressBookEntry(val address: Solidity.Address, val name: String, val description: String)
