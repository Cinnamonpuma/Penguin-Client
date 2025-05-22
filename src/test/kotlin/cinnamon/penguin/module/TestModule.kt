package cinnamon.penguin.module

import org.lwjgl.glfw.GLFW

// A concrete implementation of Module for testing purposes
class TestModule(
    name: String = "TestModule",
    description: String = "A test module",
    category: Category = Category.COMBAT,
    keyCode: Int = GLFW.GLFW_KEY_UNKNOWN
) : Module(name, description, category, keyCode) {

    var onEnableCalled = false
    var onDisableCalled = false
    var onTickCalled = false

    override fun onEnable() {
        super.onEnable()
        onEnableCalled = true
    }

    override fun onDisable() {
        super.onDisable()
        onDisableCalled = true
    }

    override fun onTick() {
        super.onTick()
        onTickCalled = true
    }

    // Helper to reset flags for multiple tests
    fun resetTestFlags() {
        onEnableCalled = false
        onDisableCalled = false
        onTickCalled = false
    }
}
