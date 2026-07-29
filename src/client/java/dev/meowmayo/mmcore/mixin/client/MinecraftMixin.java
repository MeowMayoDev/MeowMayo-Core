package dev.meowmayo.mmcore.mixin.client;

import dev.meowmayo.mmcore.events.MMClientEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    private boolean attackWasDown = false;
    private boolean useWasDown = false;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void onInput(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (client.player == null || client.gui.screen() != null) return;

        boolean attackIsDown = client.options.keyAttack.isDown();
        if (attackIsDown && !attackWasDown) {
            MMClientEvents.ATTACK.invoker().onAttack(client);
        }
        attackWasDown = attackIsDown;

        boolean useIsDown = client.options.keyUse.isDown();
        if (useIsDown && !useWasDown) {
            MMClientEvents.USE.invoker().onUse(client);
        }
        useWasDown = useIsDown;
    }
}