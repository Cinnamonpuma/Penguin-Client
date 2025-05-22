package cinnamon.penguin.module

import cinnamon.penguin.module.modules.combat.AutoClickerModule
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

/**
 * Manages all modules in the client
 */
object ModuleManager {
    private val modules = mutableListOf<Module>()
    
    /**
     * Initialize the module manager
     */
    fun init() {
        println("ModuleManager: Initializing...")
        
        // Register modules
        registerModule(AutoClickerModule())
        // Add more modules here as they're created
        
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // Check for key presses to toggle modules
            modules.forEach { module ->
                if (module.wasKeybindPressed()) {
                    module.toggle()
                    println("ModuleManager: Toggled ${module.name} (${if (module.enabled) "enabled" else "disabled"})")
                }
            }
            
            // Call onTick for enabled modules
            modules.filter { it.enabled }.forEach { it.onTick() }
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
}
