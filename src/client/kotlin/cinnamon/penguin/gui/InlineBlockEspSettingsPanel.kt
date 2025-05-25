package cinnamon.penguin.gui

import cinnamon.penguin.module.modules.render.BlockEspModule
import cinnamon.penguin.module.modules.render.AirCheckModeSimplified // Ensure this is the correct enum
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
// Removed TitledBorder as it wasn't used in the previous version of this file's code.
// If needed for specific sub-panels, it can be re-added locally.
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent

class InlineBlockEspSettingsPanel(private val module: BlockEspModule) : JPanel() {

    private val panelBackgroundColor = Color(50, 50, 50)
    private val componentBackgroundColor = Color(60, 60, 60)
    private val textColor = Color(220, 220, 220)
    private val borderColor = Color(80, 80, 80)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = panelBackgroundColor
        border = EmptyBorder(10, 15, 10, 15)
        alignmentX = Component.CENTER_ALIGNMENT

        addTitle("Block ESP Settings")

        // General Settings
        addToggle("Enable Module", module.settings::enabled) // This refers to BlockEspSettings.enabled, not Module.enabled
        // Removed: World Seed TextField
        // Removed: Sim Range Slider
        addSlider("Scan Range (Blocks)", 8, 128, module.settings::scanRange) // Added Scan Range
        addToggle("Highlight Ores Only", module.settings::highlightOresOnly)
        addComboBox("Air Check Mode", AirCheckModeSimplified.values(), module.settings::airCheckMode)

        // Ore Toggles
        addSectionTitle("Ore Types to Highlight")
        val orePanel = JPanel(GridLayout(0, 2, 5, 5))
        orePanel.isOpaque = false
        orePanel.border = BorderFactory.createEmptyBorder(0,10,0,10)
        addOreToggle(orePanel, "Diamond", module.settings::highlightDiamond)
        addOreToggle(orePanel, "Iron", module.settings::highlightIron)
        addOreToggle(orePanel, "Gold", module.settings::highlightGold)
        addOreToggle(orePanel, "Coal", module.settings::highlightCoal)
        addOreToggle(orePanel, "Lapis", module.settings::highlightLapis)
        addOreToggle(orePanel, "Redstone", module.settings::highlightRedstone)
        addOreToggle(orePanel, "Emerald", module.settings::highlightEmerald)
        addOreToggle(orePanel, "Copper", module.settings::highlightCopper)
        add(orePanel)

        // Other Block Toggles
        addSectionTitle("Other Block Types to Highlight")
        val otherBlocksPanel = JPanel(GridLayout(0, 2, 5, 5))
        otherBlocksPanel.isOpaque = false
        otherBlocksPanel.border = BorderFactory.createEmptyBorder(0,10,0,10)
        addOreToggle(otherBlocksPanel, "Spawners", module.settings::highlightSpawners)
        addOreToggle(otherBlocksPanel, "Chests", module.settings::highlightChests)
        add(otherBlocksPanel)

        // Visual Settings
        addSectionTitle("Visual Settings")
        addTextField("Ore Outline Color (RRGGBBAA)", module.settings::oreOutlineColor)
        addTextField("Other Block Outline Color (RRGGBBAA)", module.settings::otherBlockOutlineColor)
        addSliderFloat("Line Width", 0.5f, 5.0f, module.settings::lineWidth, "%.1f")

