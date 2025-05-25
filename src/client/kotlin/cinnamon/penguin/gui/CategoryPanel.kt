package cinnamon.penguin.gui

import cinnamon.penguin.module.Category
import cinnamon.penguin.module.Module
import cinnamon.penguin.module.ModuleManager
import cinnamon.penguin.config.ConfigManager // Added import
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import org.lwjgl.glfw.GLFW
import cinnamon.penguin.gui.common.RoundedButton // Added import
import cinnamon.penguin.gui.common.RoundedToggleButton // Added import
import cinnamon.penguin.module.modules.render.BlockEspModule // New import
import cinnamon.penguin.gui.InlineBlockEspSettingsPanel // New import

/**
 * Panel for displaying modules in a category with Lunar Client-inspired styling
 */
class CategoryPanel(private val category: Category) : JPanel() {
    // Colors and styling - New Palette
    private val backgroundColor = Color(35, 35, 35)
    private val foregroundColor = Color(240, 240, 240)
    private val accentColor = Color(70, 130, 180) // Steel Blue
    private val enabledColor = Color(65, 65, 65)
    private val disabledColor = Color(45, 45, 45)
    private val hoverColor = Color(80, 80, 80)
    private val buttonIconColor = Color(240, 240, 240)

    
    private val moduleButtons = mutableMapOf<Module, JToggleButton>()
    private val activeSettingsTimers = mutableMapOf<Module, Timer>()
    private val openSettingsPanel = mutableMapOf<Module, JPanel>() // Stores the actual content panel for settings

    // Inline Settings Panel for AutoClicker
    private inner class InlineAutoClickerSettingsPanel(
        private val module: cinnamon.penguin.module.modules.combat.AutoClickerModule
    ) : JPanel() {
        init {
            // Replicate UI from showAutoClickerSettings, use theme colors
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = this@CategoryPanel.backgroundColor.darker() // Slightly different background for settings area
            border = EmptyBorder(10, 15, 10, 15)
            alignmentX = Component.CENTER_ALIGNMENT

            // Title (Optional, can be omitted for inline)
            // val titleLabel = JLabel("AutoClicker Settings") ... add(titleLabel)

            // CPS Slider
            val cpsPanel = JPanel(BorderLayout(5, 5))
            cpsPanel.isOpaque = false // Children of opaque panel
            cpsPanel.border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color(80, 80, 80)), // Updated border color
                "Clicks Per Second",
                TitledBorder.LEFT, TitledBorder.TOP,
                Font("Arial", Font.BOLD, 11), this@CategoryPanel.foregroundColor // Updated
            )
            val cpsLabel = JLabel("${module.clicksPerSecond} CPS")
            cpsLabel.foreground = this@CategoryPanel.foregroundColor // Updated
            cpsLabel.font = Font("Arial", Font.PLAIN, 11)
            cpsLabel.horizontalAlignment = SwingConstants.CENTER
            val cpsSlider = JSlider(JSlider.HORIZONTAL, 1, 20, module.clicksPerSecond)
            cpsSlider.isOpaque = false
            cpsSlider.addChangeListener {
                module.clicksPerSecond = cpsSlider.value
                cpsLabel.text = "${module.clicksPerSecond} CPS"
            }
            cpsPanel.add(cpsLabel, BorderLayout.NORTH)
            cpsPanel.add(cpsSlider, BorderLayout.CENTER)
            add(cpsPanel)

