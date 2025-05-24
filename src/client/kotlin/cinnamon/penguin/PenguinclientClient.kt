package cinnamon.penguin

import net.fabricmc.api.ClientModInitializer
import cinnamon.penguin.input.KeyboardHandler
import cinnamon.penguin.module.ModuleManager
// Removed: import cinnamon.penguin.modules.esp.CustomEspModule 
// Removed: import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents 
// import net.minecraft.client.MinecraftClient // Not strictly needed for this change but good practice

object PenguinclientClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("PenguinClient: Initializing...")
        
        try {
            // Set headless mode to false for Swing GUI
            System.setProperty("java.awt.headless", "false")
            
            // Initialize the module manager
            ModuleManager.init() // This now handles ESP module initialization and event registration
            println("PenguinClient: ModuleManager initialized")

            // Removed: CustomEspModule.initialize()
            // Removed: println("PenguinClient: CustomEspModule initialized")
            
            // Register keyboard handler for opening the GUI with F7
            KeyboardHandler.register()
            println("PenguinClient: KeyboardHandler registered successfully")

            // Removed: WorldRenderEvents.AFTER_ENTITIES.register { ... } block
            // Removed: println("PenguinClient: ESP rendering registered")
            
            println("PenguinClient: Initialization complete")
        } catch (e: Exception) {
            println("PenguinClient: Error during initialization")
            e.printStackTrace()
        }
    }
}
