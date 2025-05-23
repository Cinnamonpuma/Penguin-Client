package cinnamon.penguinclient.hotkey

/**
 * Represents a hotkey.
 *
 * @property keyCode The GLFW key code for this hotkey. See org.lwjgl.glfw.GLFW.GLFW_KEY_* constants.
 * @property action The action to be performed when this hotkey is pressed.
 * @property category The category of the hotkey (e.g., "Module Toggles", "Client"). Defaults to "General".
 * @property id A unique identifier for this hotkey, typically moduleName + actionDescription.
 */
data class Hotkey(
    val id: String, // e.g., "FlightModule_toggle", "KillAura_enable"
    var keyCode: Int, 
    val action: () -> Unit,
    val category: String = "General",
    val name: String // User-friendly name, e.g., "Toggle Flight"
)
