package dev.meowmayo.mmcore.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.Packet;

public class MMPacketEvents {
    public static final Event<Receive> RECEIVE = EventFactory.createArrayBacked(Receive.class,
            listeners -> packet -> {
                for (Receive listener : listeners) listener.onReceive(packet);
            });

    public static final Event<Send> SEND = EventFactory.createArrayBacked(Send.class,
            listeners -> packet -> {
                for (Send listener : listeners) listener.onSend(packet);
            });

    public interface Receive { void onReceive(Packet<?> packet); }
    public interface Send { void onSend(Packet<?> packet); }
}