        add(Box.createVerticalStrut(10))
    }

    private fun addTitle(text: String) {
        val titleLabel = JLabel(text)
        titleLabel.font = Font("Arial", Font.BOLD, 16)
        titleLabel.foreground = textColor
        titleLabel.alignmentX = Component.CENTER_ALIGNMENT
        titleLabel.border = EmptyBorder(0, 0, 10, 0)
        add(titleLabel)
    }
    
    private fun addSectionTitle(text: String) {
        val sectionLabel = JLabel(text)
        sectionLabel.font = Font("Arial", Font.BOLD, 13)
        sectionLabel.foreground = textColor
        sectionLabel.alignmentX = Component.LEFT_ALIGNMENT
        sectionLabel.border = EmptyBorder(10, 0, 5, 0)
        add(sectionLabel)
    }

    private fun createStyledPanel(): JPanel {
        val panel = JPanel(BorderLayout(10, 0))
        panel.isOpaque = false
        panel.maximumSize = Dimension(Integer.MAX_VALUE, 30)
        panel.alignmentX = Component.CENTER_ALIGNMENT
        return panel
    }

    private fun <T> addToggle(label: String, property: kotlin.reflect.KMutableProperty0<T>) where T : Boolean {
        val panel = createStyledPanel()
        val checkBox = JCheckBox(label)
        checkBox.isOpaque = false
        checkBox.foreground = textColor
        checkBox.font = Font("Arial", Font.PLAIN, 12)
        checkBox.isSelected = property.get()
        checkBox.addActionListener {
            property.set(checkBox.isSelected as T)
            module.saveSettings()
        }
        panel.add(checkBox, BorderLayout.WEST)
        add(panel)
    }
    
    private fun addOreToggle(orePanel: JPanel, label: String, property: kotlin.reflect.KMutableProperty0<Boolean>) {
        val checkBox = JCheckBox(label)
        checkBox.isOpaque = false
        checkBox.foreground = textColor
        checkBox.font = Font("Arial", Font.PLAIN, 11)
        checkBox.isSelected = property.get()
        checkBox.addActionListener {
            property.set(checkBox.isSelected)
            module.saveSettings()
        }
        orePanel.add(checkBox)
    }

    private fun addTextField(label: String, property: kotlin.reflect.KMutableProperty0<String>, placeholder: String? = null) {
        val panel = createStyledPanel()
        val jLabel = JLabel("$label:")
        jLabel.foreground = textColor
        jLabel.font = Font("Arial", Font.PLAIN, 12)
        panel.add(jLabel, BorderLayout.WEST)

        val textField = JTextField(property.get())
        textField.background = componentBackgroundColor
        textField.foreground = textColor
        textField.caretColor = Companion.accentColor // Using companion object style
        textField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            EmptyBorder(2, 5, 2, 5)
        )
        if (placeholder != null && textField.text.isEmpty()) {
            textField.text = placeholder
            textField.foreground = textColor.darker()
        }
        textField.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                if (placeholder != null && textField.text == placeholder) {
                    textField.text = ""
                    textField.foreground = textColor
                }
            }
            override fun focusLost(e: FocusEvent?) {
                // Save on focus lost only if it's not the placeholder
                if (placeholder != null && textField.text == placeholder) {
                    // If it's still placeholder, don't save it as actual value, revert property if needed or leave as is
                } else {
                     property.set(textField.text)
                     module.saveSettings()
                }
                 // Ensure placeholder is shown if field is empty after attempting save
                if (placeholder != null && textField.text.isEmpty()) {
                    textField.text = placeholder
                    textField.foreground = textColor.darker()
                }
            }
        })
        textField.addActionListener {
            if (!(placeholder != null && textField.text == placeholder)) {
                property.set(textField.text)
                module.saveSettings()
            }
        }
        panel.add(textField, BorderLayout.CENTER)
        add(panel)
    }

    private fun addSlider(label: String, min: Int, max: Int, property: kotlin.reflect.KMutableProperty0<Int>) {
        val panel = createStyledPanel()
        val jLabel = JLabel("$label:")
        jLabel.foreground = textColor
        jLabel.font = Font("Arial", Font.PLAIN, 12)
        panel.add(jLabel, BorderLayout.WEST)

        val valueLabel = JLabel(property.get().toString())
        valueLabel.foreground = textColor
        valueLabel.font = Font("Arial", Font.PLAIN, 12)
        valueLabel.preferredSize = Dimension(30, valueLabel.preferredSize.height)
        panel.add(valueLabel, BorderLayout.EAST)

        val slider = JSlider(JSlider.HORIZONTAL, min, max, property.get())
        slider.isOpaque = false
        slider.addChangeListener {
            val value = slider.value
            property.set(value)
            valueLabel.text = value.toString()
            module.saveSettings()
        }
        panel.add(slider, BorderLayout.CENTER)
        add(panel)
    }
    
    private fun addSliderFloat(label: String, min: Float, max: Float, property: kotlin.reflect.KMutableProperty0<Float>, format: String) {
        val panel = createStyledPanel()
        val jLabel = JLabel("$label:")
        jLabel.foreground = textColor
        jLabel.font = Font("Arial", Font.PLAIN, 12)
        panel.add(jLabel, BorderLayout.WEST)

        val scale = 10
        val currentIntValue = (property.get() * scale).toInt()
        val minIntValue = (min * scale).toInt()
        val maxIntValue = (max * scale).toInt()

        val valueLabel = JLabel(String.format(format, property.get()))
        valueLabel.foreground = textColor
        valueLabel.font = Font("Arial", Font.PLAIN, 12)
        valueLabel.preferredSize = Dimension(40, valueLabel.preferredSize.height)
        panel.add(valueLabel, BorderLayout.EAST)

        val slider = JSlider(JSlider.HORIZONTAL, minIntValue, maxIntValue, currentIntValue)
        slider.isOpaque = false
        slider.addChangeListener {
            val floatValue = slider.value.toFloat() / scale
            property.set(floatValue)
            valueLabel.text = String.format(format, floatValue)
            module.saveSettings()
        }
        panel.add(slider, BorderLayout.CENTER)
        add(panel)
    }

    private fun <E : Enum<E>> addComboBox(label: String, values: Array<E>, property: kotlin.reflect.KMutableProperty0<E>) {
        val panel = createStyledPanel()
        val jLabel = JLabel("$label:")
        jLabel.foreground = textColor
        jLabel.font = Font("Arial", Font.PLAIN, 12)
        panel.add(jLabel, BorderLayout.WEST)

        val comboBox = JComboBox(values)
        comboBox.selectedItem = property.get()
        comboBox.font = Font("Arial", Font.PLAIN, 11)
        comboBox.background = componentBackgroundColor
        comboBox.foreground = textColor
        // Attempt to style the editor component for consistency if it's a JTextField
        (comboBox.editor?.editorComponent as? JTextField)?.let {
            it.background = componentBackgroundColor
            it.foreground = textColor
            it.caretColor = Companion.accentColor // Use Companion object for accentColor
        }
        // Style the list cell renderer for dropdown items
        comboBox.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                background = if (isSelected) Companion.accentColor.darker() else componentBackgroundColor
                foreground = textColor
                return this
            }
        })

        comboBox.addActionListener {
            property.set(comboBox.selectedItem as E)
            module.saveSettings()
        }
        panel.add(comboBox, BorderLayout.CENTER)
        add(panel)
    }
    
    companion object {
        val accentColor = Color(70, 130, 180) // Steel Blue, example
    }
}
