package cinnamon.penguin.gui

import cinnamon.penguin.module.Category
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import cinnamon.penguin.gui.common.RoundedButton // Added import
import cinnamon.penguin.config.GlobalSettingsManager
import cinnamon.penguin.input.KeyboardHandler
import cinnamon.penguin.module.ModuleManager
import org.lwjgl.glfw.GLFW
import net.minecraft.client.util.InputUtil

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
    // Colors and styling - New Palette
    private val backgroundColor = Color(35, 35, 35)
    private val foregroundColor = Color(240, 240, 240)
    private val accentColor = Color(70, 130, 180) // Steel Blue
    private val tabBackgroundColor = Color(45, 45, 45)
    private val tabSelectedColor = Color(60, 60, 60)
    private val headerColor = Color(25, 25, 25)

    private fun getKeyDisplayName(keyCode: Int): String {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return "NONE"
        // Placeholder due to issues resolving InputUtil.getKeyName or InputUtil.getKey in the current environment.
        // This allows the build to proceed. A proper fix requires investigating Minecraft version / mappings.
        System.err.println("[PenguinClient] Warning: InputUtil methods for key name resolution are unavailable. Using placeholder for key code $keyCode.")
        return "Key: $keyCode"
    }

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
        UIManager.put("TabbedPane.light", Color(50,50,50)) 
        UIManager.put("TabbedPane.dark", Color(30,30,30)) 
        UIManager.put("TabbedPane.focus", accentColor)
        UIManager.put("Button.background", tabBackgroundColor)
        UIManager.put("Button.foreground", foregroundColor)
    }
    
    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.setBackground(headerColor) // Updated
        panel.preferredSize = Dimension(800, 60)
        panel.setBorder(EmptyBorder(5, 15, 5, 15))
        
        // Create logo panel (left side)
        val logoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        logoPanel.setBackground(headerColor) // Updated
        
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
            logoText.setForeground(accentColor) // Updated
            logoPanel.add(logoText)
        }
        
        // Create title label
        val titleLabel = JLabel("PenguinClient")
        titleLabel.setForeground(accentColor) // Updated
        titleLabel.font = Font("Arial", Font.BOLD, 24)
        titleLabel.setBorder(EmptyBorder(0, 10, 0, 0))
        logoPanel.add(titleLabel)
        
        // Create window control buttons (right side)
        val controlPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        controlPanel.setBackground(headerColor) // Updated

        // Global Settings Button
        val globalSettingsButton = RoundedButton( // Use common RoundedButton
            icon = null, // Icon will be set below
            arcWidth = 8, 
            arcHeight = 8, 
            customBorder = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        )
        globalSettingsButton.preferredSize = Dimension(30,30) // Set preferred size after instantiation
        globalSettingsButton.toolTipText = "Global Settings"
        globalSettingsButton.setBackground(headerColor) // Updated

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
                    g2d.color = foregroundColor // Updated

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
        styleWindowControlButton(globalSettingsButton, headerColor, Color(70, 70, 70), foregroundColor, foregroundColor) // Updated hoverBg
        controlPanel.add(globalSettingsButton) // Add to the left of minimize

        // Minimize button
        val minimizeButton = createWindowControlButton("_") { // createWindowControlButton will use new headerColor
            state = Frame.ICONIFIED
        }

        // Maximize button
        val maximizeButton = createWindowControlButton("□") { // createWindowControlButton will use new headerColor
            extendedState = if (extendedState == Frame.MAXIMIZED_BOTH) Frame.NORMAL else Frame.MAXIMIZED_BOTH
        }
        
        // Close button
        val closeButton = createWindowControlButton("×") { // createWindowControlButton will use new headerColor
            dispose()
        }
        
        // Add hover effects and specific styling for close button
        styleWindowControlButton(minimizeButton, headerColor, Color(70, 70, 70), foregroundColor, foregroundColor) // Updated hoverBg
        styleWindowControlButton(maximizeButton, headerColor, Color(70, 70, 70), foregroundColor, foregroundColor) // Updated hoverBg
        styleWindowControlButton(closeButton, headerColor, Color(180, 70, 70), foregroundColor, foregroundColor) // Updated hoverBg for close
        
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
        button.setBackground(headerColor) // Updated
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
                    0f, 0f, Color(40, 40, 40), // Updated
                    0f, 30f, Color(30, 30, 30)   // Updated
                )
                g2d.paint = gradient
                g2d.fillRect(0, 0, width, 30)

                // Enhanced subtle glow at the bottom of the tab bar
                val glowLayers = 3
                val baseAlpha = 20 // Keep alpha low for subtlety
                for (i in 0 until glowLayers) {
                    val currentAlpha = baseAlpha - (i * (baseAlpha / glowLayers))
                    if (currentAlpha <= 0) continue
                    // Use accentColor for glow if it's light enough, otherwise white
                    val glowColor = if (accentColor.red > 100 || accentColor.green > 100 || accentColor.blue > 100) accentColor else Color.WHITE
                    g2d.color = Color(glowColor.red, glowColor.green, glowColor.blue, currentAlpha)
                    g2d.drawLine(0, 30 + i, width, 30 + i) // Draw lines downwards
                }
            }
        }
        
        tabbedPane.setBackground(backgroundColor) // Updated
        tabbedPane.setForeground(foregroundColor) // Updated
        tabbedPane.setBorder(BorderFactory.createEmptyBorder())
        
        // Add a tab for each category with custom styling
        Category.values().forEach { category ->
            val categoryPanel = CategoryPanel(category)
            val scrollPane = JScrollPane(categoryPanel)
            scrollPane.setBorder(null)
            scrollPane.setBackground(backgroundColor) // Updated
            scrollPane.getViewport().setBackground(backgroundColor) // Updated
            scrollPane.verticalScrollBar.unitIncrement = 16
            // Style the scrollbar to match the theme
            scrollPane.verticalScrollBar.ui = object : javax.swing.plaf.basic.BasicScrollBarUI() {
                override fun configureScrollBarColors() {
                    this.thumbColor = Color(80,80,80) // Updated
                    this.trackColor = Color(45,45,45) // Updated
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
                        val mainLineThickness = 4
                        val glowEffectLayers = 3 // Number of layers for the glow around the selection indicator
                        val glowBaseAlpha = 40 // Base alpha for the selection glow

                        // Draw main selection indicator (rounded)
                        g2d.color = accentColor // Updated
                        g2d.fillRoundRect(0, height - mainLineThickness, width, mainLineThickness, arc, arc)

                        // Draw softer glow effect around the main line
                        // This glow will be slightly outside the main selection line
                        for (i in 0 until glowEffectLayers) {
                            val currentAlpha = glowBaseAlpha - (i * (glowBaseAlpha / glowEffectLayers))
                            if (currentAlpha <= 0) continue
                            
                            g2d.color = Color(accentColor.red, accentColor.green, accentColor.blue, currentAlpha) // Updated
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
            tabLabel.setForeground(foregroundColor) // Updated
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
        dialog.setSize(550, 450) // Adjusted size for new content
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
                g2d.color = this@MainWindow.backgroundColor // Updated
                g2d.fillRoundRect(x, y, width -1 , height -1, cornerRadiusVal, cornerRadiusVal)

                // Paint glow effect (expanding outwards)
                for (i in 0 until glowLayersVal) {
                    val alpha = baseGlowAlphaVal - (i * (baseGlowAlphaVal / glowLayersVal))
                    if (alpha <= 0) continue
                    g2d.color = Color(accentColor.red, accentColor.green, accentColor.blue, alpha) // Updated
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

        val mainDialogPanel = JPanel(GridBagLayout()) // Changed to GridBagLayout
        mainDialogPanel.isOpaque = false // The rootPane's border now paints the background
        // Use the value from the border instance for padding
        val borderInsets = dialog.rootPane.border.getBorderInsets(mainDialogPanel)
        mainDialogPanel.border = EmptyBorder(15 + borderInsets.top, 15 + borderInsets.left, 15 + borderInsets.bottom, 15 + borderInsets.right)

        val gbc = GridBagConstraints()
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.anchor = GridBagConstraints.WEST

        val titleLabel = JLabel("Global Settings")
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        titleLabel.setForeground(foregroundColor)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        // titleLabel.setBorder(EmptyBorder(0,0,10,0)) // Removed, using GridBagConstraints
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2 // Span across two columns
        gbc.anchor = GridBagConstraints.CENTER
        mainDialogPanel.add(titleLabel, gbc)

        // Reset anchor for subsequent components
        gbc.anchor = GridBagConstraints.WEST
        gbc.gridwidth = 1 // Reset gridwidth

        // --- Global Keybind Setting ---
        val globalKeyPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        globalKeyPanel.isOpaque = false
        val globalKeyLabel = JLabel("Open GUI Key: ")
        globalKeyLabel.font = Font("Arial", Font.PLAIN, 14)
        globalKeyLabel.setForeground(foregroundColor)
        globalKeyPanel.add(globalKeyLabel)

        val currentGlobalKeyText = getKeyDisplayName(GlobalSettingsManager.currentConfig.guiOpenKeyCode)
        val globalKeyButton = JButton(currentGlobalKeyText)
        globalKeyButton.font = Font("Arial", Font.PLAIN, 12)
        globalKeyButton.background = tabSelectedColor
        globalKeyButton.foreground = foregroundColor
        globalKeyButton.isFocusPainted = false
        globalKeyButton.addActionListener {
            val originalText = globalKeyButton.text
            globalKeyButton.text = "Press any key..."
            globalKeyButton.requestFocusInWindow()

            // Remove previous listeners to avoid stacking them
            globalKeyButton.keyListeners.forEach { globalKeyButton.removeKeyListener(it) }

            val keyListener = object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    globalKeyButton.removeKeyListener(this) // Remove self

                    if (e.keyCode == KeyEvent.VK_ESCAPE) {
                        globalKeyButton.text = originalText
                    } else {
                        val mcKey = InputUtil.fromKeyCode(e.keyCode, e.extendedKeyCode)
                        val finalKeyCode = mcKey.code

                        KeyboardHandler.updateGuiOpenKey(finalKeyCode)
                        globalKeyButton.text = getKeyDisplayName(finalKeyCode)
                        println("Global GUI Key updated to: ${getKeyDisplayName(finalKeyCode)} (Code: $finalKeyCode)")
                    }
                }
            }
            globalKeyButton.addKeyListener(keyListener)
        }
        globalKeyPanel.add(globalKeyButton)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        mainDialogPanel.add(globalKeyPanel, gbc)

        // --- Module Keybinds Sub-header ---
        val moduleKeybindsHeader = JLabel("Module Keybinds")
        moduleKeybindsHeader.font = Font("Arial", Font.BOLD, 16)
        moduleKeybindsHeader.setForeground(foregroundColor)
        moduleKeybindsHeader.setBorder(EmptyBorder(10, 0, 5, 0)) // Add some top margin
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        mainDialogPanel.add(moduleKeybindsHeader, gbc)

        // --- Module Keybinds Panel ---
        val moduleKeybindsPanel = JPanel()
        moduleKeybindsPanel.layout = BoxLayout(moduleKeybindsPanel, BoxLayout.Y_AXIS)
        moduleKeybindsPanel.isOpaque = false // Transparent background
        moduleKeybindsPanel.background = backgroundColor // Match dialog bg

        ModuleManager.getModules().forEach { module ->
            val modulePanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 2)) // Added small gaps
            modulePanel.isOpaque = false
            val moduleNameLabel = JLabel(module.name + ": ")
            moduleNameLabel.font = Font("Arial", Font.PLAIN, 14)
            moduleNameLabel.setForeground(foregroundColor)
            modulePanel.add(moduleNameLabel)

            val currentModuleKeyText = getKeyDisplayName(module.keyCode)
            val moduleKeyButton = JButton(currentModuleKeyText)
            moduleKeyButton.font = Font("Arial", Font.PLAIN, 12)
            moduleKeyButton.background = tabSelectedColor
            moduleKeyButton.foreground = foregroundColor
            moduleKeyButton.isFocusPainted = false
            val currentModule = module // Capture module for the listener
            moduleKeyButton.addActionListener {
                val originalText = moduleKeyButton.text
                moduleKeyButton.text = "Press any key..."
                moduleKeyButton.requestFocusInWindow()

                // Remove previous listeners
                moduleKeyButton.keyListeners.forEach { moduleKeyButton.removeKeyListener(it) }

                val keyListener = object : KeyAdapter() {
                    override fun keyPressed(e: KeyEvent) {
                        moduleKeyButton.removeKeyListener(this) // Remove self

                        if (e.keyCode == KeyEvent.VK_ESCAPE) {
                            moduleKeyButton.text = originalText
                        } else {
                            val mcKey = InputUtil.fromKeyCode(e.keyCode, e.extendedKeyCode)
                            val finalKeyCode = mcKey.code

                            currentModule.setKey(finalKeyCode)
                            ModuleManager.saveModuleConfiguration()
                            moduleKeyButton.text = getKeyDisplayName(finalKeyCode)
                            println("Key for ${currentModule.name} updated to: ${getKeyDisplayName(finalKeyCode)} (Code: $finalKeyCode)")
                        }
                    }
                }
                moduleKeyButton.addKeyListener(keyListener)
            }
            modulePanel.add(moduleKeyButton)
            moduleKeybindsPanel.add(modulePanel)
        }

        val scrollPane = JScrollPane(moduleKeybindsPanel)
        scrollPane.isOpaque = false
        scrollPane.viewport.isOpaque = false
        scrollPane.border = BorderFactory.createLineBorder(Color(50,50,50)) // Subtle border for scrollpane
        scrollPane.verticalScrollBar.ui = object : javax.swing.plaf.basic.BasicScrollBarUI() {
            override fun configureScrollBarColors() {
                this.thumbColor = Color(80,80,80)
                this.trackColor = Color(45,45,45)
            }
            override fun createDecreaseButton(orientation: Int) = createZeroButton()
            override fun createIncreaseButton(orientation: Int) = createZeroButton()
            private fun createZeroButton(): JButton {
                val button = JButton()
                button.preferredSize = Dimension(0,0)
                button.minimumSize = Dimension(0,0)
                button.maximumSize = Dimension(0,0)
                return button
            }
        }
        scrollPane.verticalScrollBar.unitIncrement = 16


        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        mainDialogPanel.add(scrollPane, gbc)

        // --- Close Button ---
        val closeButton = JButton("Close")
        closeButton.setBackground(tabSelectedColor) // Updated
        closeButton.setForeground(foregroundColor) // Updated
        closeButton.font = Font("Arial", Font.PLAIN, 12) // Set font
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
