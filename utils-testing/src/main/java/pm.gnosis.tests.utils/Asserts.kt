package pm.gnosis.tests.utils

import org.junit.Assert

object Asserts {
    inline fun assertThrow(test: () -> Unit, message: String? = null, noinline throwablePredicate: ((Throwable) -> Boolean)? = null) {
        var success = false
        try {
            test()
        } catch (t: Throwable) {
            success = throwablePredicate?.invoke(t) ?: true
        }
        Assert.assertTrue(message ?: "Should have thrown", success)
    }
}
