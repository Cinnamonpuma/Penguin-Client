package cinnamon.penguin.mixin.client

import cinnamon.penguinclient.hotkey.HotkeyManager
import net.minecraft.client.Keyboard
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Keyboard::class)
abstract class KeyboardMixin {

    @Inject(
        method = ["onKey(JIIII)V"], // Method signature for Keyboard.onKey
        at = [At("HEAD")], // Inject at the beginning of the method
        cancellable = false // We are just listening, not cancelling
    )
    private fun onKeyCallback(window: Long, key: Int, scancode: Int, action: Int, modifiers: Int, ci: CallbackInfo) {
        // We are interested in key press events, not releases or repeats
        if (action == GLFW.GLFW_PRESS) {
            HotkeyManager.onKeyPress(key)
        }
    }
}
