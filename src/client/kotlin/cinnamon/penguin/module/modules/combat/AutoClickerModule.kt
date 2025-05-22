package cinnamon.penguin.module.modules.combat

import cinnamon.penguin.module.Category
import cinnamon.penguin.module.Module
import org.lwjgl.glfw.GLFW
import java.awt.Robot
import java.awt.event.InputEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Module for automatically clicking the mouse
 */
class AutoClickerModule : Module(
    name = "AutoClicker",
    description = "Automatically clicks the mouse",
    category = Category.COMBAT,
    keyCode = GLFW.GLFW_KEY_R  // Default key binding is R
) {
    private var robot: Robot? = null
    private var clickerExecutor: ScheduledExecutorService? = null
    
    // Settings
    var clicksPerSecond = 10
    var randomization = 15 // Randomization percentage
    var rightClick = false
    
    init {
        try {
            robot = Robot()
        } catch (e: Exception) {
            println("AutoClickerModule: Failed to initialize Robot")
            e.printStackTrace()
        }
    }
    
    override fun onEnable() {
        if (robot == null) return
        
        clickerExecutor = Executors.newSingleThreadScheduledExecutor()
        
        clickerExecutor?.scheduleAtFixedRate({
            try {
                if (mc.isWindowFocused && mc.currentScreen == null) {
                    // Apply randomization
                    val randomFactor = 1.0 + (Math.random() * 2 - 1) * (randomization / 100.0)
                    
                    // Determine which mouse button to click
                    val button = if (rightClick) InputEvent.BUTTON3_DOWN_MASK else InputEvent.BUTTON1_DOWN_MASK
                    
                    // Perform mouse click
                    robot?.mousePress(button)
                    Thread.sleep(10)
                    robot?.mouseRelease(button)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, (1000.0 / clicksPerSecond).toLong(), TimeUnit.MILLISECONDS)
        
        println("AutoClickerModule: Enabled (CPS: $clicksPerSecond, Randomization: $randomization%)")
    }
    
    override fun onDisable() {
        clickerExecutor?.shutdownNow()
        clickerExecutor = null
        println("AutoClickerModule: Disabled")
    }
}
