package cinnamon.penguin.module.modules.render

import cinnamon.penguin.module.Category
import cinnamon.penguin.module.Module
// import cinnamon.penguin.config.ConfigManager // Already there if needed for module's own general config
import cinnamon.penguin.utils.OreConfig
import cinnamon.penguin.utils.OreProperties
import cinnamon.penguin.utils.PenguinChunkRandom // Our new wrapper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.lwjgl.glfw.GLFW
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.world.chunk.WorldChunk

// Imports for vein generation
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import java.util.BitSet
import kotlin.math.*

// Imports for rendering
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.WorldRenderer // For drawBox outline
import net.minecraft.client.util.math.MatrixStack
// import net.minecraft.client.render.VertexConsumerProvider // Not directly used if mc.renderBuffers.outlineVertexConsumers is used
import net.minecraft.client.render.RenderLayer
// import com.mojang.blaze3d.systems.RenderSystem // For GL states if needed, but drawBox might handle it
import cinnamon.penguin.utils.ColorUtils


class BlockEspModule : Module(
    "BlockESP",
    "Highlights blocks, with ore simulation using world seed.",
    Category.RENDER,
    GLFW.GLFW_KEY_UNKNOWN
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    private val configFile = File(FabricLoader.getInstance().configDir.resolve("penguinclient/render/block_esp.json").toUri())

    var settings: BlockEspSettings = BlockEspSettings()
        private set

    // Store for simulated ores: ChunkPos.toLong() -> Map<OreProperties, Set<BlockPos>>
    val simulatedOresByChunk = ConcurrentHashMap<Long, MutableMap<OreProperties, MutableSet<BlockPos>>>()

    private var currentWorldSeed: Long = 0L
    private lateinit var chunkRandom: PenguinChunkRandom // To be initialized

    override fun onEnable() {
        super.onEnable()
        loadSettings()

        if (settings.enabled && settings.worldSeed.isNotBlank()) {
            try {
                currentWorldSeed = settings.worldSeed.toLong()
                chunkRandom = PenguinChunkRandom(currentWorldSeed) // Initialize with the seed from settings
                println("[${this.name}] Enabled. World seed: $currentWorldSeed. Ore sim range: ${settings.simulationRangeChunks} chunks.")
                
                // Initial scan of chunks around player
                scanAndSimulateChunksAroundPlayer()

            } catch (e: NumberFormatException) {
                System.err.println("[${this.name}] Invalid world seed format: ${settings.worldSeed}. Must be a number.")
                disable() // Or notify user and don't enable fully
                return
            }
        } else {
            println("[${this.name}] Enabled, but ore simulation is inactive (module disabled in settings or no world seed).")
        }
        WorldRenderEvents.AFTER_ENTITIES.register(this::onRenderWorld)
    }

    override fun onDisable() {
        super.onDisable()
        simulatedOresByChunk.clear()
        println("[${this.name}] Disabled. Cleared simulated ore data.")
        WorldRenderEvents.AFTER_ENTITIES.unregister(this::onRenderWorld)
    }
    
    private var lastPlayerChunkX: Int = 0
    private var lastPlayerChunkZ: Int = 0

    override fun onTick() { // Assuming Module base class has onTick or we register ClientTickEvents.END_CLIENT_TICK
        if (!settings.enabled || currentWorldSeed == 0L || mc.player == null || mc.world == null) return

        val playerChunkX = mc.player!!.chunkPos.x
        val playerChunkZ = mc.player!!.chunkPos.z

        if (playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ) {
            lastPlayerChunkX = playerChunkX
            lastPlayerChunkZ = playerChunkZ
            scanAndSimulateChunksAroundPlayer()
        }
    }

    private fun scanAndSimulateChunksAroundPlayer() {
        if (mc.world == null || mc.player == null) return

        val playerChunkX = mc.player!!.chunkPos.x
        val playerChunkZ = mc.player!!.chunkPos.z
        val range = settings.simulationRangeChunks

        for (dx in -range..range) {
            for (dz in -range..range) {
                val chunkX = playerChunkX + dx
                val chunkZ = playerChunkZ + dz
                mc.world?.getChunk(chunkX, chunkZ)?.let { chunk ->
                    if (chunk is WorldChunk) { // Ensure it's a full WorldChunk
                        simulateChunk(chunk)
                    }
                }
            }
        }
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

    private fun simulateChunk(chunk: WorldChunk) {
        val chunkPosLong = chunk.pos.toLong()
        if (simulatedOresByChunk.containsKey(chunkPosLong)) {
            return
        }

        val oresInThisChunk = ConcurrentHashMap<OreProperties, MutableSet<BlockPos>>()
        val populationSeed = chunkRandom.setPopulationSeed(currentWorldSeed, chunk.pos.x shl 4, chunk.pos.z shl 4)
        
        val dimensionId = mc.world?.registryKey?.value?.toString() ?: return
        // TODO: Get specific biome for chunk or for each block. For now, null means general for dimension.
        val oresToSimulate = OreConfig.getOresForDimensionAndBiome(dimensionId, null)

        for (oreProperty in oresToSimulate) {
            if (!shouldSimulateOre(oreProperty)) continue

            val orePositions = mutableSetOf<BlockPos>()
            // Ensure chunkRandom is re-seeded for each ore decorator sequence
            chunkRandom.setDecoratorSeed(populationSeed, oreProperty.index, oreProperty.step)

            // veinsPerChunk is often a float in vanilla (e.g. 0.5 means 50% chance for 1 vein)
            // For now, we'll treat it as an integer count.
            // OreSim.java: final int count = ore.count.get(random);
            // This implies ore.count could be a distribution (e.g. UniformIntProvider)
            // For simplicity, veinsPerChunk is an Int in our OreProperties.
            val veinCount = oreProperty.veinsPerChunk

            for (i in 0 until veinCount) {
                // Rarity check: if rarity is 1.0, it's common. If > 1.0, it's 1/rarity chance.
                // Example: rarity = 10.0 means 1/10 chance.
                // So, we continue (skip) if random.nextFloat() is GTE 1.0/rarity.
                if (oreProperty.rarity > 1.0f && chunkRandom.nextFloat() >= (1.0f / oreProperty.rarity)) {
                    continue
                }

                val veinX = chunkRandom.nextInt(16) + (chunk.pos.x shl 4)
                val veinZ = chunkRandom.nextInt(16) + (chunk.pos.z shl 4)
                val veinY = oreProperty.heightProvider.get(chunkRandom, 0) // context 0 is placeholder

                val veinOrigin = BlockPos(veinX, veinY, veinZ)
                
                val generatedVeinBlocks = if (oreProperty.scattered) {
                    generateVeinScattered(veinOrigin, oreProperty, chunkRandom, mc.world!!)
                } else {
                    generateVeinNormal(veinOrigin, oreProperty, chunkRandom, mc.world!!)
                }
                orePositions.addAll(generatedVeinBlocks)
            }
            if (orePositions.isNotEmpty()) {
                oresInThisChunk[oreProperty] = orePositions
            }
        }
        
        if (oresInThisChunk.isNotEmpty()) {
            simulatedOresByChunk[chunkPosLong] = oresInThisChunk
            // println("Simulated ${oresInThisChunk.values.sumOf { it.size }} ores in chunk ${chunk.pos.x}, ${chunk.pos.z}")
        }
    }
    
    private fun shouldSimulateOre(oreProperty: OreProperties): Boolean {
        return when (oreProperty.associatedBlockId) {
            "minecraft:diamond_ore" -> settings.highlightDiamond
            "minecraft:iron_ore" -> settings.highlightIron
            "minecraft:gold_ore" -> settings.highlightGold
            "minecraft:coal_ore" -> settings.highlightCoal
            "minecraft:lapis_ore" -> settings.highlightLapis // Note: OreConfig uses "minecraft:lapis_lazuli_ore" for id, "minecraft:lapis_ore" for associatedBlockId
            "minecraft:redstone_ore" -> settings.highlightRedstone
            "minecraft:emerald_ore" -> settings.highlightEmerald
            "minecraft:copper_ore" -> settings.highlightCopper
            else -> false // Don't simulate if not in settings or if highlightOresOnly is true and this isn't an ore
        }
    }

    private fun generateVeinNormal(origin: BlockPos, ore: OreProperties, random: PenguinChunkRandom, world: ClientWorld): Set<BlockPos> {
        val veinSize = ore.veinSize
        val f = random.nextFloat() * PI.toFloat()
        val g = veinSize.toFloat() / 8.0f
        val i = ceil(sin(f) * g).toInt()
        val d = ceil(cos(f) * g).toInt()
        val e = -2 * i
        val h = -2 * d
        val j = i
        val l = d
        val m = random.nextInt(3)
        val n = random.nextInt(3)
        val o = random.nextInt(3)
        val p = random.nextInt(abs(e - i) + 1) + min(e, i)
        val q = random.nextInt(abs(h - l) + 1) + min(h, l)
        val r = random.nextInt(abs(m - n) + o + 1) + min(abs(m - n) + o, o) // OreSim has a complex expression here, simplified slightly based on understanding

        // Origin is already the calculated veinX, veinY, veinZ from simulateChunk
        // No need for world.getTopY check as height is determined by ore.heightProvider
        return generateVeinPartInternal(world, random, veinSize, 
            origin.x.toDouble() + p, origin.x.toDouble() + q, 
            origin.z.toDouble() + r, origin.z.toDouble() + random.nextInt(3) - 1, // Simplified Z calculation slightly for end points
            origin.y.toDouble() + random.nextInt(3) - 1, origin.y.toDouble() + random.nextInt(3) - 1,
            p, q, r, i, d, ore)
    }

    private fun generateVeinPartInternal(world: ClientWorld, random: PenguinChunkRandom, veinSize: Int, 
                                        startX: Double, endX: Double, 
                                        startZ: Double, endZ: Double, 
                                        startY: Double, endY: Double, 
                                        xDrift: Int, zDrift: Int, yDrift: Int, // these are p, q, r from generateNormal
                                        maxHorizontalRadius: Int, maxVerticalRadius: Int, // these are i, d from generateNormal
                                        ore: OreProperties): Set<BlockPos> {
        val poses = mutableSetOf<BlockPos>()
        val bitSet = BitSet(veinSize * veinSize * veinSize)
        val ds = DoubleArray(veinSize * 4)

        for (bl in 0 until veinSize) {
            ds[bl * 4 + 0] = random.nextDouble() * maxHorizontalRadius.toDouble()
            ds[bl * 4 + 1] = random.nextDouble() * maxHorizontalRadius.toDouble()
            ds[bl * 4 + 2] = random.nextDouble() * maxVerticalRadius.toDouble()
            ds[bl * 4 + 3] = random.nextDouble() * maxVerticalRadius.toDouble()
        }

        val mutablePos = BlockPos.Mutable()

        for (bl in 0 until veinSize) {
            var bm = 0.0
            var bn = 0.0
            var bo = 0.0
            var bp = 0.0
            val bq = bl.toFloat() / veinSize.toFloat()

            for (br in 0 until veinSize) {
                val bs = br.toFloat() / veinSize.toFloat()
                val bt = lerpInternal(bs, ds[bl * 4 + 0], ds[((bl + 1) % veinSize) * 4 + 0])
                val bu = lerpInternal(bs, ds[bl * 4 + 1], ds[((bl + 1) % veinSize) * 4 + 1])
                val bv = lerpInternal(bs, ds[bl * 4 + 2], ds[((bl + 1) % veinSize) * 4 + 2])
                val bw = lerpInternal(bs, ds[bl * 4 + 3], ds[((bl + 1) % veinSize) * 4 + 3])
                val bx = lerpInternal(bq, startX, endX) + sin(PI.toFloat() * bs) * bt
                val by = lerpInternal(bq, startZ, endZ) + cos(PI.toFloat() * bs) * bu
                val bz = lerpInternal(bq, startY, endY) + sin(PI.toFloat() * bs) * bv + cos(PI.toFloat() * bs) * bw
                if (bl == 0) {
                    bm = bx
                    bn = by
                    bo = bz
                    bp = bs
                }
                val ca = floor(bx - bm).toInt()
                val cb = floor(by - bn).toInt()
                val cc = floor(bz - bo).toInt()
                val cd = floor(bs - bp).toInt()
                bm = bx
                bn = by
                bo = bz
                bp = bs
                for (ce in min(cd, 0) .. max(cd, 0)) {
                    for (cf in min(ca, 0) .. max(ca, 0)) {
                        for (cg in min(cb, 0) .. max(cb, 0)) {
                            for (ch in min(cc, 0) .. max(cc, 0)) {
                                val ci = xDrift + cf
                                val cj = zDrift + cg
                                val ck = yDrift + ch
                                mutablePos.set(ci, cj, ck) // Using drift values as base for relative positions
                                
                                // Index for bitset, adapted from OreSim.java
                                val an = (((ck % veinSize) + veinSize) % veinSize * veinSize + (((cj % veinSize) + veinSize) % veinSize)) * veinSize + (((ci % veinSize) + veinSize) % veinSize)

                                if (bitSet[an]) continue

                                // Initial Target Block Check
                                val blockState = world.getBlockState(mutablePos)
                                if (settings.airCheckMode != AirCheckMode.OFF && !blockState.isOpaque) {
                                    // Could add more checks: !blockState.material.isReplaceable, etc.
                                    // For now, only isOpaque as per prompt's core requirement.
                                    continue
                                }
                                
                                if (shouldPlaceBlockByConfig(mutablePos, ore, random, world)) {
                                    bitSet.set(an)
                                    poses.add(mutablePos.toImmutable())
                                }
                            }
                        }
                    }
                }
            }
        }
        return poses
    }

    private fun generateVeinScattered(origin: BlockPos, ore: OreProperties, random: PenguinChunkRandom, world: ClientWorld): Set<BlockPos> {
        val poses = mutableSetOf<BlockPos>()
        val tempOrePropsForScattered = ore.copy(discardOnAirChance = 1.0f) // Ensure strict air check for scattered
        val mutablePos = BlockPos.Mutable()

        for (i in 0 until ore.veinSize) { // veinSize for scattered ores usually means number of attempts/blocks
            val x = randomCoordInternal(random, 8) // Spread within a chunk area, similar to vanilla
            val y = randomCoordInternal(random, 16) // Spread within a vertical range
            val z = randomCoordInternal(random, 8)
            mutablePos.set(origin.x + x, origin.y + y, origin.z + z)

            // Initial Target Block Check (similar to generateVeinPartInternal)
            val blockState = world.getBlockState(mutablePos)
            if (settings.airCheckMode != AirCheckMode.OFF && !blockState.isOpaque) {
                continue
            }

            if (shouldPlaceBlockByConfig(mutablePos, tempOrePropsForScattered, random, world)) {
                poses.add(mutablePos.toImmutable())
            }
        }
        return poses
    }
    
    private fun shouldPlaceBlockByConfig(pos: BlockPos, ore: OreProperties, random: PenguinChunkRandom, world: ClientWorld): Boolean {
        val discardChance = ore.discardOnAirChance
        if (discardChance == 0.0f) return true // Place unconditionally if initial checks passed

        if (discardChance == 1.0f) { // Strict: only place if fully enclosed
            for (direction in Direction.values()) {
                if (!world.getBlockState(pos.offset(direction)).isOpaque) return false
            }
            return true
        }
        // Probabilistic: discardChance is probability *to discard*
        // So, return true (place) if random.nextFloat() is >= discardChance
        return random.nextFloat() >= discardChance
    }

    private fun randomCoordInternal(random: PenguinChunkRandom, size: Int): Int {
        // OreSim: Math.round((random.nextFloat() - random.nextFloat()) * (float)i);
        // Kotlin: round((random.nextFloat() - random.nextFloat()) * size.toFloat()).toInt()
        return round((random.nextFloat() - random.nextFloat()) * size.toFloat()).toInt()
    }

    private fun lerpInternal(delta: Float, start: Double, end: Double): Double = start + delta * (end - start)

    // Rendering Logic
    private fun onRenderWorld(context: WorldRenderContext) {
        if (!settings.enabled || mc.player == null || mc.world == null) return

        val matrices = context.matrixStack()
        val camera = mc.gameRenderer.camera

        matrices.push()
        matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z) // Translate to camera origin

        val color = ColorUtils.parseHexColor(settings.oreOutlineColor) ?: ColorUtils.Color(1f, 1f, 1f, 1f) // Default white

        // Iterate through simulated ores
        simulatedOresByChunk.values.forEach { oreMap ->
            oreMap.forEach { (oreProperty, positions) ->
                if (shouldRenderOreType(oreProperty)) { // Check against settings like highlightDiamond etc.
                    positions.forEach { pos ->
                        // Basic culling: check if block is reasonably close to camera
                        if (camera.pos.squaredDistanceTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) < 256 * 256) { // 256 blocks
                            WorldRenderer.drawBox(
                                matrices, // MatrixStack
                                mc.renderBuffers.outlineVertexConsumers.getBuffer(RenderLayer.getLines()), // VertexConsumer
                                pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                pos.x + 1.0, pos.y + 1.0, pos.z + 1.0,
                                color.red, color.green, color.blue, color.alpha
                            )
                        }
                    }
                }
            }
        }
        matrices.pop()
    }

    // Helper to check if a specific ore type (from OreProperties) should be rendered based on module settings
    private fun shouldRenderOreType(oreProperty: OreProperties): Boolean {
        return when (oreProperty.associatedBlockId) { // This is the same as shouldSimulateOre, maybe rename/reuse
            "minecraft:diamond_ore" -> settings.highlightDiamond
            "minecraft:iron_ore" -> settings.highlightIron
            "minecraft:gold_ore" -> settings.highlightGold
            "minecraft:coal_ore" -> settings.highlightCoal
            "minecraft:lapis_ore" -> settings.highlightLapis // Check ID, Lapis lazuli ore ID is "minecraft:lapis_ore"
            "minecraft:redstone_ore" -> settings.highlightRedstone
            "minecraft:emerald_ore" -> settings.highlightEmerald
            "minecraft:copper_ore" -> settings.highlightCopper
            else -> false
        }
    }
}
