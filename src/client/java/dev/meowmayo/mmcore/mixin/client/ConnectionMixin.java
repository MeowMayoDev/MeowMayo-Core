package dev.meowmayo.mmcore.mixin.client;

import dev.meowmayo.mmcore.events.MMChatEvent;
import dev.meowmayo.mmcore.events.MMServerTickEvent;
import dev.meowmayo.mmcore.events.MMPacketEvents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "genericsFtw", at = @At("HEAD"))
    private static void onHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        switch (packet) {
            case ClientboundPingPacket ping -> {
                if (ping.getId() != 0) {
                    MMServerTickEvent.TICK.invoker().onTick(ping);
                }
            }
            case ClientboundSystemChatPacket sysChat -> MMChatEvent.SYSTEM.invoker().onSystem(sysChat);
            case ClientboundPlayerChatPacket playerChat -> MMChatEvent.PLAYER.invoker().onPlayer(playerChat);
            case null, default -> MMPacketEvents.RECEIVE.invoker().onReceive(packet);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        MMPacketEvents.SEND.invoker().onSend(packet);
    }
}