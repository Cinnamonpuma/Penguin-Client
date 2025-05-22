package cinnamon.penguin.module

import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW

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
    }
    
    /**
     * Enable the module
     */
    fun enable() {
        if (!enabled) {
            enabled = true
            onEnable()
        }
    }
    
    /**
     * Disable the module
     */
    fun disable() {
        if (enabled) {
            enabled = false
            onDisable()
        }
    }
    
    /**
     * Set the key binding for this module
     */
    fun setKey(keyCode: Int) {
        this.keyCode = keyCode
        // KeyBindingHelper.registerKeyBinding handles overriding existing bindings with the same ID.
        // So, we just need to ensure a new one is registered if the key is not UNKNOWN.
        // If a keybinding was previously registered, it will be effectively replaced by the new one
        // or become inactive if the new key is UNKNOWN.
        
        // We need to ensure the keyBinding instance is updated or cleared.
        // Calling registerKeyBinding() will create a new KeyBinding instance
        // associated with the current keyCode (which might be UNKNOWN).
        // Fabric's KeyBindingHelper.registerKeyBinding handles overriding existing ones with the same ID.
        registerKeyBinding()
    }
    
    private fun registerKeyBinding() {
        // Always create a new KeyBinding instance when called.
        // If keyCode is UNKNOWN, it effectively unbinds it from an active key,
        // though the KeyBinding object itself might still exist if not managed carefully.
        // Fabric's KeyBindingHelper handles reregistration by ID.
        keyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.penguinclient.module.$name", // ID for the keybinding
                InputUtil.Type.KEYSYM,
                this.keyCode, // Use the current module's keyCode
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
        // Ensure we only check wasPressed if a key is actually bound.
        return keyCode != GLFW.GLFW_KEY_UNKNOWN && keyBinding?.wasPressed() ?: false
    }
    
    /**
     * Get the Minecraft client instance
     */
    protected val mc: MinecraftClient
        get() = MinecraftClient.getInstance()
}
