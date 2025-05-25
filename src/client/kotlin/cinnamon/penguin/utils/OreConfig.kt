package cinnamon.penguin.utils

import cinnamon.penguin.module.modules.render.BlockEspModule // Assuming settings are there or pass dimension
import net.minecraft.world.World

// For now, a simplified hardcoded config. Ideally, this would load from JSON or be more dynamic.
object OreConfig {

    // Example: Default Overworld Ores
    // Values for index, step, veinSize, veinsPerChunk, heightProvider are illustrative and need accurate values from MC source or extensive testing.
    private val overworldOres = listOf(
        OreProperties(
            id = "minecraft:coal_ore", index = 0, step = 10, veinSize = 17, veinsPerChunk = 20,
            heightProvider = OreHeightProvider(0, 128), rarity = 1.0f, associatedBlockId = "minecraft:coal_ore"
        ),
        OreProperties(
            id = "minecraft:iron_ore", index = 1, step = 10, veinSize = 9, veinsPerChunk = 20,
            heightProvider = OreHeightProvider(0, 64), rarity = 1.0f, associatedBlockId = "minecraft:iron_ore"
        ),
        OreProperties(
            id = "minecraft:gold_ore", index = 2, step = 2, veinSize = 9, veinsPerChunk = 2,
            heightProvider = OreHeightProvider(0, 32), rarity = 1.0f, associatedBlockId = "minecraft:gold_ore"
        ),
        OreProperties( // Gold in Badlands
            id = "minecraft:gold_ore_badlands", index = 2, step = 2, veinSize = 9, veinsPerChunk = 20, // Higher count in badlands
            heightProvider = OreHeightProvider(32, 79), rarity = 1.0f, conditions = mapOf("biome_category" to "mesa"), // Fictional condition
            associatedBlockId = "minecraft:gold_ore"
        ),
        OreProperties(
            id = "minecraft:redstone_ore", index = 3, step = 8, veinSize = 8, veinsPerChunk = 8,
            heightProvider = OreHeightProvider(0, 16), rarity = 1.0f, associatedBlockId = "minecraft:redstone_ore"
        ),
        OreProperties(
            id = "minecraft:diamond_ore", index = 4, step = 1, veinSize = 8, veinsPerChunk = 1,
            heightProvider = OreHeightProvider(0, 16), rarity = 1.0f, discardOnAirChance = 0.5f, associatedBlockId = "minecraft:diamond_ore"
        ),
        OreProperties(
            id = "minecraft:lapis_lazuli_ore", index = 5, step = 1, veinSize = 7, veinsPerChunk = 1,
            heightProvider = OreHeightProvider(0, 32), scattered = true, rarity = 1.0f, associatedBlockId = "minecraft:lapis_ore"
        ),
        OreProperties(
            id = "minecraft:copper_ore", index = 6, step = 10, veinSize = 10, veinsPerChunk = 16, // Example values
            heightProvider = OreHeightProvider(40, 96), rarity = 1.0f, associatedBlockId = "minecraft:copper_ore"
        ),
        OreProperties(
            id = "minecraft:emerald_ore", index = 7, step = 1, veinSize = 1, veinsPerChunk = 6, // Typically 3-8 per chunk in mountains
            heightProvider = OreHeightProvider(4, 32), scattered = true, rarity = 1.0f, conditions = mapOf("biome_category" to "mountains"), // Fictional
            associatedBlockId = "minecraft:emerald_ore"
        )
        // TODO: Add Deepslate variants and Nether ores (Ancient Debris, Nether Gold, Nether Quartz)
    )

    // Placeholder for biome specific conditions, very simplified
    // In reality, Minecraft's ore placement is tied to FeatureGenerationStep and PlacedFeatures which are biome-specific.
    fun getOresForDimensionAndBiome(dimension: String, biomeId: String?): List<OreProperties> {
        // This is highly simplified. Real implementation needs to check biome categories, etc.
        // The reference OreSim.java uses `oreConfig.containsKey(biomeRegistryKey)`
        // and `oreConfig.values().stream().findAny().get()` which implies a more complex mapping.
        return when (dimension) {
            World.OVERWORLD.toString() -> { // This toString might not be the right key
                overworldOres.filter { ore ->
                    // Crude biome filtering example
                    if (ore.id.contains("badlands") && biomeId?.contains("badlands", ignoreCase = true) == true) true
                    else if (ore.id.contains("emerald") && biomeId?.contains("mountain", ignoreCase = true) == true) true
                    else !ore.id.contains("badlands") && !ore.id.contains("emerald") // general ores
                    // A proper implementation would use biome tags or direct biome checks.
                }
            }
            // TODO: World.NETHER.toString(), World.END.toString()
            else -> emptyList()
        }
    }
}
