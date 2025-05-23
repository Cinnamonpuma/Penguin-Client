package cinnamon.penguin.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString // Required for json.decodeFromString
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import java.io.File
import cinnamon.penguin.module.ModuleManager
import cinnamon.penguin.module.Module // Required for module.enable/disable/setKey

@Serializable
data class ModuleSettings(
    val name: String,
    var enabled: Boolean = false,
    var keyCode: Int = GLFW.GLFW_KEY_UNKNOWN
)

object ConfigManager {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val configFile = File(MinecraftClient.getInstance().runDirectory, "config/penguinclient/config.json")

    fun saveConfig(moduleManager: ModuleManager) {
        try {
            configFile.parentFile?.mkdirs()
            val moduleSettingsList = moduleManager.getModules().map { module ->
                ModuleSettings(name = module.name, enabled = module.enabled, keyCode = module.keyCode)
            }
            val jsonString = json.encodeToString(moduleSettingsList)
            configFile.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadConfig(moduleManager: ModuleManager) {
        if (!configFile.exists()) {
            // Config file doesn't exist, save the current default module settings
            // println("PenguinClient Config: No config file found. Creating default config.")
            saveConfig(moduleManager)
            return
        }

        try {
            val jsonString = configFile.readText()
            val loadedSettingsList = json.decodeFromString<List<ModuleSettings>>(jsonString)

            val modules = moduleManager.getModules().associateBy { it.name }

            loadedSettingsList.forEach { settings ->
                modules[settings.name]?.let { module ->
                    // Apply enabled state, ensuring onEnable/onDisable are called
                    if (module.enabled != settings.enabled) {
                        if (settings.enabled) {
                            module.enable()
                        } else {
                            module.disable()
                        }
                    }
                    // Apply keyCode, ensuring key registration logic is handled
                    if (module.keyCode != settings.keyCode) {
                        module.setKey(settings.keyCode)
                    }
                }
            }
            // println("PenguinClient Config: Loaded module configurations.")
        } catch (e: Exception) {
            // System.err.println("PenguinClient Config: Error loading module configurations: ${e.message}")
            e.printStackTrace() // Consider a more robust logging strategy
            // Optional: Could back up corrupted config and save a default one
            // configFile.renameTo(File(configFile.path + ".corrupted"))
            // saveConfig(moduleManager)
        }
    }
}
