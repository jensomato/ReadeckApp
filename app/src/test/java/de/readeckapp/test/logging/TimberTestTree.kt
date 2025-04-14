package de.readeckapp.test.logging

import timber.log.Timber

object TestTimberTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logMessage = "[${priority.levelName()}] ${tag ?: "UNTAGGED"}: $message"

        when (priority) {
            LogPriority.ASSERT -> {
                System.err.println(logMessage)
                t?.printStackTrace(System.err) // Print stack trace to STDERR
            }
            LogPriority.ERROR -> {
                System.err.println(logMessage)
                t?.printStackTrace(System.err) // Print stack trace to STDERR
            }
            LogPriority.WARN -> System.err.println(logMessage)
            LogPriority.INFO -> println(logMessage)
            LogPriority.DEBUG -> println(logMessage)
            LogPriority.VERBOSE -> println(logMessage)
            else -> println(logMessage)
        }

        if (t != null && priority >= LogPriority.WARN) {
            t.printStackTrace()
        }
    }

    // Extension function to get log level name.
    private fun Int.levelName(): String {
        return when (this) {
            2 -> "VERBOSE"
            3 -> "DEBUG"
            4 -> "INFO"
            5 -> "WARN"
            6 -> "ERROR"
            7 -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    object LogPriority {
        const val VERBOSE = 2
        const val DEBUG = 3
        const val INFO = 4
        const val WARN = 5
        const val ERROR = 6
        const val ASSERT = 7
    }
}

// Helper function to replace the DebugTree
fun replaceDebugTree() {
    // Remove any existing TestTimberTree (in case of previous failures)

    val existingDebugTrees = Timber.forest().filterIsInstance<Timber.DebugTree>()
    existingDebugTrees.forEach { Timber.uproot(it) }
    Timber.plant(TestTimberTree)
}

// Helper function to restore the DebugTree (original impl)
fun restoreDebugTree() {
    Timber.uproot(TestTimberTree) // Remove TestTimberTree
    // Plant the original DebugTree.  This assumes that the DebugTree is already planted at app startup.
    // If it's not, you'll need to either plant a new DebugTree here or add code to remember and restore the original Trees.

    //Plant the production debug tree
    Timber.plant(Timber.DebugTree())
}