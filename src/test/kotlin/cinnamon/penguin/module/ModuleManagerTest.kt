package cinnamon.penguin.module

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.lwjgl.glfw.GLFW
import java.io.File

// Mock MinecraftClient and its runDirectory for testing if needed,
// but for these tests, directly setting ModuleManager.configFile is easier.

class ModuleManagerTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var testConfigFile: File
    private lateinit var originalConfigFile: File

    // Hold a reference to the original modules list if ModuleManager keeps state internally
    // For this test, we will clear and re-register modules to control state.

    @BeforeEach
    fun setUp() {
        testConfigFile = File(tempDir, "test-modules.json")
        originalConfigFile = ModuleManager.configFile // Assuming internal visibility or a setter
        ModuleManager.configFile = testConfigFile

        // Clear any existing modules in ModuleManager to ensure a clean state for each test.
        // This requires exposing a way to clear or manage modules for testing.
        // For simplicity, we'll assume ModuleManager.modules can be cleared or it's reset.
        // A proper implementation might involve a reset method in ModuleManager.
        ModuleManager.getModules().forEach { module ->
            // Need a way to unregister modules or reset ModuleManager state.
            // This is a simplification. In a real scenario, ModuleManager would need a reset method.
        }
        // Hacky way to reset modules for now, ideally ModuleManager.reset()
        val modulesField = ModuleManager::class.java.getDeclaredField("modules")
        modulesField.isAccessible = true
        (modulesField.get(ModuleManager) as MutableList<Module>).clear()
    }

    @AfterEach
    fun tearDown() {
        ModuleManager.configFile = originalConfigFile
        if (testConfigFile.exists()) {
            testConfigFile.delete()
        }
        // Clean up modules again
        val modulesField = ModuleManager::class.java.getDeclaredField("modules")
        modulesField.isAccessible = true
        (modulesField.get(ModuleManager) as MutableList<Module>).clear()
    }

    private fun registerTestModule(module: Module) {
        // ModuleManager.registerModule(module) // if public
        // Using reflection if not public for testing purposes
        val registerModuleMethod = ModuleManager::class.java.getDeclaredMethod("registerModule", Module::class.java)
        registerModuleMethod.isAccessible = true
        registerModuleMethod.invoke(ModuleManager, module)
    }
    
    private fun reinitializeModuleManagerState() {
        // This function would call methods on ModuleManager to mimic a clean startup for loading tests
        // For example, if loadModuleConfiguration is usually called in init:
        // ModuleManager.init() // but this might have other side effects (like event registration)
        // For now, we directly call loadModuleConfiguration in tests after setup.
    }


    @Test
    fun `save and load configuration should restore keyCode`() {
        val module1 = TestModule("Module1", keyCode = GLFW.GLFW_KEY_X)
        val module2 = TestModule("Module2", keyCode = GLFW.GLFW_KEY_Y)
        val module3 = TestModule("Module3", keyCode = GLFW.GLFW_KEY_UNKNOWN)

        registerTestModule(module1)
        registerTestModule(module2)
        registerTestModule(module3)

        ModuleManager.saveModuleConfiguration()
        assertTrue(testConfigFile.exists(), "Config file should be created after save.")
        assertTrue(testConfigFile.length() > 0, "Config file should not be empty.")

        // Clear current modules to simulate loading into a fresh ModuleManager state
        (ModuleManager::class.java.getDeclaredField("modules").get(ModuleManager) as MutableList<Module>).clear()
        
        // Re-register modules with default state before loading, so we can see them updated
        val freshModule1 = TestModule("Module1", keyCode = GLFW.GLFW_KEY_UNKNOWN)
        val freshModule2 = TestModule("Module2", keyCode = GLFW.GLFW_KEY_UNKNOWN)
        val freshModule3 = TestModule("Module3", keyCode = GLFW.GLFW_KEY_Z) // different default
        registerTestModule(freshModule1)
        registerTestModule(freshModule2)
        registerTestModule(freshModule3)

        // Directly call load. In real app, init() might do this.
        val loadModuleConfigMethod = ModuleManager::class.java.getDeclaredMethod("loadModuleConfiguration")
        loadModuleConfigMethod.isAccessible = true
        loadModuleConfigMethod.invoke(ModuleManager)


        assertEquals(GLFW.GLFW_KEY_X, ModuleManager.getModule("Module1")?.keyCode, "Module1 keyCode not restored.")
        assertEquals(GLFW.GLFW_KEY_Y, ModuleManager.getModule("Module2")?.keyCode, "Module2 keyCode not restored.")
        assertEquals(GLFW.GLFW_KEY_UNKNOWN, ModuleManager.getModule("Module3")?.keyCode, "Module3 keyCode not restored to UNKNOWN.")
    }

    @Test
    fun `load configuration when file does not exist should use defaults and create file`() {
        if (testConfigFile.exists()) testConfigFile.delete()

        val defaultModule = TestModule("DefaultModule", keyCode = GLFW.GLFW_KEY_K)
        registerTestModule(defaultModule) // Register with its default key

        // Attempt to load (which should fail, use defaults, then save)
        val loadModuleConfigMethod = ModuleManager::class.java.getDeclaredMethod("loadModuleConfiguration")
        loadModuleConfigMethod.isAccessible = true
        loadModuleConfigMethod.invoke(ModuleManager)

        // Check that the module still has its default key
        assertEquals(GLFW.GLFW_KEY_K, ModuleManager.getModule("DefaultModule")?.keyCode, "DefaultModule should retain its default key.")
        assertTrue(testConfigFile.exists(), "Config file should be created with default settings if it didn't exist.")
        
        // Verify content of the newly created file
        (ModuleManager::class.java.getDeclaredField("modules").get(ModuleManager) as MutableList<Module>).clear()
        val freshDefaultModule = TestModule("DefaultModule", keyCode = GLFW.GLFW_KEY_UNKNOWN) // different key
        registerTestModule(freshDefaultModule)
        
        loadModuleConfigMethod.invoke(ModuleManager) // Load what was just saved
        assertEquals(GLFW.GLFW_KEY_K, ModuleManager.getModule("DefaultModule")?.keyCode, "DefaultModule key not saved/loaded correctly after initial creation.")

    }

    @Test
    fun `save configuration with no modules should create empty json array`() {
        // Modules list is already cleared in setUp

        ModuleManager.saveModuleConfiguration()
        assertTrue(testConfigFile.exists(), "Config file should be created even with no modules.")
        assertEquals("[]", testConfigFile.readText().trim(), "Config file should contain an empty JSON array.")
    }
    
    @Test
    fun `load configuration should restore enabled state`() {
        val module1 = TestModule("StateModule1")
        module1.enable() // Enabled, default key
        val module2 = TestModule("StateModule2", keyCode = GLFW.GLFW_KEY_S) 
        // Disabled (default), specific key

        registerTestModule(module1)
        registerTestModule(module2)

        ModuleManager.saveModuleConfiguration()

        // Clear current modules and re-register with opposite states
        (ModuleManager::class.java.getDeclaredField("modules").get(ModuleManager) as MutableList<Module>).clear()
        val freshModule1 = TestModule("StateModule1") // default: disabled
        val freshModule2 = TestModule("StateModule2", keyCode = GLFW.GLFW_KEY_S) 
        freshModule2.enable() // set to enabled to see if loading disables it

        registerTestModule(freshModule1)
        registerTestModule(freshModule2)
        
        val loadModuleConfigMethod = ModuleManager::class.java.getDeclaredMethod("loadModuleConfiguration")
        loadModuleConfigMethod.isAccessible = true
        loadModuleConfigMethod.invoke(ModuleManager)

        assertTrue(ModuleManager.getModule("StateModule1")!!.enabled, "StateModule1 should be enabled after loading.")
        assertFalse(ModuleManager.getModule("StateModule2")!!.enabled, "StateModule2 should be disabled after loading.")
        assertEquals(GLFW.GLFW_KEY_S, ModuleManager.getModule("StateModule2")?.keyCode)
    }
}
