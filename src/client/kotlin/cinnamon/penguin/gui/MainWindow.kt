package cinnamon.penguin.gui

import cinnamon.penguin.module.Category
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import cinnamon.penguin.gui.common.RoundedButton // Added import

// Custom JPanel for rounded main window background
private class RoundedMainPanel(layout: LayoutManager, private val cornerRadius: Int, private val bgColor: Color) : JPanel(layout) {
    init {
        isOpaque = false // Important for transparency of corners
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = bgColor
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius)
        g2.dispose()
    }
}

class MainWindow : JFrame("PenguinClient") {
    // Colors and styling - Black and white with glow
    private val backgroundColor = Color(0, 0, 0) // black
    private val foregroundColor = Color(255, 255, 255) // white
    private val accentColor = Color(255, 255, 255) // white
    private val tabBackgroundColor = Color(15, 15, 15) // very dark grey
    private val tabSelectedColor = Color(30, 30, 30) // dark grey
    private val headerColor = Color(5, 5, 5) // near black

    init {
        try {
            // Set up the frame as undecorated
            setUndecorated(true)
            this.setBackground(Color(0,0,0,0)) // Make JFrame background transparent

            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            setSize(800, 500)
            setLocationRelativeTo(null)
            
            // Set custom icon if available
            try {
                // Try to load from resources first
                val iconStream = javaClass.getResourceAsStream("/assets/penguinclient/icon.png")
                if (iconStream != null) {
                    val icon = ImageIO.read(iconStream)
                    iconImage = icon
                } else {
                    // Try to load from file system as fallback
                    val iconFile = File("src/main/resources/assets/penguinclient/icon.png")
                    if (iconFile.exists()) {
                        val icon = ImageIO.read(iconFile)
                        iconImage = icon
                    }
                }
            } catch (e: Exception) {
                println("Failed to load icon: ${e.message}")
            }
            
            // Apply custom look and feel
            setupLookAndFeel()
            
            // Create main panel with a dark background using our custom RoundedMainPanel
            val mainPanel = RoundedMainPanel(BorderLayout(), 20, this.backgroundColor)
            // mainPanel.background = backgroundColor // No longer needed, RoundedMainPanel handles its background
            
            // Add header panel with logo and title
            val headerPanel = createHeaderPanel()
            mainPanel.add(headerPanel, BorderLayout.NORTH)
            
            // Create tabbed pane for categories
            val tabbedPane = createTabbedPane()
            mainPanel.add(tabbedPane, BorderLayout.CENTER)
            
            // Set content pane
            contentPane = mainPanel
            
            // Add window listener
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    // Clean up any resources if needed
                }
            })
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupLookAndFeel() {
        // Set custom UI properties
        UIManager.put("Panel.background", backgroundColor)
        UIManager.put("Panel.foreground", foregroundColor)
        UIManager.put("Label.foreground", foregroundColor)
        UIManager.put("TabbedPane.background", backgroundColor)
        UIManager.put("TabbedPane.foreground", foregroundColor)
        UIManager.put("TabbedPane.selected", tabSelectedColor)
        UIManager.put("TabbedPane.contentAreaColor", backgroundColor)
        UIManager.put("TabbedPane.light", Color(20,20,20)) // Adjusted for B&W
        UIManager.put("TabbedPane.dark", Color(0,0,0)) // Adjusted for B&W
        UIManager.put("TabbedPane.focus", accentColor)
        UIManager.put("Button.background", tabBackgroundColor)
        UIManager.put("Button.foreground", foregroundColor)
    }
    
    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.setBackground(headerColor)
        panel.preferredSize = Dimension(800, 60)
        panel.setBorder(EmptyBorder(5, 15, 5, 15))
        
        // Create logo panel (left side)
        val logoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        logoPanel.setBackground(headerColor)
        
        // Try to load logo image
        try {
            val logoStream = javaClass.getResourceAsStream("/assets/penguinclient/logo.png")
            if (logoStream != null) {
                val logoImage = ImageIO.read(logoStream)
                val scaledLogo = logoImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH)
                val logoLabel = JLabel(ImageIcon(scaledLogo))
                logoPanel.add(logoLabel)
            } else {
                // Create a penguin logo as a placeholder
                val logoLabel = object : JLabel() {
                    override fun paintComponent(g: Graphics) {
                        super.paintComponent(g)
                        val g2d = g as Graphics2D
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        
                        // Draw penguin body (oval)
                        g2d.color = Color(255, 255, 255) // White body
                        g2d.fillOval(5, 5, 30, 35)
                        
                        // Draw penguin belly (black oval)
                        g2d.color = Color(0,0,0) // Black belly
                        g2d.fillOval(10, 15, 20, 20)
                        
                        // Draw penguin eyes (black on white)
                        g2d.color = Color(0,0,0)
                        g2d.fillOval(12, 10, 6, 6)
                        g2d.fillOval(22, 10, 6, 6)
                        
                        // Pupils (white on black) - not really visible with this scheme but kept for structure
                        g2d.color = Color(255,255,255)
                        g2d.fillOval(14, 12, 2, 2)
                        g2d.fillOval(24, 12, 2, 2)
                        
                        // Draw penguin beak (grey)
                        g2d.color = Color(150,150,150) // Grey beak
                        val beakX = intArrayOf(17, 23, 20)
                        val beakY = intArrayOf(15, 15, 20)
                        g2d.fillPolygon(beakX, beakY, 3)
                        
                        // Draw penguin feet (grey)
                        g2d.fillOval(12, 35, 8, 4)
                        g2d.fillOval(20, 35, 8, 4)
                    }
                }
                logoLabel.preferredSize = Dimension(40, 40)
                logoPanel.add(logoLabel)
            }
        } catch (e: Exception) {
            println("Failed to load logo: ${e.message}")
            // Create a text-based logo as fallback
            val logoText = JLabel("P")
            logoText.font = Font("Arial", Font.BOLD, 24)
            logoText.setForeground(accentColor)
            logoPanel.add(logoText)
        }
        
        // Create title label
        val titleLabel = JLabel("PenguinClient")
        titleLabel.setForeground(accentColor)
        titleLabel.font = Font("Arial", Font.BOLD, 24)
        titleLabel.setBorder(EmptyBorder(0, 10, 0, 0))
        logoPanel.add(titleLabel)
        
        // Create window control buttons (right side)
        val controlPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        controlPanel.setBackground(headerColor)

        // Global Settings Button
        val globalSettingsButton = RoundedButton( // Use common RoundedButton
            icon = null, // Icon will be set below
            arcWidth = 8, 
            arcHeight = 8, 
            customBorder = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        )
        globalSettingsButton.preferredSize = Dimension(30,30) // Set preferred size after instantiation
        globalSettingsButton.toolTipText = "Global Settings"
        globalSettingsButton.setBackground(headerColor) // Match other header buttons

        try {
            // Attempt to load icon from path (relative to resources)
            val iconPath = "/assets/penguinclient/icons/global_settings_icon.png"
            val iconStream = javaClass.getResourceAsStream(iconPath)
            if (iconStream != null) {
                val iconImg = ImageIO.read(iconStream)
                globalSettingsButton.icon = ImageIcon(iconImg.getScaledInstance(16, 16, Image.SCALE_SMOOTH))
            } else {
                // This case might not be strictly necessary if getResourceAsStream returns null for non-existent files
                // and the catch block handles it. But good for explicit clarity.
                println("Icon stream is null for $iconPath. Using fallback.")
                throw RuntimeException("Icon not found at $iconPath, using fallback.")
            }
        } catch (e: Exception) {
            // Fallback to custom painted icon if loading fails for any reason
            println("Failed to load global_settings_icon.png: ${e.message}. Using fallback custom icon.")
            globalSettingsButton.icon = object : Icon {
                override fun getIconWidth(): Int = 16
                override fun getIconHeight(): Int = 16
                override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                    val g2d = g.create() as Graphics2D
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
                    g2d.color = foregroundColor // Use theme's foreground color (white)

                    val barHeight = 2
                    val barWidth = 14 // Max width within 16x16
                    val spacing = 2 // Spacing between bars

                    // Center the bars vertically
                    val totalHeight = (barHeight * 3) + (spacing * 2)
                    val startY = y + (getIconHeight() - totalHeight) / 2

                    g2d.fillRect(x + (getIconWidth() - barWidth) / 2, startY, barWidth, barHeight)
                    g2d.fillRect(x + (getIconWidth() - barWidth) / 2, startY + barHeight + spacing, barWidth, barHeight)
                    g2d.fillRect(x + (getIconWidth() - barWidth) / 2, startY + (barHeight + spacing) * 2, barWidth, barHeight)
                    g2d.dispose()
                }
            }
        }
        
        globalSettingsButton.addActionListener {
            showGlobalSettings()
        }
        // Use existing styling function, ensure it works with RoundedButton background
        styleWindowControlButton(globalSettingsButton, headerColor, Color(50, 50, 50), foregroundColor, foregroundColor)
        controlPanel.add(globalSettingsButton) // Add to the left of minimize

        // Minimize button
        val minimizeButton = createWindowControlButton("_") {
            state = Frame.ICONIFIED
        }

        // Maximize button
        val maximizeButton = createWindowControlButton("□") {
            extendedState = if (extendedState == Frame.MAXIMIZED_BOTH) Frame.NORMAL else Frame.MAXIMIZED_BOTH
        }
        
        // Close button
        val closeButton = createWindowControlButton("×") {
            dispose()
        }
        
        // Add hover effects and specific styling for close button
        styleWindowControlButton(minimizeButton, headerColor, Color(50, 50, 50), foregroundColor, foregroundColor)
        styleWindowControlButton(maximizeButton, headerColor, Color(50, 50, 50), foregroundColor, foregroundColor)
        styleWindowControlButton(closeButton, headerColor, Color(70, 70, 70), foregroundColor, foregroundColor) // Grey hover for close
        
        controlPanel.add(minimizeButton)
        controlPanel.add(maximizeButton)
        controlPanel.add(closeButton)
        
        // Add components to header panel
        panel.add(logoPanel, BorderLayout.WEST)
        panel.add(controlPanel, BorderLayout.EAST)
        
        // Make the header panel draggable to move the window
        var initialClick: Point? = null
        
        panel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                initialClick = e.point
            }
        })
        
        panel.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                // Get current location of the frame
                val thisX = location.x
                val thisY = location.y
                
                // Determine how much the mouse moved since the initial click
                val xMoved = e.x - initialClick!!.x
                val yMoved = e.y - initialClick!!.y
                
                // Move frame to new location
                setLocation(thisX + xMoved, thisY + yMoved)
            }
        })
        
        return panel
    }

    private fun createWindowControlButton(text: String, action: () -> Unit): JButton {
        val button = JButton(text)
        button.font = Font("Arial", Font.BOLD, 14) // Consistent font for controls
        button.setBackground(headerColor)
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10))
        button.isFocusPainted = false
        button.addActionListener { action() }
        return button
    }

    private fun styleWindowControlButton(
        button: JButton,
        baseBg: Color,
        hoverBg: Color,
        baseFg: Color,
        hoverFg: Color = baseFg // Default hover foreground is same as base
    ) {
        button.setForeground(baseFg)
        var timer: Timer? = null

        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = button.getBackground()
                    val currentFg = button.getForeground()

                    val nextBg = Color(
                        currentBg.red + (hoverBg.red - currentBg.red) / 5,
                        currentBg.green + (hoverBg.green - currentBg.green) / 5,
                        currentBg.blue + (hoverBg.blue - currentBg.blue) / 5
                    )
                    val nextFg = Color(
                        currentFg.red + (hoverFg.red - currentFg.red) / 5,
                        currentFg.green + (hoverFg.green - currentFg.green) / 5,
                        currentFg.blue + (hoverFg.blue - currentFg.blue) / 5
                    )

                    button.setBackground(nextBg)
                    button.setForeground(nextFg)

                    if (button.getBackground().getRGB() == hoverBg.rgb && button.getForeground().getRGB() == hoverFg.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }

            override fun mouseExited(e: MouseEvent) {
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = button.getBackground()
                    val currentFg = button.getForeground()

                    val nextBg = Color(
                        currentBg.red + (baseBg.red - currentBg.red) / 5,
                        currentBg.green + (baseBg.green - currentBg.green) / 5,
                        currentBg.blue + (baseBg.blue - currentBg.blue) / 5
                    )
                    val nextFg = Color(
                        currentFg.red + (baseFg.red - currentFg.red) / 5,
                        currentFg.green + (baseFg.green - currentFg.green) / 5,
                        currentFg.blue + (baseFg.blue - currentFg.blue) / 5
                    )

                    button.setBackground(nextBg)
                    button.setForeground(nextFg)

                    if (button.getBackground().getRGB() == baseBg.rgb && button.getForeground().getRGB() == baseFg.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }
        })
    }
    
    private fun createTabbedPane(): JTabbedPane {
        // Create a custom styled tabbed pane with glow effect
        val tabbedPane = object : JTabbedPane(JTabbedPane.TOP) {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                
                val g2d = g as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                
                // Add a subtle gradient to the tab bar
                val gradient = GradientPaint(
                    0f, 0f, Color(10, 10, 10), // Darker top for B&W
                    0f, 30f, Color(5, 5, 5)   // Even darker bottom for B&W
                )
                g2d.paint = gradient
                g2d.fillRect(0, 0, width, 30)

                // Enhanced subtle glow at the bottom of the tab bar
                val glowLayers = 3
                val baseAlpha = 20
                for (i in 0 until glowLayers) {
                    val currentAlpha = baseAlpha - (i * (baseAlpha / glowLayers))
                    if (currentAlpha <= 0) continue
                    g2d.color = Color(255, 255, 255, currentAlpha)
                    g2d.drawLine(0, 30 + i, width, 30 + i) // Draw lines downwards
                }
            }
        }
        
        tabbedPane.setBackground(backgroundColor)
        tabbedPane.setForeground(foregroundColor)
        tabbedPane.setBorder(BorderFactory.createEmptyBorder())
        
        // Add a tab for each category with custom styling
        Category.values().forEach { category ->
            val categoryPanel = CategoryPanel(category)
            val scrollPane = JScrollPane(categoryPanel)
            scrollPane.setBorder(null)
            scrollPane.setBackground(backgroundColor) // Background for the scrollpane itself
            scrollPane.getViewport().setBackground(backgroundColor) // Background for the viewport
            scrollPane.verticalScrollBar.unitIncrement = 16
            // Style the scrollbar to match the theme
            scrollPane.verticalScrollBar.ui = object : javax.swing.plaf.basic.BasicScrollBarUI() {
                override fun configureScrollBarColors() {
                    this.thumbColor = Color(50,50,50) // Dark grey thumb
                    this.trackColor = Color(15,15,15) // Very dark grey track
                }
                override fun createDecreaseButton(orientation: Int): JButton {
                    return createZeroButton()
                }
                override fun createIncreaseButton(orientation: Int): JButton {
                    return createZeroButton()
                }
                private fun createZeroButton(): JButton {
                    val button = JButton()
                    button.preferredSize = Dimension(0,0)
                    button.minimumSize = Dimension(0,0)
                    button.maximumSize = Dimension(0,0)
                    return button
                }
            }
            
            // Create a custom tab component with glow
            val tabLabel = object : JLabel(category.displayName) {
                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    
                    val g2d = g as Graphics2D
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    
                    // Add a bottom border to the selected tab with glow
                    if (tabbedPane.selectedComponent == scrollPane) {
                        val arc = 6 // Corner radius for tab selection indicator
                        val mainLineThickness = 3
                        val glowEffectLayers = 3 // Number of layers for the glow around the selection indicator
                        val glowBaseAlpha = 40 // Base alpha for the selection glow

                        // Draw main selection indicator (rounded)
                        g2d.color = accentColor // White for selected tab line
                        g2d.fillRoundRect(0, height - mainLineThickness, width, mainLineThickness, arc, arc)

                        // Draw softer glow effect around the main line
                        // This glow will be slightly outside the main selection line
                        for (i in 0 until glowEffectLayers) {
                            val currentAlpha = glowBaseAlpha - (i * (glowBaseAlpha / glowEffectLayers))
                            if (currentAlpha <= 0) continue
                            
                            g2d.color = Color(255, 255, 255, currentAlpha)
                            // The glow should be drawn around the main selection indicator.
                            // We can expand the rectangle slightly for each layer.
                            // Offset x,y and increase width,height for an outset effect.
                            // For a simple bottom glow, we can draw lines/rects under it.
                            // Let's try drawing rounded rects just outside the main one.
                            // This paints a glow *above* the main line, for a more integrated look.
                            val glowOffset = i + 1 // Glow starts 1px above the main line and expands
                            g2d.drawRoundRect(
                                0 - glowOffset, 
                                height - mainLineThickness - glowOffset, 
                                width + (glowOffset * 2), 
                                mainLineThickness + glowOffset, // Make glow slightly taller
                                arc + glowOffset, // Slightly larger arc for outer layers
                                arc + glowOffset
                            )
                        }
                    }
                }
            }
            tabLabel.setForeground(foregroundColor)
            tabLabel.font = Font("Arial", Font.BOLD, 12)
            tabLabel.setBorder(EmptyBorder(8, 15, 8, 15))
            
            tabbedPane.addTab(null, scrollPane)
            tabbedPane.setTabComponentAt(tabbedPane.tabCount - 1, tabLabel)
        }
        
        return tabbedPane
    }

    private fun showGlobalSettings() {
        val dialog = JDialog(this, "Global Settings", true)
        dialog.isUndecorated = true // Important for custom border painting
        dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        dialog.setSize(450, 350) // Adjusted size
        dialog.setLocationRelativeTo(this)
        dialog.setBackground(Color(0,0,0,0)) // Make dialog background transparent for custom border

        // Custom border for rounded corners and glow
        dialog.rootPane.border = object : javax.swing.border.AbstractBorder() {
            private val cornerRadiusVal = 15 // Renamed to avoid conflict if cornerRadius is a parameter
            private val glowLayersVal = 4 // Made a property of the AbstractBorder instance
            private val baseGlowAlphaVal = 30 // Made a property of the AbstractBorder instance

            override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
                val g2d = g.create() as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Paint the actual dialog background (rounded)
                g2d.color = this@MainWindow.backgroundColor // Use MainWindow's theme background
                g2d.fillRoundRect(x, y, width -1 , height -1, cornerRadiusVal, cornerRadiusVal)

                // Paint glow effect (expanding outwards)
                for (i in 0 until glowLayersVal) {
                    val alpha = baseGlowAlphaVal - (i * (baseGlowAlphaVal / glowLayersVal))
                    if (alpha <= 0) continue
                    g2d.color = Color(255, 255, 255, alpha)
                    // Draw glow slightly outside the main rounded rect
                    g2d.drawRoundRect(
                        x - i, y - i,
                        width - 1 + (i * 2), height - 1 + (i * 2),
                        cornerRadiusVal + i, cornerRadiusVal + i
                    )
                }
                g2d.dispose()
            }

            override fun getBorderInsets(c: Component): Insets {
                // Insets to accommodate the glow effect
                return Insets(glowLayersVal, glowLayersVal, glowLayersVal, glowLayersVal)
            }
        }

        val mainDialogPanel = JPanel(BorderLayout())
        mainDialogPanel.isOpaque = false // The rootPane's border now paints the background
        // Use the value from the border instance for padding
        val borderInsets = dialog.rootPane.border.getBorderInsets(mainDialogPanel)
        mainDialogPanel.border = EmptyBorder(15 + borderInsets.top, 15 + borderInsets.left, 15 + borderInsets.bottom, 15 + borderInsets.right)


        val titleLabel = JLabel("Global Settings")
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        titleLabel.setForeground(foregroundColor)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        titleLabel.setBorder(EmptyBorder(0,0,10,0))
        mainDialogPanel.add(titleLabel, BorderLayout.NORTH)
        
        val placeholderLabel = JLabel("Global settings options will appear here.")
        placeholderLabel.font = Font("Arial", Font.PLAIN, 14)
        placeholderLabel.setForeground(foregroundColor)
        placeholderLabel.horizontalAlignment = SwingConstants.CENTER
        mainDialogPanel.add(placeholderLabel, BorderLayout.CENTER)

        val closeButton = JButton("Close") // Standard JButton, can be RoundedButton if preferred
        closeButton.setBackground(tabSelectedColor) // A theme color
        closeButton.setForeground(foregroundColor)
        closeButton.isFocusPainted = false
        closeButton.addActionListener { dialog.dispose() }
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.isOpaque = false // Transparent background for button panel too
        buttonPanel.setBorder(EmptyBorder(10,0,0,0))
        buttonPanel.add(closeButton)
        mainDialogPanel.add(buttonPanel, BorderLayout.SOUTH)

        dialog.contentPane = mainDialogPanel // Set content pane for the dialog
        dialog.isVisible = true
    }
}
