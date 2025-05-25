package cinnamon.penguin.utils

object ColorUtils {
    data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float)

    fun parseHexColor(hexColor: String): Color? {
        var hex = hexColor.removePrefix("#")
        if (hex.length == 6) {
            hex += "FF" // Add alpha if not present
        }
        if (hex.length != 8) {
            return null // Invalid format
        }
        return try {
            val r = Integer.parseInt(hex.substring(0, 2), 16) / 255.0f
            val g = Integer.parseInt(hex.substring(2, 4), 16) / 255.0f
            val b = Integer.parseInt(hex.substring(4, 6), 16) / 255.0f
            val a = Integer.parseInt(hex.substring(6, 8), 16) / 255.0f
            Color(r, g, b, a)
        } catch (e: NumberFormatException) {
            null
        }
    }
}
