package dev.meowmayo.mmcore.utils;

import java.util.Timer;
import java.util.TimerTask;

public class DelayUtils {
    public static void scheduleTask(Runnable r, long delayMs) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, delayMs);
    }
}
