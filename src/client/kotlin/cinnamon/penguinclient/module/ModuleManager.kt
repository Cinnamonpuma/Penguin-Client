package cinnamon.penguinclient.module

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Serializable
data class ModuleConfig(val name: String, var isEnabled: Boolean)

object ModuleManager {
    private val modules = mutableListOf<Module>()
    // Store configs by module name for quick lookup
    private val moduleConfigs = mutableMapOf<String, ModuleConfig>() 
    
    private val configDirectory: Path = FabricLoader.getInstance().configDir.resolve("penguinclient")
    private val configFile: File = configDirectory.resolve("modules.json").toFile()

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        try {
            if (!Files.exists(configDirectory)) {
                Files.createDirectories(configDirectory)
            }
        } catch (e: Exception) {
            // Log this error using a proper logger once available
            println("Error creating config directory: ${configDirectory}")
            e.printStackTrace()
        }
    }

    fun registerModule(module: Module) {
        modules.add(module)
        // Ensure a default config is present if not loaded
        moduleConfigs.putIfAbsent(module.name, ModuleConfig(module.name, module.isEnabled))
    }

    fun getModule(name: String): Module? {
        return modules.find { it.name == name }
    }

    fun getAllModules(): List<Module> {
        return modules.toList()
    }

    fun loadConfig() {
        if (configFile.exists()) {
            try {
                val jsonString = configFile.readText()
                if (jsonString.isNotBlank()) {
                    val loadedConfigsList = json.decodeFromString<List<ModuleConfig>>(jsonString)
                    loadedConfigsList.forEach { config ->
                        moduleConfigs[config.name] = config
                    }
                }
            } catch (e: Exception) {
                // Log error: "Failed to load module configs"
                println("Failed to load module configs from ${configFile.absolutePath}: ${e.message}")
                e.printStackTrace()
            }
        }
        // Apply loaded config to modules
        applyConfigToModules()
    }

    private fun applyConfigToModules() {
        modules.forEach { module ->
            moduleConfigs[module.name]?.let { config ->
                // Enable module if config says enabled and module is not, or disable if config says disabled and module is.
                if (config.isEnabled && !module.isEnabled) {
                    module.toggle() 
                } else if (!config.isEnabled && module.isEnabled) {
                    module.toggle()
                }
            }
        }
    }
    
    fun saveConfig() {
        try {
            // Update moduleConfigs from the current state of modules before saving
            val currentConfigs = modules.map { module ->
                ModuleConfig(module.name, module.isEnabled)
            }
            val jsonString = json.encodeToString(currentConfigs)
            configFile.writeText(jsonString)
        } catch (e: Exception) {
            // Log error: "Failed to save module configs"
            println("Failed to save module configs to ${configFile.absolutePath}: ${e.message}")
            e.printStackTrace()
        }
    }

    // Call this after all modules are registered and config is loaded.
    // Typically in your mod's initializer.
    fun initializeModules() {
        loadConfig() // Load saved states
        // Modules states are applied within loadConfig via applyConfigToModules
        // Any module that was not in the config file will retain its default isEnabled state.
        // If a module was in the config, its state (enabled/disabled) would have been set by applyConfigToModules.
        // For modules registered after loadConfig (if any), their default state will be used,
        // or they can be manually updated and then saveConfig called.
    }
}
