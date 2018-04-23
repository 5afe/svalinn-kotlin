package pm.gnosis.svalinn.common

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    companion object {
        const val GNOSIS_PREFS_NAME = "GnosisPrefs"
        const val FINISHED_TOKENS_SETUP = "prefs.boolean.finished_tokens_setup"
        const val PASSPHRASE_KEY = "prefs.string.passphrase"
        const val CURRENT_ACCOUNT_ADDRESS_KEY = "prefs.string.current_account"
        const val MNEMONIC_KEY = "prefs.string.mnemonic"
        const val DISMISS_LOW_BALANCE = "prefs.boolean.dismiss_low_balance"
    }

    val prefs: SharedPreferences = context.getSharedPreferences(GNOSIS_PREFS_NAME, Context.MODE_PRIVATE)
}
