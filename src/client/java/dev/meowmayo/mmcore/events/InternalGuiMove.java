package dev.meowmayo.mmcore.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class InternalGuiMove {
    public static final Event<InternalGuiMove.Gui> GUI = EventFactory.createArrayBacked(InternalGuiMove.Gui.class,
            listeners -> name -> {
                for (InternalGuiMove.Gui listener : listeners) listener.onGui(name);
            });

    public interface Gui { void onGui(String name); }
}
