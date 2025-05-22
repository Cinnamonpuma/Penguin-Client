package cinnamon.penguin.gui.common

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JToggleButton
import javax.swing.border.Border
import javax.swing.border.EmptyBorder

internal class RoundedButton(
    text: String? = null,
    icon: Icon? = null,
    private val arcWidth: Int = 10,
    private val arcHeight: Int = 10,
    customBorder: Border? = BorderFactory.createEmptyBorder(5, 10, 5, 10) // Default padding
) : JButton(text, icon) {

    init {
        isContentAreaFilled = false // We paint our own background
        isFocusPainted = false
        border = customBorder
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D // Create a copy to avoid affecting other components
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Paint background
        g2.color = background
        g2.fillRoundRect(0, 0, width, height, arcWidth, arcHeight)
        
        // Paint text and icon
        super.paintComponent(g2)
        g2.dispose()
    }
}

internal class RoundedToggleButton(
    text: String? = null,
    icon: Icon? = null,
    private val arcWidth: Int = 10,
    private val arcHeight: Int = 10,
    customBorder: Border? = BorderFactory.createEmptyBorder(8, 15, 8, 15) // Default padding
) : JToggleButton(text, icon) {

    init {
        isContentAreaFilled = false // We paint our own background
        isFocusPainted = false
        border = customBorder
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Paint background using the component's current background color.
        g2.color = background 
        g2.fillRoundRect(0, 0, width, height, arcWidth, arcHeight)
        
        // Let the superclass paint the text and icon
        super.paintComponent(g2)
        g2.dispose()
    }
}
