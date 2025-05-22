package cinnamon.penguin.input

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import cinnamon.penguin.gui.MainWindow
import javax.swing.SwingUtilities

object KeyboardHandler {
    private lateinit var openGuiKey: KeyBinding
    private var guiWindow: MainWindow? = null
    private var isGuiOpen = false

    fun register() {
        println("KeyboardHandler: Registering key binding...")
        
        // Register the key binding for F7
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.penguinclient.open_gui_v2", // Changed ID to avoid conflicts with old versions
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,
                "category.penguinclient.general"
            )
        )
        
        println("KeyboardHandler: Key binding registered for F7")

        // Register the tick event to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (openGuiKey.wasPressed()) {
                println("KeyboardHandler: F7 was pressed!")
                
                if (!isGuiOpen) {
                    println("KeyboardHandler: Opening GUI window")
                    // Create and show window on the Swing EDT
                    SwingUtilities.invokeLater {
                        try {
                            if (guiWindow == null) {
                                guiWindow = MainWindow()
                                println("KeyboardHandler: Created new MainWindow")
                            }
                            guiWindow?.isVisible = true
                            isGuiOpen = true
                            println("KeyboardHandler: Window should now be visible")
                        } catch (e: Exception) {
                            println("KeyboardHandler: Error showing window")
                            e.printStackTrace()
                        }
                    }
                } else {
                    println("KeyboardHandler: Hiding GUI window")
                    // Hide window on the Swing EDT
                    SwingUtilities.invokeLater {
                        try {
                            guiWindow?.isVisible = false
                            isGuiOpen = false
                            println("KeyboardHandler: Window should now be hidden")
                        } catch (e: Exception) {
                            println("KeyboardHandler: Error hiding window")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        
        println("KeyboardHandler: Tick event registered successfully")
    }
}
