package dev.meowmayo.mmcore.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

public class MMClientEvents {
    public static final Event<Attack> ATTACK = EventFactory.createArrayBacked(Attack.class,
            listeners -> client -> {
                for (Attack listener : listeners) listener.onAttack(client);
            });

    public static final Event<Use> USE = EventFactory.createArrayBacked(Use.class,
            listeners -> client -> {
                for (Use listener : listeners) listener.onUse(client);
            });

    public interface Attack { void onAttack(Minecraft client); }
    public interface Use { void onUse(Minecraft client); }
}
