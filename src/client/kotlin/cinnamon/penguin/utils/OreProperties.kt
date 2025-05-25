package cinnamon.penguin.utils

import net.minecraft.util.math.random.Random // For a potential height provider lambda

// Simplified version of OreHeightProvider from reference
// This might need to be more complex to match vanilla generation precisely
// For now, a simple range, but could be a lambda taking Random.
data class OreHeightProvider(
    val minHeight: Int,
    val maxHeight: Int
    // val distribution: (Random, Int, Int) -> Int // Example: for more complex shapes
) {
    fun get(random: Random, contextIgnored: Int = 0): Int { // contextIgnored is a placeholder
        if (minHeight > maxHeight) return minHeight // Or throw error
        return random.nextInt(maxHeight - minHeight + 1) + minHeight
    }
}

data class OreProperties(
    val id: String, // e.g., "minecraft:diamond_ore" - Changed from 'valid' to 'id'
    val index: Int, // Decorator index
    val step: Int,  // Decorator step (placement order)
    val veinSize: Int,
    val veinsPerChunk: Int, // Simplified from ore.count.get(random) for now
    val heightProvider: OreHeightProvider,
    val rarity: Float = 1.0f, // 1.0 = common, higher = rarer (1/rarity chance)
    val discardOnAirChance: Float = 0.0f, // 0.0 = place even in air, 1.0 = only if fully enclosed or airCheck is complex
    val scattered: Boolean = false, // True for emeralds, Lapis lazuli (generates differently)
    val associatedBlockId: String, // For rendering, what block does this represent
    val conditions: Map<String, String> = emptyMap() // Added conditions field
)
