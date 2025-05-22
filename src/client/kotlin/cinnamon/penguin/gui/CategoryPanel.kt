package cinnamon.penguin.gui

import cinnamon.penguin.module.Category
import cinnamon.penguin.module.Module
import cinnamon.penguin.module.ModuleManager
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import org.lwjgl.glfw.GLFW

/**
 * Panel for displaying modules in a category with Lunar Client-inspired styling
 */
class CategoryPanel(private val category: Category) : JPanel() {
    // Colors and styling - Lunar Client inspired
    // Colors and styling - Black and white with glow
    private val backgroundColor = Color(15, 15, 15) // Near black
    private val foregroundColor = Color(240, 240, 240) // Near white
    private val accentColor = Color(255, 255, 255) // Pure white for accent/glow
    private val enabledColor = Color(220, 220, 220) // Light gray for enabled state
    private val disabledColor = Color(40, 40, 40) // Dark gray for disabled state
    private val hoverColor = Color(60, 60, 60) // Slightly lighter gray for hover
    private val buttonIconColor = Color(200, 200, 200) // Light gray for icons

    
    private val moduleButtons = mutableMapOf<Module, JToggleButton>()
    
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = backgroundColor
        border = EmptyBorder(15, 15, 15, 15)
        
        // Get modules for this category
        val modules = ModuleManager.getModulesByCategory(category)
        
        if (modules.isEmpty()) {
            val label = JLabel("No modules in this category")
            label.foreground = foregroundColor
            label.font = Font("Arial", Font.PLAIN, 14)
            label.alignmentX = Component.LEFT_ALIGNMENT
            add(label)
        } else {
            // Add each module as a toggle button
            modules.forEach { module ->
                val panel = JPanel(BorderLayout())
                panel.background = backgroundColor
                panel.alignmentX = Component.LEFT_ALIGNMENT
                panel.maximumSize = Dimension(Integer.MAX_VALUE, 45)
                panel.border = BorderFactory.createMatteBorder(0, 0, 1, 0, Color(20, 20, 20))
                
                val button = JToggleButton(module.name)
                button.isSelected = module.enabled
                button.background = if (module.enabled) enabledColor else disabledColor
                button.foreground = if (module.enabled) Color.WHITE else foregroundColor
                button.font = Font("Arial", Font.BOLD, 13)
                button.isFocusPainted = false
                button.border = BorderFactory.createEmptyBorder(8, 15, 8, 15)
                
                // Create buttonPanel here before referencing it
                val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0))
                buttonPanel.background = if (module.enabled) enabledColor else disabledColor
                
                // Add hover effect
                addHoverTransition(button, disabledColor, hoverColor, condition = { !button.isSelected })
                
