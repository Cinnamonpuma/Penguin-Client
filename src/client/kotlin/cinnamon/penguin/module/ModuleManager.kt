package cinnamon.penguin.module

import cinnamon.penguin.module.modules.combat.AutoClickerModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import java.io.File
import java.io.FileReader
import java.io.FileWriter

// Data class for storing module configuration
data class ModuleConfig(val name: String, var enabled: Boolean, var keyCode: Int)

/**
 * Manages all modules in the client
 */
object ModuleManager {
    private val modules = mutableListOf<Module>()
    private val gson = Gson()
    private val configFile = File(MinecraftClient.getInstance().runDirectory, "config/penguinclient/modules.json")

    /**
     * Initialize the module manager
     */
    fun init() {
        println("ModuleManager: Initializing...")

        // Ensure config directory exists
        configFile.parentFile.mkdirs()

        // Register modules first
        registerModule(AutoClickerModule())
        // Add more modules here as they're created

        // Load configuration after modules are registered
        loadModuleConfiguration()
        
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // Check for key presses to toggle modules
            modules.forEach { module ->
                if (module.wasKeybindPressed()) {
                    module.toggle() // This will also trigger onEnable/onDisable which can be saved
                    saveModuleConfiguration() // Save changes immediately after toggle via keybind
                    println("ModuleManager: Toggled ${module.name} (${if (module.enabled) "enabled" else "disabled"}) by keybind")
                }
            }
            
            // Call onTick for enabled modules
            modules.filter { it.enabled }.forEach { it.onTick() }
        }

        // Register shutdown event to save configurations
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            saveModuleConfiguration()
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

    /**
     * Save module configurations to a file
     */
    fun saveModuleConfiguration() {
        try {
            FileWriter(configFile).use { writer ->
                val moduleConfigs = modules.map { ModuleConfig(it.name, it.enabled, it.keyCode) }
                gson.toJson(moduleConfigs, writer)
                println("ModuleManager: Saved module configurations to ${configFile.absolutePath}")
            }
        } catch (e: Exception) {
            println("ModuleManager: Error saving module configurations: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load module configurations from a file
     */
    private fun loadModuleConfiguration() {
        if (!configFile.exists()) {
            println("ModuleManager: No configuration file found at ${configFile.absolutePath}. Using default settings.")
            // Save default config after first load attempt if file doesn't exist
            saveModuleConfiguration()
            return
        }
        try {
            FileReader(configFile).use { reader ->
                val type = object : TypeToken<List<ModuleConfig>>() {}.type
                val loadedConfigs: List<ModuleConfig> = gson.fromJson(reader, type)
                
                loadedConfigs.forEach { config ->
                    getModule(config.name)?.let { module ->
                        if (module.enabled != config.enabled) { // Only toggle if state is different
                           if(config.enabled) module.enable() else module.disable()
                        }
                        if (module.keyCode != config.keyCode) {
                            module.setKey(config.keyCode) // This will re-register the keybinding
                        }
                    }
                }
                println("ModuleManager: Loaded module configurations from ${configFile.absolutePath}")
            }
        } catch (e: Exception) {
            println("ModuleManager: Error loading module configurations: ${e.message}")
            e.printStackTrace()
            // If loading fails, it might be good to save a backup or default config
            saveModuleConfiguration() // Save current (default or partially loaded) state
        }
    }
}
