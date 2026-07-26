package dev.meowmayo.mmcore.config;

import dev.meowmayo.mmcore.config.settings.HudElementSetting;
import dev.meowmayo.mmcore.events.InternalGuiMove;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;

public class HudManager {
    private final List<HudElementSetting> expected = new ArrayList<>();
    private final File configFile;

    private long lastWriteTime = 0;
    private ScheduledFuture<?> scheduledWrite;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long WRITE_DELAY_MS = 10_000;

    public HudManager(File parentDir, String fileName) {
        this.configFile = new File(parentDir, fileName + ".meow");
    }

    public void register(HudElementSetting setting) {
        expected.add(setting);
    }

    public void init() {
        read();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scheduledWrite != null && !scheduledWrite.isDone()) {
                scheduledWrite.cancel(false);
            }
            write();
            scheduler.shutdownNow();
        }));
    }

    public synchronized void scheduleWrite() {
        long now = System.currentTimeMillis();
        long timeSinceLastWrite = now - lastWriteTime;

        if (timeSinceLastWrite >= WRITE_DELAY_MS) {
            write();
        } else {
            if (scheduledWrite != null && !scheduledWrite.isDone()) {
                scheduledWrite.cancel(false);
            }
            long delay = WRITE_DELAY_MS - timeSinceLastWrite;
            scheduledWrite = scheduler.schedule(this::write, delay, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void write() {
        lastWriteTime = System.currentTimeMillis();
        File tempFile = new File(configFile.getAbsolutePath() + ".tmp");

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (HudElementSetting s : expected) {
                    writer.write(s.getTitle() + ":" + s.getX() + ":" + s.getY() + ":" + s.getScale() + "\n");
                }
            }

            Files.move(tempFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            if (tempFile.exists()) tempFile.delete();
        }
    }

    private void read() {
        if (!configFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length != 4) continue;

                for (HudElementSetting s : expected) {
                    if (s.getTitle().equals(parts[0])) {
                        try {
                            s.setX(Integer.parseInt(parts[1]));
                            s.setY(Integer.parseInt(parts[2]));
                            s.setScale(Float.parseFloat(parts[3]));
                        } catch (NumberFormatException ignored) {}
                        break;
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    public boolean edit(String title, float scale) {
        for (HudElementSetting s : expected) {
            if (s.getTitle().equals(title)) {
                s.setScale(scale);
                InternalGuiMove.GUI.invoker().onGui(title);
                scheduleWrite();
                return true;
            }
        }

        return false;
    }

    public boolean edit(String title, int x, int y) {
        for (HudElementSetting s : expected) {
            if (s.getTitle().equals(title)) {
                s.setX(x);
                s.setY(y);
                InternalGuiMove.GUI.invoker().onGui(title);
                scheduleWrite();
                return true;
            }
        }

        return false;
    }

    public HudElementSetting getLocation(String name) {
        for (HudElementSetting s : expected) {
            if (s.getTitle().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public List<HudElementSetting> getElements() {
        return expected;
    }
}