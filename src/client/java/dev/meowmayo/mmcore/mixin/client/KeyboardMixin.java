package dev.meowmayo.mmcore.mixin.client;

import dev.meowmayo.mmcore.config.ChatMacro;
import dev.meowmayo.mmcore.config.ChatMacroManager;
import dev.meowmayo.mmcore.utils.ChatUtils;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void keyPress(final long handle, final @KeyEvent.Action int action, final KeyEvent event, CallbackInfo ci) {
        if (action == 1 && Minecraft.getInstance().screen == null) {
            for (ChatMacro m : ChatMacroManager.getMacros()) {
                 if (event.key() == m.key && ((event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0) == m.useShift && ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) == m.useCtrl) {
                     if (m.command.startsWith("/")) {
                         ChatUtils.command(m.command.substring(1));
                     } else {
                         ChatUtils.message(m.command);
                     }
                 }
            }
        }
    }
}
