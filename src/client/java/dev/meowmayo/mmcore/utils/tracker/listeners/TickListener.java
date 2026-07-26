package dev.meowmayo.mmcore.utils.tracker.listeners;

import dev.meowmayo.mmcore.utils.tracker.Events;
import dev.meowmayo.mmcore.utils.tracker.PhaseListener;

public class TickListener extends PhaseListener {
    private final Runnable onTick; // Code to run each tick

    protected TickListener(Events event, Runnable onTick) {
        super(event);
        this.onTick = onTick;
    }

    public void tick() {
        if (!isActive()) return;
        if (onTick != null) {
            onTick.run();
        }
    }
}

