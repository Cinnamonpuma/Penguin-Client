package cinnamon.penguin

import net.fabricmc.api.ClientModInitializer
import cinnamon.penguin.input.KeyboardHandler
import cinnamon.penguin.module.ModuleManager
import cinnamon.penguin.modules.esp.CustomEspModule
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
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

            // Initialize ESP Module
            CustomEspModule.initialize()
            println("PenguinClient: CustomEspModule initialized")
            
            // Register keyboard handler for opening the GUI with F7
            KeyboardHandler.register()
            println("PenguinClient: KeyboardHandler registered successfully")

            // Register ESP rendering
            WorldRenderEvents.AFTER_ENTITIES.register { context ->
                CustomEspModule.updateTargetedEntities() 
                CustomEspModule.renderEspHighlights(context.matrixStack(), context.consumers(), context.camera().pos)
            }
            println("PenguinClient: ESP rendering registered")
            
            println("PenguinClient: Initialization complete")
        } catch (e: Exception) {
            println("PenguinClient: Error during initialization")
            e.printStackTrace()
        }
    }
}
