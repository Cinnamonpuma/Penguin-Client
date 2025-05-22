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


class MainWindow : JFrame("PenguinClient") {
    // Colors and styling - Black and white with glow
    private val backgroundColor = Color(10, 10, 10) // Near black
    private val foregroundColor = Color(240, 240, 240) // Near white
    private val accentColor = Color(255, 255, 255) // Pure white for accent/glow
    private val tabBackgroundColor = Color(25, 25, 25) // Dark gray for tabs
    private val tabSelectedColor = Color(40, 40, 40) // Slightly lighter for selected tab
    private val headerColor = Color(5, 5, 5) // Very dark for header

    init {
        try {
            // Set up the frame as undecorated
            setUndecorated(true)
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
            
            // Create main panel with a dark background
            val mainPanel = JPanel(BorderLayout())
            mainPanel.background = backgroundColor
            
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
        UIManager.put("TabbedPane.light", backgroundColor.brighter())
        UIManager.put("TabbedPane.dark", backgroundColor.darker())
        UIManager.put("TabbedPane.focus", accentColor)
        UIManager.put("Button.background", tabBackgroundColor)
        UIManager.put("Button.foreground", foregroundColor)
    }
    
    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = headerColor
        panel.preferredSize = Dimension(800, 60)
        panel.border = EmptyBorder(5, 15, 5, 15)
        
        // Create logo panel (left side)
        val logoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        logoPanel.background = headerColor
        
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
                        g2d.color = Color(0, 0, 0)
                        g2d.fillOval(5, 5, 30, 35)
                        
                        // Draw penguin belly (white oval)
                        g2d.color = Color(255, 255, 255)
                        g2d.fillOval(10, 15, 20, 20)
                        
                        // Draw penguin eyes
                        g2d.color = Color(255, 255, 255)
                        g2d.fillOval(12, 10, 6, 6)
                        g2d.fillOval(22, 10, 6, 6)
                        
                        g2d.color = Color(0, 0, 0)
                        g2d.fillOval(14, 12, 2, 2)
                        g2d.fillOval(24, 12, 2, 2)
                        
                        // Draw penguin beak
                        g2d.color = Color(255, 165, 0) // Orange
                        val beakX = intArrayOf(17, 23, 20)
                        val beakY = intArrayOf(15, 15, 20)
                        g2d.fillPolygon(beakX, beakY, 3)
                        
                        // Draw penguin feet
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
            logoText.foreground = accentColor
            logoPanel.add(logoText)
        }
        
        // Create title label
        val titleLabel = JLabel("PenguinClient")
        titleLabel.foreground = accentColor
        titleLabel.font = Font("Arial", Font.BOLD, 24)
        titleLabel.border = EmptyBorder(0, 10, 0, 0)
        logoPanel.add(titleLabel)
        
        // Create window control buttons (right side)
        val controlPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        controlPanel.background = headerColor
        
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
        styleWindowControlButton(minimizeButton, headerColor, Color(50, 50, 50), Color(200, 200, 200))
        styleWindowControlButton(maximizeButton, headerColor, Color(50, 50, 50), Color(200, 200, 200))
        styleWindowControlButton(closeButton, headerColor, Color(180, 30, 30), Color(200, 200, 200), Color.WHITE)

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
        button.background = headerColor
        button.border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
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
        button.foreground = baseFg
        var timer: Timer? = null

        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = button.background
                    val currentFg = button.foreground

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

                    button.background = nextBg
                    button.foreground = nextFg

                    if (button.background.rgb == hoverBg.rgb && button.foreground.rgb == hoverFg.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }

            override fun mouseExited(e: MouseEvent) {
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = button.background
                    val currentFg = button.foreground

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

                    button.background = nextBg
                    button.foreground = nextFg

                    if (button.background.rgb == baseBg.rgb && button.foreground.rgb == baseFg.rgb) {
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
                    0f, 0f, Color(20, 20, 20),
                    0f, 30f, Color(10, 10, 10)
                )
                g2d.paint = gradient
                g2d.fillRect(0, 0, width, 30)

                // Simplified subtle glow at the bottom of the tab bar
                g2d.color = Color(255, 255, 255, 30) // Single, subtle glow line
                g2d.drawLine(0, 30, width, 30)
                g2d.drawLine(0, 31, width, 31) // A slightly thicker feel
            }
        }
        
        tabbedPane.background = backgroundColor
        tabbedPane.foreground = foregroundColor
        tabbedPane.border = BorderFactory.createEmptyBorder()
        
        // Add a tab for each category with custom styling
        Category.values().forEach { category ->
            val categoryPanel = CategoryPanel(category)
            val scrollPane = JScrollPane(categoryPanel)
            scrollPane.border = null
            scrollPane.background = backgroundColor // Background for the scrollpane itself
            scrollPane.viewport.background = backgroundColor // Background for the viewport
            scrollPane.verticalScrollBar.unitIncrement = 16
            // Style the scrollbar to match the theme
            scrollPane.verticalScrollBar.ui = object : javax.swing.plaf.basic.BasicScrollBarUI() {
                override fun configureScrollBarColors() {
                    this.thumbColor = tabSelectedColor // Color for the scrollbar thumb
                    this.trackColor = tabBackgroundColor // Color for the scrollbar track
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
                        // Draw main line (thicker and brighter for selection)
                        g2d.color = accentColor.brighter() 
                        g2d.fillRect(0, height - 4, width, 4) // Slightly thicker selected indicator

                        // Simplified glow effect
                        g2d.color = Color(255, 255, 255, 70) // Brighter glow for selected tab
                        g2d.fillRect(0, height - 5, width, 1) // Single line glow above the main line
                    }
                }
            }
            tabLabel.foreground = foregroundColor
            tabLabel.font = Font("Arial", Font.BOLD, 12)
            tabLabel.border = EmptyBorder(8, 15, 8, 15)
            
            tabbedPane.addTab(null, scrollPane)
            tabbedPane.setTabComponentAt(tabbedPane.tabCount - 1, tabLabel)
        }
        
        return tabbedPane
    }
}
