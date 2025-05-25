package cinnamon.penguin.module

import cinnamon.penguin.config.ConfigManager // Added import for ConfigManager
import cinnamon.penguin.module.modules.combat.AutoClickerModule
import cinnamon.penguin.module.modules.render.CustomEspModule // Added import
import cinnamon.penguin.module.modules.render.BlockEspModule // Added import for BlockEspModule
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
// Removed Gson, TypeToken, File, FileReader, FileWriter imports

/**
 * Manages all modules in the client
 */
object ModuleManager {
    private val modules = mutableListOf<Module>()
    // Removed gson and configFile

    /**
     * Initialize the module manager
     */
    fun init() {
        println("ModuleManager: Initializing...")

        // Register modules first
        registerModule(AutoClickerModule())
        registerModule(CustomEspModule()) // Added registration
        registerModule(BlockEspModule())   // New registration for BlockESP
        // Add more modules here as they're created

        // Load configuration using ConfigManager
        ConfigManager.loadConfig(this) // MODIFIED LINE
        
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // Check for key presses to toggle modules
            modules.forEach { module ->
                if (module.wasKeybindPressed()) {
                    module.toggle() // This will also trigger onEnable/onDisable which can be saved
                    ConfigManager.saveConfig(this) // MODIFIED LINE
                    println("ModuleManager: Toggled ${module.name} (${if (module.enabled) "enabled" else "disabled"}) by keybind")
                }
            }
            
            // Call onTick for enabled modules
            modules.filter { it.enabled }.forEach { it.onTick() }
        }

        // Register shutdown event to save configurations
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigManager.saveConfig(this) // MODIFIED LINE
        }
        
        println("ModuleManager: Initialized ${modules.size} modules")
    }
    
    /**
     * Register a module
     */
    private fun registerModule(module: Module) {
        modules.add(module)
        println("ModuleManager: Registered module ${module.name}")
    }
    
    /**
     * Get all modules
     */
    fun getModules(): List<Module> = modules.toList()
    
    /**
     * Get modules by category
     */
    fun getModulesByCategory(category: Category): List<Module> = 
        modules.filter { it.category == category }
    
    /**
     * Get a module by name
     */
    fun getModule(name: String): Module? =
        modules.find { it.name.equals(name, ignoreCase = true) }

    // REMOVED saveModuleConfiguration method
    // REMOVED loadModuleConfiguration method
    // REMOVED ModuleConfig data class
}
