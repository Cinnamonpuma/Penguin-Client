package cinnamon.penguin

import net.fabricmc.api.ClientModInitializer
import cinnamon.penguin.input.KeyboardHandler
import cinnamon.penguin.module.ModuleManager
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

            // Register keyboard handler for opening the GUI with F7
            KeyboardHandler.register()
            println("PenguinClient: KeyboardHandler registered successfully")

            println("PenguinClient: Initialization complete")
        } catch (e: Exception) {
            println("PenguinClient: Error during initialization")
            e.printStackTrace()
        }
    }
}
