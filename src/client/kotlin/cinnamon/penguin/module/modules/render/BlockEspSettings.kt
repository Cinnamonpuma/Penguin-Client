package cinnamon.penguin.module.modules.render

import kotlinx.serialization.Serializable

// Add this enum
enum class AirCheckMode {
    ON_LOAD, // Check only when initially placing (approximates this from OreSim)
    RECHECK, // OreSim's RECHECK on block update is complex, simplify to ON_LOAD or always check
    OFF      // Don't check for air/opaqueness
}

@Serializable
data class BlockEspSettings(
    var enabled: Boolean = false,
    var worldSeed: String = "",
    var simulationRangeChunks: Int = 8, // Default to 8 chunks simulation range
    var highlightOresOnly: Boolean = true,
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
    // Visual settings
    var oreOutlineColor: String = "FFFFFFFF",
    var otherBlockOutlineColor: String = "FF00FFFF",
    var lineWidth: Float = 1.5f,
    // Add this property
    var airCheckMode: AirCheckMode = AirCheckMode.ON_LOAD
)