            // Randomization Slider
            val randomPanel = JPanel(BorderLayout(5, 5))
            randomPanel.isOpaque = false
            randomPanel.border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color(80, 80, 80)), // Updated border color
                "Randomization",
                TitledBorder.LEFT, TitledBorder.TOP,
                Font("Arial", Font.BOLD, 11), this@CategoryPanel.foregroundColor // Updated
            )
            val randomLabel = JLabel("${module.randomization}% Variation")
            randomLabel.foreground = this@CategoryPanel.foregroundColor // Updated
            randomLabel.font = Font("Arial", Font.PLAIN, 11)
            randomLabel.horizontalAlignment = SwingConstants.CENTER
            val randomSlider = JSlider(JSlider.HORIZONTAL, 0, 50, module.randomization)
            randomSlider.isOpaque = false
            randomSlider.addChangeListener {
                module.randomization = randomSlider.value
                randomLabel.text = "${module.randomization}% Variation"
            }
            randomPanel.add(randomLabel, BorderLayout.NORTH)
            randomPanel.add(randomSlider, BorderLayout.CENTER)
            add(Box.createRigidArea(Dimension(0, 5)))
            add(randomPanel)

            // Right Click Option
            val optionsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            optionsPanel.isOpaque = false
            // optionsPanel.border = BorderFactory.createTitledBorder(...) // Optional border for options
            val rightClickCheckbox = JCheckBox("Right Click Instead")
            rightClickCheckbox.isOpaque = false
            rightClickCheckbox.foreground = this@CategoryPanel.foregroundColor // Updated
            rightClickCheckbox.font = Font("Arial", Font.PLAIN, 11)
            rightClickCheckbox.isSelected = module.rightClick
            rightClickCheckbox.addActionListener { module.rightClick = rightClickCheckbox.isSelected }
            optionsPanel.add(rightClickCheckbox)
            add(Box.createRigidArea(Dimension(0, 5)))
            add(optionsPanel)
        }
    }
    
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = backgroundColor
        border = EmptyBorder(15, 15, 15, 15)
        
        // Get modules for this category
        val modules = ModuleManager.getModulesByCategory(category)
        
        if (modules.isEmpty()) {
            val label = JLabel("No modules in this category")
            label.foreground = foregroundColor // Updated
            label.font = Font("Arial", Font.PLAIN, 14)
            label.alignmentX = Component.CENTER_ALIGNMENT // Center the label
            add(label)
        } else {
            // Add each module as a toggle button
            modules.forEach { module ->
                val panel = JPanel(BorderLayout())
                panel.background = backgroundColor // Updated
                panel.alignmentX = Component.CENTER_ALIGNMENT // Center the panel
                // panel.maximumSize will be set after components are added to determine preferredWidth
                panel.border = BorderFactory.createMatteBorder(0, 0, 1, 0, Color(50, 50, 50)) // Updated border color
                
                val button = RoundedToggleButton(module.name) // Use RoundedToggleButton
                button.isSelected = module.enabled
                button.background = if (module.enabled) enabledColor else disabledColor // Updated
                button.foreground = foregroundColor // Updated
                button.font = Font("Arial", Font.BOLD, 13)
                // isFocusPainted and border are handled in RoundedToggleButton init
                
                // Create buttonPanel here before referencing it
                val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0))
                buttonPanel.background = if (module.enabled) enabledColor else disabledColor // Updated
                
                // Add hover effect
                addHoverTransition(button, disabledColor, hoverColor, condition = { !button.isSelected })
                
                button.addActionListener {
                    module.toggle()
                    button.background = if (module.enabled) enabledColor else disabledColor // Updated
                    button.foreground = foregroundColor // Updated
                    
                    // Add glow effect to button panel when enabled
                    if (module.enabled) {
                        val glowLayers = 4 // Number of layers for the diffused glow
                        val baseAlpha = 30 // Base alpha for the outermost glow layer
                        val arc = 10 // Corner radius for the glow, matching buttons

                        buttonPanel.border = object : javax.swing.border.AbstractBorder() {
                            override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
                                val g2d = g.create() as Graphics2D
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                                
                                for (i in 0 until glowLayers) {
                                    val currentAlpha = baseAlpha - (i * (baseAlpha / glowLayers))
                                    if (currentAlpha <= 0) continue // Don't draw if alpha is zero or less
                                    
                                    g2d.color = Color(accentColor.red, accentColor.green, accentColor.blue, currentAlpha) // Updated glow color
                                    // Each layer is slightly inset from the previous one
                                    // The glow should expand outwards, so we draw from outside-in, or adjust x,y,width,height
                                    // Let's draw from component edge inwards for simplicity of border insets
                                    g2d.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), arc, arc)
                                }
                                g2d.dispose()
                            }

                            override fun getBorderInsets(c: Component): Insets {
                                // Insets should match the number of layers to ensure content is not overlapped
                                return Insets(glowLayers, glowLayers, glowLayers, glowLayers)
                            }
                        }
                    } else {
                        buttonPanel.border = null // Remove border when disabled
                    }
                }
                
                moduleButtons[module] = button
                
                // Add settings button with Lunar-style icon
                val settingsButton = RoundedButton() // Use RoundedButton
                settingsButton.background = backgroundColor // Updated
                // isFocusPainted and border are handled in RoundedButton init
                settingsButton.preferredSize = Dimension(30, 30)
                
                // Custom paint for gear icon
                settingsButton.icon = object : Icon {
                    override fun getIconWidth(): Int = 16
                    override fun getIconHeight(): Int = 16
                    
                    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                        val g2d = g as Graphics2D
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE) // For cleaner strokes
                        
                        g2d.color = buttonIconColor // Updated
                        val centerX = x + 8
                        val centerY = y + 8
                        val outerRadius = 7
                        val innerRadius = 2.5f // Made slightly smaller for a cleaner look
                        val toothHeight = 2.5f
                        val toothCount = 6 // Reduced tooth count for a less cluttered look

                        // Draw gear body (a filled circle)
                        g2d.fillOval(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2)
                        
                        // Draw teeth (as filled rectangles extending from the center)
                        // Erase parts of the outer circle to create teeth
                        g2d.color = backgroundColor // Updated (cut out color)
                        val toothAngleStep = 2 * Math.PI / (toothCount * 2) // Angle for each half-tooth (space and tooth)
                        
                        for (i in 0 until toothCount * 2) {
                            if (i % 2 == 0) { // This is a space between teeth
                                val angle1 = i * toothAngleStep
                                val angle2 = (i + 1) * toothAngleStep
                                
                                val path = java.awt.geom.Path2D.Double()
                                path.moveTo(centerX.toDouble(), centerY.toDouble())
                                path.lineTo(centerX + (outerRadius + toothHeight) * Math.cos(angle1), centerY + (outerRadius + toothHeight) * Math.sin(angle1))
                                path.lineTo(centerX + (outerRadius + toothHeight) * Math.cos(angle2), centerY + (outerRadius + toothHeight) * Math.sin(angle2))
                                path.closePath()
                                g2d.fill(path)
                            }
                        }

                        // Draw inner hole (filled with background color to appear as a hole)
                        g2d.color = backgroundColor // Updated (cut out color)
                        g2d.fillOval(
                            (centerX - innerRadius).toInt(), 
                            (centerY - innerRadius).toInt(), 
                            (innerRadius * 2).toInt(), 
                            (innerRadius * 2).toInt()
                        )
                        // Re-draw a thin border for the inner circle if needed, or leave as a hole
                        g2d.color = buttonIconColor // Updated
                        g2d.drawOval(
                            (centerX - innerRadius).toInt(),
                            (centerY - innerRadius).toInt(),
                            (innerRadius * 2).toInt(),
                            (innerRadius * 2).toInt()
                        )
                    }
                }
                
                // Add hover effect for settings button
                addHoverTransition(settingsButton, backgroundColor, hoverColor)
                
                settingsButton.addActionListener {
                    when (module) {
                        is cinnamon.penguin.module.modules.combat.AutoClickerModule -> {
                            toggleInlineSettings(module, panel) // 'panel' is the moduleEntryPanel
                        }
                        is BlockEspModule -> { // New case
                            toggleInlineBlockEspSettings(module, panel)
                        }
                        // Add cases for other module types as needed for their specific settings UI
                        else -> {
                            // Placeholder for modules without specific inline UI or if dialog is preferred
                             println("No inline settings UI for ${module.name}")
                        }
                    }
                }
                
                // Add keybind button with keyboard icon
                val keybindButton = RoundedButton() // Use RoundedButton
                keybindButton.background = backgroundColor // Updated
                // isFocusPainted and border are handled in RoundedButton init
                keybindButton.preferredSize = Dimension(30, 30)
                
                // Custom paint for keyboard icon
                keybindButton.icon = object : Icon {
                    override fun getIconWidth(): Int = 16
                    override fun getIconHeight(): Int = 16
                    
                    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                        val g2d = g as Graphics2D
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        
                        // Draw keyboard icon
                        g2d.color = buttonIconColor // Updated
                        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)


                        // Keyboard body (a filled rounded rectangle)
                        g2d.fillRoundRect(x + 1, y + 4, 14, 8, 3, 3)

                        // Keys (small filled rectangles, "cut out" with background color)
                        g2d.color = backgroundColor // Updated (cut out color)
                        val keyWidth = 3
                        val keyHeight = 2
                        val keyPadding = 1

                        // Top row of keys
                        g2d.fillRect(x + 3, y + 5, keyWidth, keyHeight)
                        g2d.fillRect(x + 3 + keyWidth + keyPadding, y + 5, keyWidth, keyHeight)
                        g2d.fillRect(x + 3 + (keyWidth + keyPadding) * 2, y + 5, keyWidth, keyHeight)

                        // Bottom row of keys (space bar like)
                        g2d.fillRect(x + 5, y + 5 + keyHeight + keyPadding, keyWidth + keyPadding + keyWidth/2, keyHeight)
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

                // To ensure the maximumSize change takes effect after components are added and preferred size is calculated:
                // It might be better to set maximumSize *after* adding all child components to 'panel',
                // or by creating a wrapper panel if direct preferredSize is tricky with BorderLayout.
                // However, let's try this first. If it doesn't work, we might need to explicitly set a preferred width.
                // For now, we also need to ensure that the panel's preferred width is not itself Integer.MAX_VALUE.
                // Let's re-evaluate maximumSize after children are added.
                val prefSize = panel.preferredSize
                panel.maximumSize = Dimension(prefSize.width, 45)


                add(panel) // This is the moduleEntryPanel

                // Settings Container - initially empty and not visible
                val settingsContainer = JPanel(BorderLayout())
                settingsContainer.isOpaque = false
                settingsContainer.name = "settingsContainer_${module.name}" // For easier identification if needed
                settingsContainer.maximumSize = Dimension(panel.maximumSize.width, 0) // Start collapsed
                settingsContainer.preferredSize = Dimension(panel.maximumSize.width, 0)
                add(settingsContainer)

                add(Box.createRigidArea(Dimension(0, 5))) // Keep the spacing
            }
        }
    }

    private fun toggleInlineSettings(module: cinnamon.penguin.module.modules.combat.AutoClickerModule, moduleEntryPanel: JPanel) {
        activeSettingsTimers[module]?.stop() // Stop any existing animation for this module

        val settingsContainer = (moduleEntryPanel.parent as JPanel).components.filterIsInstance<JPanel>()
            .find { it.name == "settingsContainer_${module.name}" } ?: return


        val isExpanding = openSettingsPanel[module] == null || !openSettingsPanel[module]!!.isVisible
        val currentContent = openSettingsPanel[module]

        if (isExpanding) {
            if (currentContent == null) {
                val newSettingsPanel = InlineAutoClickerSettingsPanel(module)
                newSettingsPanel.alignmentX = Component.CENTER_ALIGNMENT
                openSettingsPanel[module] = newSettingsPanel
                settingsContainer.add(newSettingsPanel, BorderLayout.CENTER)
            }
            val content = openSettingsPanel[module]!!
            content.isVisible = true
            val targetHeight = content.preferredSize.height
            animatePanel(settingsContainer, moduleEntryPanel.width, targetHeight, true, module)
        } else {
            // Collapse
            val content = openSettingsPanel[module]
            if (content != null) {
                animatePanel(settingsContainer, moduleEntryPanel.width, 0, false, module) {
                    content.isVisible = false
                    settingsContainer.removeAll() // Remove content after collapse
                    openSettingsPanel.remove(module)
                    settingsContainer.revalidate()
                    settingsContainer.repaint()
                }
            }
        }
    }

    private fun animatePanel(
        container: JPanel, 
        targetWidth: Int, 
        targetHeight: Int, 
        expand: Boolean, 
        module: Module,
        onCompletion: (() -> Unit)? = null
    ) {
        val animationSteps = 20 // Number of steps for the animation
        val stepDelay = 10 // Milliseconds between steps

        val initialHeight = container.preferredSize.height
        val heightChangePerStep = (targetHeight - initialHeight) / animationSteps
        val stepKey = "animationStep_${module.name}" // Unique key for the container's property

        val timer = Timer(stepDelay) { timerEvent -> // Renamed 'it' to 'timerEvent' for clarity
            val currentStep = (container.getClientProperty(stepKey) as? Int ?: 0) + 1
            var newHeight = initialHeight + (heightChangePerStep * currentStep)

            if ((expand && newHeight >= targetHeight) || (!expand && newHeight <= targetHeight)) {
                newHeight = targetHeight
                (timerEvent.source as Timer).stop() // Stop the timer
                activeSettingsTimers.remove(module)
                container.putClientProperty(stepKey, null) // Clean up property
                onCompletion?.invoke()
            } else {
                container.putClientProperty(stepKey, currentStep) // Update step property on container
            }

            container.preferredSize = Dimension(targetWidth, newHeight)
            container.maximumSize = Dimension(targetWidth, newHeight)
            container.revalidate()
            
            // Revalidate the parent scroll pane's viewport if CategoryPanel is in one
            (this@CategoryPanel.parent?.parent as? JViewport)?.revalidate()
            (this@CategoryPanel.parent?.parent as? JViewport)?.repaint()
            this@CategoryPanel.revalidate() // Revalidate CategoryPanel itself
            this@CategoryPanel.repaint()
        }
        container.putClientProperty(stepKey, 0) // Initialize step property on container
        activeSettingsTimers[module] = timer
        timer.start()
    }

    private fun toggleInlineBlockEspSettings(module: BlockEspModule, moduleEntryPanel: JPanel) {
        activeSettingsTimers[module]?.stop() // Stop any existing animation

        val settingsContainer = (moduleEntryPanel.parent as JPanel).components.filterIsInstance<JPanel>()
            .find { it.name == "settingsContainer_${module.name}" } ?: return

        val isExpanding = openSettingsPanel[module] == null || !openSettingsPanel[module]!!.isVisible
        val currentContent = openSettingsPanel[module]

        if (isExpanding) {
            if (currentContent == null) {
                val newSettingsPanel = InlineBlockEspSettingsPanel(module) // Use the new panel
                newSettingsPanel.alignmentX = Component.CENTER_ALIGNMENT
                openSettingsPanel[module] = newSettingsPanel
                settingsContainer.add(newSettingsPanel, BorderLayout.CENTER)
            }
            val content = openSettingsPanel[module]!!
            content.isVisible = true
            // Ensure panel.maximumSize.width is valid, might need to take from moduleEntryPanel.width
            val targetWidth = moduleEntryPanel.width.takeIf { it > 0 } ?: this.width - 30 // Fallback width
            animatePanel(settingsContainer, targetWidth, content.preferredSize.height, true, module)
        } else {
            val content = openSettingsPanel[module]
            if (content != null) {
                val targetWidth = moduleEntryPanel.width.takeIf { it > 0 } ?: this.width - 30
                animatePanel(settingsContainer, targetWidth, 0, false, module) {
                    content.isVisible = false
                    settingsContainer.removeAll()
                    openSettingsPanel.remove(module)
                    settingsContainer.revalidate()
                    settingsContainer.repaint()
                }
            }
        }
    }
    
    // Remove or comment out the old showAutoClickerSettings dialog
    // private fun showAutoClickerSettings(module: cinnamon.penguin.module.modules.combat.AutoClickerModule) { ... }

    /**
     * Show dialog for setting module keybind
     */
    private fun showKeybindDialog(module: Module) {
        val dialog = JDialog((SwingUtilities.getWindowAncestor(this) as Frame), "Set Keybind", true)
        dialog.layout = BorderLayout()
        dialog.size = Dimension(300, 150)
        dialog.setLocationRelativeTo(this)
        
        val panel = JPanel(BorderLayout())
        panel.background = backgroundColor // Updated
        panel.border = EmptyBorder(10, 10, 10, 10)
        
        val label = JLabel("Press any key to set the keybind for ${module.name}")
        label.foreground = foregroundColor // Updated
        label.horizontalAlignment = SwingConstants.CENTER
        label.font = Font("Arial", Font.PLAIN, 12)
        
        var tempKeyCode = module.keyCode // Temporary storage for the keybind

        val keyField = JTextField()
        keyField.isEditable = false
        keyField.background = disabledColor // Updated
        keyField.foreground = foregroundColor // Updated
        keyField.horizontalAlignment = SwingConstants.CENTER
        keyField.font = Font("Arial", Font.PLAIN, 12)
        keyField.text = if (tempKeyCode == GLFW.GLFW_KEY_UNKNOWN) "None" else KeyEvent.getKeyText(tempKeyCode)
        
        keyField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                tempKeyCode = e.keyCode
                keyField.text = KeyEvent.getKeyText(tempKeyCode)
                // Key is only set to module and saved when "OK" is pressed
            }
        })
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        buttonPanel.background = backgroundColor // Updated

        val okButton = RoundedButton("OK")
        okButton.background = disabledColor // Updated
        okButton.foreground = foregroundColor // Updated
        okButton.font = Font("Arial", Font.PLAIN, 12)
        okButton.addActionListener {
            module.setKey(tempKeyCode)
            // ModuleManager.saveModuleConfiguration() // Removed, module.setKey now handles saving
            dialog.dispose()
        }
        
        val clearButton = RoundedButton("Clear") // Use RoundedButton
        clearButton.background = disabledColor // Updated
        clearButton.foreground = foregroundColor // Updated
        clearButton.font = Font("Arial", Font.PLAIN, 12)
        clearButton.addActionListener {
            tempKeyCode = GLFW.GLFW_KEY_UNKNOWN
            module.setKey(GLFW.GLFW_KEY_UNKNOWN)
            keyField.text = "None"
            // ModuleManager.saveModuleConfiguration() // Removed, module.setKey now handles saving
            dialog.dispose()
        }
        
        val cancelButton = RoundedButton("Cancel") // Use RoundedButton
        cancelButton.background = disabledColor // Updated
        cancelButton.foreground = foregroundColor // Updated
        cancelButton.font = Font("Arial", Font.PLAIN, 12)
        cancelButton.addActionListener {
            dialog.dispose()
        }
        
        buttonPanel.add(okButton) // Added OK button
        buttonPanel.add(clearButton)
        buttonPanel.add(cancelButton)
        
        panel.add(label, BorderLayout.NORTH)
        panel.add(keyField, BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        dialog.add(panel)
        dialog.isVisible = true
    }

    private fun addHoverTransition(
        component: JComponent, // Changed to JComponent to allow use with RoundedButton/RoundedToggleButton
        baseBg: Color,
        hoverBg: Color,
        condition: () -> Boolean = { true } // Optional condition to apply hover
    ) {
        var timer: Timer? = null
        component.background = baseBg // Ensure initial background is set

        component.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                if (!condition()) {
                    // if condition is false, ensure baseBg is set, especially for toggle buttons
                    // that might have their background changed by selection state.
                    if (component.background != baseBg && component.background != hoverBg) { // Avoid needless repaint
                        // Only revert to baseBg if not selected or in a state that should keep another color
                        if (component is JToggleButton && component.isSelected) {
                            // For selected toggle buttons, hover should likely be on a different base (e.g. enabledColor.darker())
                            // This part might need refinement based on desired hover behavior for selected toggles
                        } else {
                           // component.background = baseBg // Reverting to baseBg if condition not met
                        }
                    }
                    return
                }
                timer?.stop()
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = component.background
                    val targetBg = if (component is JToggleButton && component.isSelected) {
                        // Determine hover for selected state, e.g. a slightly brighter/darker version of enabledColor
                        enabledColor.brighter() // Updated (uses new enabledColor)
                    } else {
                        hoverColor // Updated (uses new hoverColor)
                    }
                    val nextBg = Color(
                        currentBg.red + (targetBg.red - currentBg.red) / 5,
                        currentBg.green + (targetBg.green - currentBg.green) / 5,
                        currentBg.blue + (targetBg.blue - currentBg.blue) / 5
                    )
                    component.background = nextBg
                    if (component.background.rgb == targetBg.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }

            override fun mouseExited(e: MouseEvent) {
                timer?.stop()
                val targetBgExit = if (component is JToggleButton && component.isSelected) {
                     // When mouse exits a selected toggle button, it should revert to its selected color (e.g., enabledColor)
                    enabledColor // Updated (uses new enabledColor)
                } else {
                    baseBg // baseBg will be the new disabledColor or backgroundColor
                }
                timer = Timer(10) { // Adjust delay for smoothness
                    val currentBg = component.background
                    val nextBg = Color(
                        currentBg.red + (targetBgExit.red - currentBg.red) / 5,
                        currentBg.green + (targetBgExit.green - currentBg.green) / 5,
                        currentBg.blue + (targetBgExit.blue - currentBg.blue) / 5
                    )
                    component.background = nextBg
                    if (component.background.rgb == targetBgExit.rgb) {
                        (it.source as Timer).stop()
                    }
                }
                timer?.start()
            }
        })
    }
}