                button.addActionListener {
                    module.toggle()
                    button.background = if (module.enabled) enabledColor else disabledColor
                    button.foreground = if (module.enabled) Color.WHITE else foregroundColor
                    
                    // Add glow effect to button panel when enabled
                    if (module.enabled) {
                        buttonPanel.border = object : javax.swing.border.AbstractBorder() {
                            private val glowColor = Color(255, 255, 255, 70) // Single color for glow
                            private val thickness = 3 // Thickness of the glow

                            override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
                                val g2d = g as Graphics2D
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                                g2d.color = glowColor
                                // Draw a thicker line around the component
                                for (i in 0 until thickness) {
                                    g2d.drawRect(x + i, y + i, width - 1 - i * 2, height - 1 - i * 2)
                                }
                            }

                            override fun getBorderInsets(c: Component): Insets {
                                return Insets(thickness, thickness, thickness, thickness)
                            }
                        }
                    } else {
                        buttonPanel.border = null // Remove border when disabled
                    }
                }
                
                moduleButtons[module] = button
                
                // Add settings button with Lunar-style icon
                val settingsButton = JButton()
                settingsButton.background = backgroundColor
                settingsButton.isFocusPainted = false
                settingsButton.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                settingsButton.preferredSize = Dimension(30, 30)
                
                // Custom paint for gear icon
                settingsButton.icon = object : Icon {
                    override fun getIconWidth(): Int = 16
                    override fun getIconHeight(): Int = 16
                    
                    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                        val g2d = g as Graphics2D
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        
                        // Draw gear icon
                        g2d.color = buttonIconColor
                        val centerX = x + 8
                        val centerY = y + 8
                        val outerRadius = 7
                        val innerRadius = 3
                        val toothCount = 8
                        
                        // Draw outer circle with teeth
                        for (i in 0 until toothCount * 2) {
                            val angle = Math.PI * i / toothCount
                            val radius = if (i % 2 == 0) outerRadius else outerRadius - 2
                            val pointX = centerX + (radius * Math.cos(angle)).toInt()
                            val pointY = centerY + (radius * Math.sin(angle)).toInt()
                            
                            if (i == 0) {
                                g2d.drawLine(centerX, centerY, pointX, pointY)
                            } else {
                                val prevAngle = Math.PI * (i - 1) / toothCount
                                val prevRadius = if ((i - 1) % 2 == 0) outerRadius else outerRadius - 2
                                val prevX = centerX + (prevRadius * Math.cos(prevAngle)).toInt()
                                val prevY = centerY + (prevRadius * Math.sin(prevAngle)).toInt()
                                
                                g2d.drawLine(prevX, prevY, pointX, pointY)
                            }
                        }
                        
                        // Draw inner circle
                        g2d.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2)
                    }
                }
                
                // Add hover effect for settings button
                addHoverTransition(settingsButton, backgroundColor, hoverColor)
                
                settingsButton.addActionListener {
                    // Open module settings dialog
                    when (module) {
                        is cinnamon.penguin.module.modules.combat.AutoClickerModule -> {
                            showAutoClickerSettings(module)
                        }
                        // Add cases for other module types as needed
                    }
                }
                
                // Add keybind button with keyboard icon
                val keybindButton = JButton()
                keybindButton.background = backgroundColor
                keybindButton.isFocusPainted = false
                keybindButton.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                keybindButton.preferredSize = Dimension(30, 30)
                
                // Custom paint for keyboard icon
                keybindButton.icon = object : Icon {
                    override fun getIconWidth(): Int = 16
                    override fun getIconHeight(): Int = 16
                    
                    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                        val g2d = g as Graphics2D
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        
                        // Draw keyboard icon
                        g2d.color = buttonIconColor
                        
                        // Draw keyboard outline
                        g2d.drawRoundRect(x + 2, y + 5, 12, 8, 2, 2)
                        
                        // Draw keys
                        g2d.drawLine(x + 6, y + 5, x + 6, y + 13)
                        g2d.drawLine(x + 10, y + 5, x + 10, y + 13)
                        g2d.drawLine(x + 2, y + 9, x + 14, y + 9)
                    }
                }
                
                // Add hover effect for keybind button
                addHoverTransition(keybindButton, backgroundColor, hoverColor)
                
                keybindButton.addActionListener {
                    showKeybindDialog(module)
                }
                
                // Add components to panel
                panel.add(button, BorderLayout.CENTER)
                
                // Add keybind and settings buttons to buttonPanel
                buttonPanel.add(keybindButton)
                buttonPanel.add(settingsButton)
                
                // Update button panel background when module is toggled
                button.addChangeListener {
                    buttonPanel.background = if (button.isSelected) enabledColor else disabledColor
                }
                
                panel.add(buttonPanel, BorderLayout.EAST)
                
                add(panel)
                add(Box.createRigidArea(Dimension(0, 5)))
            }
        }
    }
    
    /**
     * Show settings dialog for AutoClicker module with Lunar-style UI
     */
    private fun showAutoClickerSettings(module: cinnamon.penguin.module.modules.combat.AutoClickerModule) {
        val dialog = JDialog((SwingUtilities.getWindowAncestor(this) as Frame), "AutoClicker Settings", true)
        dialog.layout = BorderLayout()
        dialog.size = Dimension(350, 280)
        dialog.setLocationRelativeTo(this)

        // Add a simplified glow effect to the dialog
        dialog.rootPane.border = object : javax.swing.border.AbstractBorder() {
            private val glowColor = Color(255, 255, 255, 50) // Single color for glow
            private val thickness = 4 // Thickness of the glow

            override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
                val g2d = g as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2d.color = glowColor
                for (i in 0 until thickness) {
                    g2d.drawRoundRect(x + i, y + i, width - 1 - i * 2, height - 1 - i * 2, 8, 8) // Rounded corners
                }
            }

            override fun getBorderInsets(c: Component): Insets {
                return Insets(thickness, thickness, thickness, thickness)
            }
        }
        
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = backgroundColor
        panel.border = EmptyBorder(15, 15, 15, 15)
        
        // Title
        val titleLabel = JLabel("AutoClicker Settings")
        titleLabel.foreground = accentColor
        titleLabel.font = Font("Arial", Font.BOLD, 16)
        titleLabel.alignmentX = Component.CENTER_ALIGNMENT
        titleLabel.border = EmptyBorder(0, 0, 15, 0)
        
        // CPS Slider
        val cpsPanel = JPanel(BorderLayout(5, 5))
        cpsPanel.background = backgroundColor
        cpsPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color(60, 60, 60)),
            "Clicks Per Second",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Font("Arial", Font.BOLD, 12),
            foregroundColor
        )
        
        val cpsLabel = JLabel("${module.clicksPerSecond} CPS")
        cpsLabel.foreground = foregroundColor
        cpsLabel.font = Font("Arial", Font.PLAIN, 12)
        cpsLabel.horizontalAlignment = SwingConstants.CENTER
        
        val cpsSlider = JSlider(JSlider.HORIZONTAL, 1, 20, module.clicksPerSecond)
        cpsSlider.background = backgroundColor
        cpsSlider.foreground = foregroundColor
        cpsSlider.paintTicks = true
        cpsSlider.paintLabels = true
        cpsSlider.majorTickSpacing = 5
        cpsSlider.minorTickSpacing = 1
        
        cpsSlider.addChangeListener { 
            module.clicksPerSecond = cpsSlider.value
            cpsLabel.text = "${module.clicksPerSecond} CPS"
        }
        
        cpsPanel.add(cpsLabel, BorderLayout.NORTH)
        cpsPanel.add(cpsSlider, BorderLayout.CENTER)
        
        // Randomization Slider
        val randomPanel = JPanel(BorderLayout(5, 5))
        randomPanel.background = backgroundColor
        randomPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color(60, 60, 60)),
            "Randomization",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Font("Arial", Font.BOLD, 12),
            foregroundColor
        )
        
        val randomLabel = JLabel("${module.randomization}% Variation")
        randomLabel.foreground = foregroundColor
        randomLabel.font = Font("Arial", Font.PLAIN, 12)
        randomLabel.horizontalAlignment = SwingConstants.CENTER
        
        val randomSlider = JSlider(JSlider.HORIZONTAL, 0, 50, module.randomization)
        randomSlider.background = backgroundColor
        randomSlider.foreground = foregroundColor
        randomSlider.paintTicks = true
        randomSlider.paintLabels = true
        randomSlider.majorTickSpacing = 10
        randomSlider.minorTickSpacing = 5
        
        randomSlider.addChangeListener { 
            module.randomization = randomSlider.value
            randomLabel.text = "${module.randomization}% Variation"
        }
        
        randomPanel.add(randomLabel, BorderLayout.NORTH)
        randomPanel.add(randomSlider, BorderLayout.CENTER)
        
        // Right Click Option
        val optionsPanel = JPanel()
        optionsPanel.layout = BoxLayout(optionsPanel, BoxLayout.Y_AXIS)
        optionsPanel.background = backgroundColor
        optionsPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color(60, 60, 60)),
            "Options",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Font("Arial", Font.BOLD, 12),
            foregroundColor
        )
        
        val rightClickCheckbox = JCheckBox("Right Click Instead")
        rightClickCheckbox.background = backgroundColor
        rightClickCheckbox.foreground = foregroundColor
        rightClickCheckbox.font = Font("Arial", Font.PLAIN, 12)
        rightClickCheckbox.isSelected = module.rightClick
        rightClickCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        rightClickCheckbox.addActionListener {
            module.rightClick = rightClickCheckbox.isSelected
        }
        
        val optionsSubPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        optionsSubPanel.background = backgroundColor
        optionsSubPanel.add(rightClickCheckbox)
        
        optionsPanel.add(optionsSubPanel)
        
        // Add all panels to the main panel
        panel.add(titleLabel)
        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(cpsPanel)
        panel.add(Box.createRigidArea(Dimension(0, 10)))
        panel.add(randomPanel)
        panel.add(Box.createRigidArea(Dimension(0, 10)))
        panel.add(optionsPanel)
        
        // Add buttons
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        buttonPanel.background = backgroundColor
        
        val closeButton = JButton("Close")
        closeButton.background = accentColor
        closeButton.foreground = Color.WHITE
        closeButton.addActionListener {
            dialog.dispose()
        }
        
        buttonPanel.add(closeButton)
        
        dialog.add(panel, BorderLayout.CENTER)
        dialog.add(buttonPanel, BorderLayout.SOUTH)
        dialog.isVisible = true
    }
    
    /**
     * Show dialog for setting module keybind
     */
    private fun showKeybindDialog(module: Module) {
        val dialog = JDialog((SwingUtilities.getWindowAncestor(this) as Frame), "Set Keybind", true)
        dialog.layout = BorderLayout()
        dialog.size = Dimension(300, 150)
        dialog.setLocationRelativeTo(this)
        
        val panel = JPanel(BorderLayout())
        panel.background = backgroundColor
        panel.border = EmptyBorder(10, 10, 10, 10)
        
        val label = JLabel("Press any key to set the keybind for ${module.name}")
        label.foreground = foregroundColor
        label.horizontalAlignment = SwingConstants.CENTER
        label.font = Font("Arial", Font.PLAIN, 12)
        
        val keyField = JTextField()
        keyField.isEditable = false
        keyField.background = backgroundColor.brighter()
        keyField.foreground = foregroundColor
        keyField.horizontalAlignment = SwingConstants.CENTER
        keyField.font = Font("Arial", Font.PLAIN, 12)
        keyField.text = if (module.keyCode == GLFW.GLFW_KEY_UNKNOWN) "None" else 
                        KeyEvent.getKeyText(module.keyCode)
        
        keyField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                val keyCode = e.keyCode
                keyField.text = KeyEvent.getKeyText(keyCode)
                module.setKey(keyCode)
                ModuleManager.saveModuleConfiguration() // Save after changing key
                dialog.dispose()
            }
        })
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        buttonPanel.background = backgroundColor
        
        val clearButton = JButton("Clear")
        clearButton.background = backgroundColor.brighter()
        clearButton.foreground = foregroundColor
        clearButton.font = Font("Arial", Font.PLAIN, 12)
        clearButton.addActionListener {
            module.setKey(GLFW.GLFW_KEY_UNKNOWN)
            keyField.text = "None"
            ModuleManager.saveModuleConfiguration() // Save after clearing key
            dialog.dispose()
        }
        
        val cancelButton = JButton("Cancel")
        cancelButton.background = backgroundColor.brighter()
        cancelButton.foreground = foregroundColor
        cancelButton.font = Font("Arial", Font.PLAIN, 12)
        cancelButton.addActionListener {
            dialog.dispose()
        }
        
        buttonPanel.add(clearButton)
        buttonPanel.add(cancelButton)
        
        panel.add(label, BorderLayout.NORTH)
        panel.add(keyField, BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        dialog.add(panel)
        dialog.isVisible = true
    }

    private fun addHoverTransition(
        component: JComponent,
        baseBg: Color,
        hoverBg: Color,
        condition: () -> Boolean = { true } // Optional condition to apply hover
    ) {
        var timer: Timer? = null
        component.background = baseBg // Ensure initial background is set

        component.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                if (!condition()) return
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = component.background
                    val nextBg = Color(
                        currentBg.red + (hoverBg.red - currentBg.red) / 5,
                        currentBg.green + (hoverBg.green - currentBg.green) / 5,
                        currentBg.blue + (hoverBg.blue - currentBg.blue) / 5
                    )
                    component.background = nextBg
                    if (component.background.rgb == hoverBg.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }

            override fun mouseExited(e: MouseEvent) {
                // No need to check condition for exiting, always revert if timer is running or bg is not base
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = component.background
                    val nextBg = Color(
                        currentBg.red + (baseBg.red - currentBg.red) / 5,
                        currentBg.green + (baseBg.green - currentBg.green) / 5,
                        currentBg.blue + (baseBg.blue - currentBg.blue) / 5
                    )
                    component.background = nextBg
                    if (component.background.rgb == baseBg.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }
        })
    }
}
