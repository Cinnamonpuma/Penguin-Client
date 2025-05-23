package cinnamon.penguin.config

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import org.lwjgl.glfw.GLFW
import net.minecraft.client.MinecraftClient

data class GlobalConfig(
    val guiOpenKeyCode: Int = GLFW.GLFW_KEY_F7
)

object GlobalSettingsManager {
    private val gson = Gson()
    private val configFile = File(MinecraftClient.getInstance().runDirectory, "config/penguinclient/global_settings.json")
    var currentConfig: GlobalConfig = GlobalConfig()

    fun loadSettings() {
        if (configFile.exists()) {
            try {
                FileReader(configFile).use { reader ->
                    currentConfig = gson.fromJson(reader, GlobalConfig::class.java)
                    println("[PenguinClient] Global settings loaded successfully.")
                }
            } catch (e: Exception) {
                System.err.println("[PenguinClient] Error loading global settings: ${e.message}")
                currentConfig = GlobalConfig()
                saveSettings()
            }
        } else {
            println("[PenguinClient] Global settings file not found, creating with default values.")
            currentConfig = GlobalConfig()
            saveSettings()
        }
    }

    fun saveSettings() {
        try {
            configFile.parentFile.mkdirs()
            FileWriter(configFile).use { writer ->
                gson.toJson(currentConfig, writer)
                println("[PenguinClient] Global settings saved successfully.")
            }
        } catch (e: Exception) {
            System.err.println("[PenguinClient] Error saving global settings: ${e.message}")
        }
    }
}
