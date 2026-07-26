package dev.meowmayo.mmcore.utils.tracker.listeners;

import dev.meowmayo.mmcore.utils.tracker.Events;
import dev.meowmayo.mmcore.utils.tracker.PhaseListener;

public abstract class ChatMatchListener extends PhaseListener {
    final String check;
    Events events;

    public ChatMatchListener(String check, Events event) {
        super(event);
        this.check = check;
    }

    public abstract void onChatMessage(String msg);
}
