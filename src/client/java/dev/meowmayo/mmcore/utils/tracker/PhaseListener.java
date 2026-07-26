package dev.meowmayo.mmcore.utils.tracker;

public abstract class PhaseListener {
    Events event;

    protected PhaseListener(Events event) {
        this.event = event;
    }

    boolean active = false;

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
