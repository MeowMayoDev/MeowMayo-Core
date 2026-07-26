package dev.meowmayo.mmcore.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class InternalConfigChange {
    public static final Event<InternalConfigChange.Config> CONFIG = EventFactory.createArrayBacked(InternalConfigChange.Config.class,
            listeners -> name -> {
                for (InternalConfigChange.Config listener : listeners) listener.onConfig(name);
            });

    public interface Config { void onConfig(String name); }
}
