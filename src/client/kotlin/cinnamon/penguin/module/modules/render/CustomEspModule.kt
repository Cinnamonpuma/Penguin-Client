package cinnamon.penguin.module.modules.render

import cinnamon.penguin.module.Category
import cinnamon.penguin.module.Module
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.io.File

@Serializable
data class EspSettings(
    var enabledFromDetailSettings: Boolean = true, // Renamed from 'enabled'
    var renderPlayers: Boolean = true,
    var renderHostiles: Boolean = true,
    var renderPassives: Boolean = true,
    var renderItems: Boolean = true,
    var glowColorPlayers: String = "FFFFFF",
    var glowColorHostiles: String = "FF0000",
    var glowColorPassives: String = "00FF00",
    var glowColorItems: String = "0000FF",
    var glowIntensity: Float = 1.0f
)

class CustomEspModule : Module("EntityESP", "Highlights entities through walls.", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    private val espSpecificConfigFile = File(FabricLoader.getInstance().configDir.resolve("penguinclient/render/entity_esp.json").toUri())
    
    var espSettings: EspSettings = EspSettings()
        private set

    private val entitiesToRender = mutableListOf<Entity>()
    private val previouslyGlowingEntities = mutableSetOf<Entity>()

    private val worldRenderListener = WorldRenderEvents.AfterEntities { context ->
        if (this.enabled) { // Check module's main enabled state from base Module class
            updateTargetedEntities() // This method will internally check espSettings.enabledFromDetailSettings

            // Null checks for context variables are important, as established in previous build fix
            val matrixStack = context.matrixStack()
            val consumers = context.consumers()
            val cameraPos = context.camera().pos
            if (matrixStack != null && consumers != null && cameraPos != null) {
                renderEspHighlights(matrixStack, consumers, cameraPos) // This method also checks espSettings.enabledFromDetailSettings
            }
        } else {
             // If module is disabled, ensure cleanup (also handled in onDisable, but good for immediate effect)
            if (previouslyGlowingEntities.isNotEmpty()){
                val client = MinecraftClient.getInstance()
                client.world?.let { world -> // Safe call for world
                    previouslyGlowingEntities.forEach { entity ->
                        if (entity.isAlive && world.getEntityById(entity.id) == entity) {
                            entity.isGlowing = false
                        }
                    }
                }
                previouslyGlowingEntities.clear()
                entitiesToRender.clear() // entitiesToRender might be redundant if only using glow
            }
        }
    }

    override fun onEnable() {
        super.onEnable()
        loadEspSpecificSettings() // Load settings when module is enabled
        WorldRenderEvents.AFTER_ENTITIES.register(worldRenderListener)
        previouslyGlowingEntities.clear() // Clear previous state
        entitiesToRender.clear()          // Clear previous state
        println("[${this.name}] Enabled. ESP Specific settings are initially ${if (espSettings.enabledFromDetailSettings) "active" else "inactive"}.")
    }

    override fun onDisable() {
        super.onDisable()
        WorldRenderEvents.AFTER_ENTITIES.unregister(worldRenderListener)
        val client = MinecraftClient.getInstance()
        if (client.world != null) {
            previouslyGlowingEntities.forEach { entity ->
                if (entity.isAlive && client.world!!.getEntityById(entity.id) == entity) {
                    entity.isGlowing = false
                }
            }
        }
        previouslyGlowingEntities.clear()
        entitiesToRender.clear()
        println("[${this.name}] Disabled.")
    }

    private fun loadEspSpecificSettings() {
        try {
            if (espSpecificConfigFile.exists()) {
                val fileContent = espSpecificConfigFile.readText()
                if (fileContent.isNotBlank()) { // Ensure file is not blank before decoding
                    espSettings = json.decodeFromString<EspSettings>(fileContent)
                    // Optional: println("[${this.name}] Loaded ESP specific settings from ${espSpecificConfigFile.absolutePath}")
                } else {
                    // Optional: println("[${this.name}] ESP specific settings file is blank. Using default settings and saving.")
                    espSettings = EspSettings() // Use defaults
                    saveEspSpecificSettings() // Create file with defaults
                }
            } else {
                // Optional: println("[${this.name}] ESP specific settings file not found. Creating with default settings.")
                espSettings = EspSettings() // Use defaults
                saveEspSpecificSettings() // Create file with defaults
            }
        } catch (e: Exception) {
            System.err.println("[${this.name}] Error loading specific ESP settings: ${e.message}")
            // e.printStackTrace() // Potentially too verbose for common errors like file not found initially
            espSettings = EspSettings() // Fallback to defaults
        }
    }

    private fun saveEspSpecificSettings() { // Changed to private as per typical module structure, GUI should use ModuleManager
        try {
            espSpecificConfigFile.parentFile?.mkdirs()
            val fileContent = json.encodeToString(espSettings)
            espSpecificConfigFile.writeText(fileContent)
            // Optional: println("[${this.name}] Saved ESP specific settings to ${espSpecificConfigFile.absolutePath}")
        } catch (e: Exception) {
            System.err.println("[${this.name}] Error saving specific ESP settings: ${e.message}")
            // e.printStackTrace()
        }
    }
    
    private fun updateTargetedEntities() {
        val client = MinecraftClient.getInstance()
        val world = client.world
        val player = client.player

        // Primary check from Module base class, secondary from ESP specific settings
        if (!this.enabled || !espSettings.enabledFromDetailSettings || world == null || player == null) {
            previouslyGlowingEntities.forEach { entity ->
                if (entity.isAlive && client.world?.getEntityById(entity.id) == entity) {
                    entity.isGlowing = false
                }
            }
            previouslyGlowingEntities.clear()
            entitiesToRender.clear()
            return
        }
        
        val currentFrameGlowingEntities = mutableSetOf<Entity>()

        for (entity in world.entities) {
            if (entity == player) {
                continue
            }

            val shouldRender = when {
                espSettings.renderPlayers && entity is PlayerEntity -> true
                espSettings.renderHostiles && entity is HostileEntity -> true
                espSettings.renderPassives && entity is PassiveEntity -> true
                espSettings.renderItems && entity is ItemEntity -> true
                else -> false
            }

            if (shouldRender) {
                if (entity.isAlive) {
                    entity.isGlowing = true
                    // Team based coloring logic could be added here if needed by inspecting entity.team
                    // For now, vanilla glow does not support custom colors per entity without shaders or complex workarounds.
                    // The glowColor settings in EspSettings are for future use if we implement custom shader-based glow.
                    currentFrameGlowingEntities.add(entity)
                }
            }
        }
        
        // Determine which entities are no longer targeted and turn off their glow
        val noLongerGlowing = previouslyGlowingEntities.subtract(currentFrameGlowingEntities)
        noLongerGlowing.forEach { entity ->
            if (entity.isAlive && MinecraftClient.getInstance().world?.getEntityById(entity.id) == entity) {
                entity.isGlowing = false
            }
        }

        previouslyGlowingEntities.clear()
        previouslyGlowingEntities.addAll(currentFrameGlowingEntities)
        
        // entitiesToRender list might still be useful if we add other rendering modes (boxes, etc.)
        entitiesToRender.clear()
        entitiesToRender.addAll(currentFrameGlowingEntities)
    }

    private fun renderEspHighlights(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, cameraPos: Vec3d) {
        // This function is kept for potential future use (e.g., drawing boxes, names, etc.)
        // For vanilla glow, the primary work is done in updateTargetedEntities by setting entity.isGlowing.
        // If you wanted to draw boxes *in addition* to glow, you'd do it here.
        // Ensure this check is also here for safety, though worldRenderListener also checks.
        if (!this.enabled || !espSettings.enabledFromDetailSettings || entitiesToRender.isEmpty()) {
            return
        }
        // Example: If you wanted to draw boxes (from previous implementation)
        // val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines())
        // for (entity in entitiesToRender) { ... draw box ... }
    }
}
