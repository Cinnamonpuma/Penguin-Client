package cinnamon.penguin.module

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW

class ModuleTest {

    private lateinit var testModule: TestModule

    @BeforeEach
    fun setUp() {
        // Create a new TestModule before each test
        // This is important because KeyBindingHelper.registerKeyBinding might have global effects
        // or side effects if not handled carefully. For unit testing module state,
        // re-initializing is safer.
        testModule = TestModule(name = "SpecificTestModule", keyCode = GLFW.GLFW_KEY_A)
    }

    @Test
    fun `setKey should update keyCode property`() {
        val newKeyCode = GLFW.GLFW_KEY_B
        testModule.setKey(newKeyCode)
        assertEquals(newKeyCode, testModule.keyCode, "keyCode should be updated to the new key.")
    }

    @Test
    fun `setKey with UNKNOWN should update keyCode property to UNKNOWN`() {
        testModule.setKey(GLFW.GLFW_KEY_UNKNOWN)
        assertEquals(GLFW.GLFW_KEY_UNKNOWN, testModule.keyCode, "keyCode should be updated to UNKNOWN.")
    }

    @Test
    fun `initial keyCode should be respected`() {
        assertEquals(GLFW.GLFW_KEY_A, testModule.keyCode, "Initial keyCode should be A.")
    }

    @Test
    fun `setKey multiple times should reflect the last set key`() {
        testModule.setKey(GLFW.GLFW_KEY_C)
        testModule.setKey(GLFW.GLFW_KEY_D)
        assertEquals(GLFW.GLFW_KEY_D, testModule.keyCode, "keyCode should reflect the last key set.")
    }

    @Test
    fun `wasKeybindPressed should return false if key is UNKNOWN`() {
        // This test focuses on the guard clause in wasKeybindPressed
        // It does not test the actual KeyBinding.wasPressed() logic
        val moduleWithUnknownKey = TestModule(keyCode = GLFW.GLFW_KEY_UNKNOWN)
        assertEquals(false, moduleWithUnknownKey.wasKeybindPressed(), "wasKeybindPressed should be false if keyCode is UNKNOWN.")
    }

    // Note: Testing the actual registration logic of KeyBindingHelper.registerKeyBinding
    // and the behavior of keyBinding.wasPressed() is more of an integration test
    // and would require a more complex setup, potentially with Minecraft environment mocking.
    // These tests focus on the state management within the Module class itself.
}
