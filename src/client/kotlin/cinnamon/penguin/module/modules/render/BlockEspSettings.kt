package cinnamon.penguin.module.modules.render

import kotlinx.serialization.Serializable

enum class AirCheckModeSimplified {
    OFF,        // Render all targeted blocks
    NO_EXPOSED  // Only render if the block is not adjacent to any air blocks
}

@Serializable
data class BlockEspSettings(
    var enabled: Boolean = false,
    // Removed: worldSeed: String = "",
    // Removed: simulationRangeChunks: Int = 8,
    var highlightOresOnly: Boolean = true, // Kept: If true, only checks ore list; if false, checks ore list AND other block list.

    // Ore types
    var highlightDiamond: Boolean = true,
    var highlightIron: Boolean = true,
    var highlightGold: Boolean = true,
    var highlightCoal: Boolean = true,
    var highlightLapis: Boolean = true,
    var highlightRedstone: Boolean = true,
    var highlightEmerald: Boolean = true,
    var highlightCopper: Boolean = true,

    // Other block types
    var highlightSpawners: Boolean = false,
    var highlightChests: Boolean = false,
    // Add more specific non-ore blocks here if needed, e.g.:
    // var highlightFurnaces: Boolean = false,
    // var highlightEndPortals: Boolean = false,

    // Visual settings
    var oreOutlineColor: String = "FFFFFFFF", // White for ores
    var otherBlockOutlineColor: String = "FF00FFFF", // Magenta for other blocks
    var lineWidth: Float = 1.5f,
    
    var airCheckMode: AirCheckModeSimplified = AirCheckModeSimplified.OFF,

    // New setting for simpler Block ESP: scan range for loaded blocks
    var scanRange: Int = 32 // Radius in blocks to scan around the player
)
