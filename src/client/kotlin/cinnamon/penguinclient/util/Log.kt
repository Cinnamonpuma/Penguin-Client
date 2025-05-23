package cinnamon.penguinclient.util

/**
 * Simple logging utility.
 * Prepends log messages with a client name and log level.
 */
object Log {
    private const val CLIENT_PREFIX = "[PenguinClient]"

    fun info(message: String) {
        println("$CLIENT_PREFIX [INFO] $message")
    }

    fun warn(message: String) {
        println("$CLIENT_PREFIX [WARN] $message")
    }

    fun error(message: String) {
        System.err.println("$CLIENT_PREFIX [ERROR] $message") // Use System.err for errors
    }

    fun error(message: String, throwable: Throwable) {
        System.err.println("$CLIENT_PREFIX [ERROR] $message")
        throwable.printStackTrace(System.err)
    }

    // Optional: Debug level, can be controlled by a config later
    private var debugEnabled = false // Potentially load from a config file

    fun setDebugEnabled(enabled: Boolean) {
        debugEnabled = enabled
    }

    fun debug(message: String) {
        if (debugEnabled) {
            println("$CLIENT_PREFIX [DEBUG] $message")
        }
    }
}
