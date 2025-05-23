package cinnamon.penguinclient.module

/**
 * Base class for modules.
 */
abstract class Module(
    val name: String,
    val description: String
) {
    var isEnabled: Boolean = false
        protected set // Allow internal toggling but read-only from outside

    /**
     * Called when the module is enabled.
     * Implementations should override this to add their specific logic.
     */
    open fun onEnable() {
        // Default implementation does nothing
    }

    /**
     * Called when the module is disabled.
     * Implementations should override this to add their specific logic.
     */
    open fun onDisable() {
        // Default implementation does nothing
    }

    /**
     * Toggles the module's state (enabled/disabled).
     * Calls onEnable() or onDisable() accordingly.
     */
    fun toggle() {
        isEnabled = !isEnabled
        if (isEnabled) {
            onEnable()
        } else {
            onDisable()
        }
    }
}
