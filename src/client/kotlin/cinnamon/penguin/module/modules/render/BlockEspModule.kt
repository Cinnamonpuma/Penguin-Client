package cinnamon.penguin.module.modules.render

import cinnamon.penguin.module.Category
import cinnamon.penguin.module.Module
import cinnamon.penguin.utils.ColorUtils // Keep ColorUtils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext // Keep for rendering
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box // For range checking
import net.minecraft.world.World
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks // For checking specific block types
import net.minecraft.client.render.WorldRenderer // Still needed for drawBox
import net.minecraft.client.render.RenderLayer // Still needed for drawBox
import org.lwjgl.glfw.GLFW // Keep for default key
import java.io.File
import java.util.concurrent.ConcurrentHashMap // Might not be needed if block list is updated synchronously

class BlockEspModule : Module(
    "BlockESP",
    // Updated description for simpler ESP
    "Highlights selected types of blocks through walls.",
    Category.RENDER,
    GLFW.GLFW_KEY_UNKNOWN
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    private val configFile = File(FabricLoader.getInstance().configDir.resolve("penguinclient/render/block_esp.json").toUri())

    var settings: BlockEspSettings = BlockEspSettings()
        private set

    // Store for blocks to render - updated on tick
    private val blocksToRender = mutableSetOf<BlockPos>()
    private val tempBlockListForScan = mutableSetOf<BlockPos>() // To avoid CMEs if accessed by render during scan

    override fun onEnable() {
        super.onEnable()
        loadSettings()
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick)
        WorldRenderEvents.AFTER_ENTITIES.register(this::onRenderWorld)
        println("[${this.name}] Enabled. Scan range: ${settings.scanRange} blocks.")
    }

    override fun onDisable() {
        super.onDisable()
        // Unregistering can be tricky if lambda instances are not stored.
        // For now, rely on the 'enabled' flag within the handlers.
        // Proper unregistration would require storing the lambda or using a dedicated event handler class.
        blocksToRender.clear()
        println("[${this.name}] Disabled. Cleared blocks to render.")
    }

    fun loadSettings() {
        try {
            if (configFile.exists()) {
                val fileContent = configFile.readText()
                if (fileContent.isNotBlank()) {
                    settings = json.decodeFromString<BlockEspSettings>(fileContent)
                } else {
                    settings = BlockEspSettings(); saveSettings()
                }
            } else {
                settings = BlockEspSettings(); saveSettings()
            }
        } catch (e: Exception) {
            System.err.println("[${this.name}] Error loading settings: ${e.message}")
            settings = BlockEspSettings()
        }
    }

    fun saveSettings() {
        try {
            configFile.parentFile?.mkdirs()
            val fileContent = json.encodeToString(settings)
            configFile.writeText(fileContent)
        } catch (e: Exception) {
            System.err.println("[${this.name}] Error saving settings: ${e.message}")
        }
    }
    
    private fun onTick(client: MinecraftClient) {
        if (!settings.enabled || client.player == null || client.world == null) {
            if (blocksToRender.isNotEmpty()) blocksToRender.clear() // Clear if disabled or no world/player
            return
        }

        tempBlockListForScan.clear()
        val playerPos = client.player!!.blockPos
        val world = client.world!!
        val range = settings.scanRange

        for (yOffset in -range..range) {
            for (xOffset in -range..range) {
                for (zOffset in -range..range) {
                    val currentPos = playerPos.add(xOffset, yOffset, zOffset)
                    val blockState = world.getBlockState(currentPos)
                    if (blockState.isAir) continue

                    if (shouldHighlightBlock(blockState, currentPos, world)) {
                        tempBlockListForScan.add(currentPos.toImmutable())
                    }
                }
            }
        }
        
        // Synchronize update to blocksToRender
        // This basic swap is okay for now. For very large numbers of blocks/frequent updates,
        // more sophisticated double buffering or concurrent sets might be considered.
        synchronized(blocksToRender) {
            blocksToRender.clear()
            blocksToRender.addAll(tempBlockListForScan)
        }
    }

    private fun shouldHighlightBlock(blockState: BlockState, blockPos: BlockPos, world: World): Boolean {
        val block = blockState.block
        var shouldHighlight = false

        // Check ore types
        if (settings.highlightDiamond && block == Blocks.DIAMOND_ORE) shouldHighlight = true
        else if (settings.highlightIron && block == Blocks.IRON_ORE) shouldHighlight = true
        else if (settings.highlightGold && block == Blocks.GOLD_ORE) shouldHighlight = true
        else if (settings.highlightCoal && block == Blocks.COAL_ORE) shouldHighlight = true
        else if (settings.highlightLapis && block == Blocks.LAPIS_ORE) shouldHighlight = true
        else if (settings.highlightRedstone && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) ) shouldHighlight = true // Assuming deepslate variant too
        else if (settings.highlightEmerald && block == Blocks.EMERALD_ORE) shouldHighlight = true
        else if (settings.highlightCopper && block == Blocks.COPPER_ORE) shouldHighlight = true
        // Add deepslate variants for all ores if desired, e.g. Blocks.DEEPSLATE_DIAMOND_ORE

        // If not highlighting ores only, or if it's an ore that passed, check other blocks
        if (!settings.highlightOresOnly || shouldHighlight) {
            if (settings.highlightSpawners && block == Blocks.SPAWNER) shouldHighlight = true
            else if (settings.highlightChests && block == Blocks.CHEST) shouldHighlight = true
            // Add other specific blocks from settings here
        }
        
        if (!shouldHighlight) return false

        // Air Check
        if (settings.airCheckMode == AirCheckModeSimplified.NO_EXPOSED) {
            for (direction in net.minecraft.util.math.Direction.values()) {
                if (world.getBlockState(blockPos.offset(direction)).isAir) {
                    return false // It's exposed to air
                }
            }
        }
        return true
    }
    
    private fun onRenderWorld(context: WorldRenderContext) {
        if (!settings.enabled || mc.player == null || mc.world == null || blocksToRender.isEmpty()) return

        val matrices = context.matrixStack() ?: return // Ensure matrices is not null
        val consumers = context.consumers() ?: return // Ensure consumers is not null
        val camera = mc.gameRenderer.camera
        val outlineBuffer = consumers.getBuffer(RenderLayer.getLines()) // Use consumers from context

        matrices.push()
        matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)

        val oreColor = ColorUtils.parseHexColor(settings.oreOutlineColor) ?: ColorUtils.Color(1f, 1f, 1f, 1f)
        val otherColor = ColorUtils.parseHexColor(settings.otherBlockOutlineColor) ?: ColorUtils.Color(1f, 0f, 1f, 1f)
        val currentLineThickness = settings.lineWidth // Note: WorldRenderer.drawBox doesn't directly use a line width param.
                                                    // Custom line rendering would be needed for variable thickness.
                                                    // For now, this setting is unused by drawBox.

        synchronized(blocksToRender) { // Synchronize access if onTick is on another thread (usually not for client tick)
            blocksToRender.forEach { pos ->
                // Basic distance culling (squared distance)
                if (camera.pos.squaredDistanceTo(pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5) > settings.scanRange * settings.scanRange * 1.5) { // A bit more than scanRange
                     // continue is not allowed in forEach, use return@forEach
                     return@forEach
                }

                val blockState = mc.world!!.getBlockState(pos) // Get blockstate again to determine color
                val block = blockState.block
                val colorToUse = when {
                    // This logic needs to be robust. Comparing block instances directly is fine for vanilla blocks.
                    // A more robust way for ores might be checking against a list of ore block instances.
                    (block == Blocks.DIAMOND_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE ||
                     block == Blocks.COAL_ORE || block == Blocks.LAPIS_ORE || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE ||
                     block == Blocks.EMERALD_ORE || block == Blocks.COPPER_ORE /* || add deepslate variants */) -> oreColor
                    else -> otherColor
                }

                // WorldRenderer.drawBox(
                //     matrices,
                //     outlineBuffer,
                //     pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                //     pos.x + 1.0, pos.y + 1.0, pos.z + 1.0,
                //     colorToUse.red, colorToUse.green, colorToUse.blue, colorToUse.alpha,
                //     0f, 0f, 0f // Added normal parameters
                // )
            }
        }
        matrices.pop()
    }
}
