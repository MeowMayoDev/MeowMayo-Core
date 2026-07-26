package dev.meowmayo.mmcore.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class MMChatEvent {
    public static final Event<MMChatEvent.System> SYSTEM = EventFactory.createArrayBacked(MMChatEvent.System.class,
            listeners -> packet -> {
                for (MMChatEvent.System listener : listeners) listener.onSystem(packet);
            });
    public static final Event<MMChatEvent.Player> PLAYER = EventFactory.createArrayBacked(MMChatEvent.Player.class,
            listeners -> packet -> {
                for (MMChatEvent.Player listener : listeners) listener.onPlayer(packet);
            });

    public interface System { void onSystem(ClientboundSystemChatPacket packet); }
    public interface Player { void onPlayer(ClientboundPlayerChatPacket packet); }
}
