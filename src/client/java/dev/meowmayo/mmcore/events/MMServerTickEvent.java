package dev.meowmayo.mmcore.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.common.ClientboundPingPacket;

public class MMServerTickEvent {
    public static final Event<Tick> TICK = EventFactory.createArrayBacked(Tick.class,
            listeners -> packet -> {
                for (MMServerTickEvent.Tick listener : listeners) listener.onTick(packet);
            });

    public interface Tick { void onTick(ClientboundPingPacket packet); }
}