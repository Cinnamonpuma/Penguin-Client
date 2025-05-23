package cinnamon.penguinclient.hotkey

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

// Hotkey data class remains the same, defined in Hotkey.kt

@Serializable
data class HotkeyConfig(val id: String, val keyCode: Int)

object HotkeyManager {
    private val hotkeys = mutableMapOf<String, Hotkey>() // Keyed by Hotkey.id
    private val hotkeyConfigs = mutableMapOf<String, HotkeyConfig>() // For storing loaded keycodes

    private val configDirectory: Path = FabricLoader.getInstance().configDir.resolve("penguinclient")
    private val configFile: File = configDirectory.resolve("hotkeys.json").toFile()
    
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        // Ensure config directory exists (also done by ModuleManager, but good practice to have it here too)
        try {
            if (!Files.exists(configDirectory)) {
                Files.createDirectories(configDirectory)
            }
        } catch (e: Exception) {
            println("Error creating config directory for HotkeyManager: ${configDirectory}")
            e.printStackTrace()
        }
    }

    fun registerHotkey(hotkey: Hotkey) {
        if (hotkeys.containsKey(hotkey.id)) {
            println("Warning: Hotkey with ID '${hotkey.id}' already registered. Overwriting definition but preserving custom keybind if loaded.")
        }
        // Apply loaded keycode if available, otherwise use default
        hotkeyConfigs[hotkey.id]?.let { loadedConfig ->
            hotkey.keyCode = loadedConfig.keyCode
        }
        hotkeys[hotkey.id] = hotkey
        // Note: We don't save here immediately; allow batch registration then explicit save.
    }

    fun unregisterHotkey(id: String) {
        hotkeys.remove(id)
        hotkeyConfigs.remove(id) // Also remove its loaded config
        // saveHotkeys() // Consider if unregistering should auto-save
    }

    fun getHotkey(id: String): Hotkey? {
        return hotkeys[id]
    }

    fun getAllHotkeys(): List<Hotkey> {
        return hotkeys.values.toList()
    }

    fun getHotkeysByCategory(category: String): List<Hotkey> {
        return hotkeys.values.filter { it.category == category }
    }

    fun onKeyPress(keyCode: Int) {
        hotkeys.values.forEach { hotkey ->
            if (hotkey.keyCode == keyCode) {
                try {
                    hotkey.action()
                } catch (e: Exception) {
                    println("Error executing hotkey action for ${hotkey.id}: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadHotkeys() {
        if (configFile.exists()) {
            try {
                val jsonString = configFile.readText()
                if (jsonString.isNotBlank()) {
                    val loadedConfigsList = json.decodeFromString<List<HotkeyConfig>>(jsonString)
                    hotkeyConfigs.clear() // Clear previous loaded configs
                    loadedConfigsList.forEach { config ->
                        hotkeyConfigs[config.id] = config
                    }
                    // Apply loaded keycodes to already registered hotkeys
                    hotkeys.values.forEach { hotkey ->
                        hotkeyConfigs[hotkey.id]?.let { loadedConfig ->
                            hotkey.keyCode = loadedConfig.keyCode
                        }
                    }
                }
            } catch (e: Exception) {
                println("Failed to load hotkey configs from ${configFile.absolutePath}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun saveHotkeys() {
        try {
            val configsToSave = hotkeys.values.map { hotkey ->
                HotkeyConfig(hotkey.id, hotkey.keyCode)
            }
            val jsonString = json.encodeToString(configsToSave)
            configFile.writeText(jsonString)
        } catch (e: Exception) {
            println("Failed to save hotkey configs to ${configFile.absolutePath}: ${e.message}")
            e.printStackTrace()
        }
    }

    // Call this after all hotkeys are initially registered, typically in your mod's initializer.
    fun initialize() {
        loadHotkeys() // Load custom keybinds
        // Hotkeys registered via registerHotkey will automatically try to apply any loaded config for their ID.
    }
}
