package cinnamon.penguin.modules.esp

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString // Added
import kotlinx.serialization.json.Json // Added
import java.io.File // Added
import net.fabricmc.loader.api.FabricLoader // Added
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.client.render.VertexConsumerProvider // Added
import net.minecraft.client.render.WorldRenderer          // Added
import net.minecraft.client.util.math.MatrixStack         // Added
import net.minecraft.util.math.Box                        // Added
import net.minecraft.util.math.Vec3d                      // Added
import net.minecraft.client.render.RenderLayer            // Added

@Serializable
data class EspSettings(
    var enabled: Boolean = true,
    var renderPlayers: Boolean = true,
    var renderHostiles: Boolean = true,
    var renderPassives: Boolean = true,
    var renderItems: Boolean = true,
    var glowColorPlayers: String = "FFFFFF", // Hex color, default white
    var glowColorHostiles: String = "FF0000", // Hex color, default red
    var glowColorPassives: String = "00FF00", // Hex color, default green
    var glowColorItems: String = "0000FF",  // Hex color, default blue
    var glowIntensity: Float = 1.0f // Placeholder for now, might relate to shader uniforms or outline thickness
    // Add more specific entity type toggles or color options later if needed
)

object CustomEspModule {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true } // Added
    private val configFile = File(FabricLoader.getInstance().configDir.resolve("penguinclient/esp_settings.json").toUri()) // Added

    var settings: EspSettings = EspSettings() // Initialize with default settings
        private set // Modified: Make setter private

    // Placeholder for initialization logic (e.g., loading settings from config)
    fun initialize() {
        println("CustomEspModule: Initializing...") // Modified
        loadSettings() // Modified
    }

    // Added: Load settings from config file
    private fun loadSettings() {
        try {
            if (configFile.exists()) {
                val fileContent = configFile.readText()
                if (fileContent.isNotBlank()) {
                    settings = json.decodeFromString<EspSettings>(fileContent)
                    println("CustomEspModule: Settings loaded from ${configFile.absolutePath}")
                } else {
                    println("CustomEspModule: Config file is blank, using default settings and saving.")
                    saveSettings() // Save defaults if file is blank
                }
            } else {
                println("CustomEspModule: Config file not found at ${configFile.absolutePath}. Using default settings and creating a new one.")
                saveSettings() // Create config file with default settings if it doesn't exist
            }
        } catch (e: Exception) {
            println("CustomEspModule: Error loading settings from ${configFile.absolutePath}. Using default settings. Error: ${e.message}")
            e.printStackTrace() // Log the stack trace for debugging
            // Fallback to default settings in case of any error
            settings = EspSettings() 
            // Attempt to save the default settings to ensure a valid file for next launch if it was corrupt
            try {
                saveSettings()
                println("CustomEspModule: Attempted to save default settings after load error.")
            } catch (saveEx: Exception) {
                println("CustomEspModule: Failed to save default settings after load error. Error: ${saveEx.message}")
                saveEx.printStackTrace()
            }
        }
    }

    // Added: Save settings to config file
    fun saveSettings() { // Made public for potential external use (e.g., GUI)
        try {
            configFile.parentFile?.mkdirs() // Ensure parent directories exist
            val jsonString = json.encodeToString(settings)
            configFile.writeText(jsonString)
            println("CustomEspModule: Settings saved to ${configFile.absolutePath}")
        } catch (e: Exception) {
            println("CustomEspModule: Error saving settings to ${configFile.absolutePath}. Error: ${e.message}")
            e.printStackTrace() // Log the stack trace for debugging
        }
    }

    private val previouslyGlowingEntities = mutableSetOf<Entity>()

    // Placeholder for enabling the module
    fun enable() {
        settings.enabled = true
        println("CustomEspModule enabled")
        // Potentially register event listeners here
    }

    // Placeholder for disabling the module
    fun disable() {
        settings.enabled = false
        println("CustomEspModule disabled")
        val client = MinecraftClient.getInstance() // Get client instance
        if (client.world != null) {
            previouslyGlowingEntities.forEach { entity ->
                if (entity.isAlive && client.world!!.getEntityById(entity.id) == entity) {
                    entity.isGlowing = false
                }
            }
        }
        previouslyGlowingEntities.clear()
        entitiesToRender.clear()
    }

    // Private list to store entities that should be highlighted
    private val entitiesToRender = mutableListOf<Entity>()

    // Updates the list of entities to be rendered by ESP
    fun updateTargetedEntities() {
        val client = MinecraftClient.getInstance()
        val world = client.world
        val player = client.player

        if (!settings.enabled || world == null || player == null) {
            // If disabled or world/player is null, ensure all previously glowing entities are no longer glowing
            previouslyGlowingEntities.forEach { entity ->
                 if (entity.isAlive && client.world?.getEntityById(entity.id) == entity) { // client.world can be null here
                    entity.isGlowing = false
                }
            }
            previouslyGlowingEntities.clear()
            entitiesToRender.clear()
            return
        }
        
        val noLongerGlowing = previouslyGlowingEntities.toMutableSet()
        entitiesToRender.clear()

        for (entity in world.entities) {
            if (entity == player) {
                continue
            }

            val shouldAdd = when {
                settings.renderPlayers && entity is PlayerEntity -> true
                settings.renderHostiles && entity is HostileEntity -> true
                settings.renderPassives && entity is PassiveEntity -> true
                settings.renderItems && entity is ItemEntity -> true
                else -> false
            }

            if (shouldAdd) {
                entitiesToRender.add(entity)
            }
        }

        noLongerGlowing.removeAll(entitiesToRender.toSet())

        noLongerGlowing.forEach { entity ->
            if (entity.isAlive && MinecraftClient.getInstance().world?.getEntityById(entity.id) == entity) {
                entity.isGlowing = false
            }
        }

        entitiesToRender.forEach { entity ->
            if (entity.isAlive) { 
                entity.isGlowing = true
            }
        }

        previouslyGlowingEntities.clear()
        if (settings.enabled) { // This check is technically redundant due to the early return, but good for clarity
            previouslyGlowingEntities.addAll(entitiesToRender)
        }
    }

    // Renders ESP highlights for the targeted entities
    fun renderEspHighlights(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, cameraPos: Vec3d) {
        if (!settings.enabled || entitiesToRender.isEmpty()) {
            return
        }
        // Bounding box rendering is removed/commented for this step.
        // Vanilla glow is handled by entity.isGlowing = true/false in updateTargetedEntities.
    }

    // TODO:
    // 1. Implement configuration loading/saving for EspSettings.
    // 2. Implement entity iteration and filtering based on settings. (Filtering is partially done in updateTargetedEntities)
    // 3. Implement actual rendering logic (outline, glow). (Vanilla glow implemented)
}
