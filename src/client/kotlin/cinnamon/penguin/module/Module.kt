package cinnamon.penguin.module

import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW
import cinnamon.penguin.config.ConfigManager // Added import
import cinnamon.penguin.module.ModuleManager // Added import

/**
 * Base class for all modules in PenguinClient
 */
abstract class Module(
    val name: String,
    val description: String,
    val category: Category,
    var keyCode: Int = GLFW.GLFW_KEY_UNKNOWN
) {
    var enabled = false
        private set
    
    private var keyBinding: KeyBinding? = null
    
    init {
        // Register key binding if a key is specified
        if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
            registerKeyBinding()
        }
    }
    
    /**
     * Toggle the module on/off
     */
    fun toggle() {
        enabled = !enabled
        if (enabled) {
            onEnable()
        } else {
            onDisable()
        }
        // Save config after state change
        ConfigManager.saveConfig(ModuleManager)
    }
    
    /**
     * Enable the module
     */
    fun enable() {
        if (!enabled) {
            enabled = true
            onEnable()
            // ConfigManager.saveConfig(ModuleManager) // Decided against saving here to avoid double saves if called by loadConfig
        }
    }
    
    /**
     * Disable the module
     */
    fun disable() {
        if (enabled) {
            enabled = false
            onDisable()
            // ConfigManager.saveConfig(ModuleManager) // Decided against saving here to avoid double saves if called by loadConfig
        }
    }
    
    /**
     * Set the key binding for this module
     */
    fun setKey(keyCode: Int) {
        this.keyCode = keyCode
        registerKeyBinding()
        // Save config after key change
        ConfigManager.saveConfig(ModuleManager)
    }
    
    private fun registerKeyBinding() {
        keyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.penguinclient.module.$name", 
                InputUtil.Type.KEYSYM,
                this.keyCode, 
                "category.penguinclient.modules"
            )
        )
    }
    
    /**
     * Called when the module is enabled
     */
    open fun onEnable() {}
    
    /**
     * Called when the module is disabled
     */
    open fun onDisable() {}
    
    /**
     * Called every tick when the module is enabled
     */
    open fun onTick() {}
    
    /**
     * Check if the module's key binding was pressed
     */
    fun wasKeybindPressed(): Boolean {
        return keyCode != GLFW.GLFW_KEY_UNKNOWN && keyBinding?.wasPressed() ?: false
    }
    
    /**
     * Get the Minecraft client instance
     */
    protected val mc: MinecraftClient
        get() = MinecraftClient.getInstance()
}
