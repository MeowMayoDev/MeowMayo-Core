package dev.meowmayo.mmcore.config;

import dev.meowmayo.mmcore.config.settings.*;
import dev.meowmayo.mmcore.events.InternalConfigChange;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;

public class ConfigSettings {
    private final List<Setting> allSettings = new ArrayList<>(); // this is for grabbing settings and storage

    private final File configFile;

    private long lastWriteTime = 0;
    private ScheduledFuture<?> scheduledWrite;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long WRITE_DELAY_MS = 10_000;

    private static boolean initialized = false;

    public ConfigSettings(File parentDir, String fileName) {
        this.configFile = new File(parentDir, fileName + ".meow");
    }

    public void register(Setting setting) {
        allSettings.add(setting);
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

        initialized = true;
    }

    public List<Setting> getMiscSettings() {
        List<Setting> misc = new ArrayList<>();

        allSettings.stream()
                .filter(item -> item.getSubcategory().isEmpty())
                .forEach(misc::add);

        misc.sort(Comparator.comparing(Setting::getTitle, String.CASE_INSENSITIVE_ORDER));

        return misc;
    }

    public List<SettingSubcategory> getSortedSubcategories() {
        Map<String, SettingSubcategory> subMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Setting s : allSettings) {
            String subName = s.getSubcategory();

            if (!subName.isEmpty()) {
                subMap.computeIfAbsent(subName, SettingSubcategory::new).addSetting(s);
            }
        }

        for (SettingSubcategory sub : subMap.values()) {
            sub.getSettings().sort(Comparator.comparing(Setting::getTitle, String.CASE_INSENSITIVE_ORDER));
        }

        return new ArrayList<>(subMap.values());
    }

    public synchronized void scheduleWrite() {
        long now = System.currentTimeMillis();
        long timeSinceLastWrite = now - lastWriteTime;

        if (timeSinceLastWrite >= WRITE_DELAY_MS) {
            write();
        } else {
            // cancel the previous pending save and schedule a new one
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
                writer.write("// MeowMayo Config\n");
                writer.write("// Do not edit these files unless you know what you are doing\n");
                writer.write("// MeowMayo Development is not responsible for lost config settings\n");
                for (Setting s : allSettings) {
                    int type = 0;
                    if (s instanceof ToggleSetting) type = 1;
                    else if (s instanceof TextSetting) type = 2;
                    else if (s instanceof IntSliderSetting) type = 3;
                    else if (s instanceof FloatSliderSetting) type = 4;

                    writer.write(s.getTitle() + ":" + type + ":" + s.getValue() + "\n");
                }
            }

            // atomic move for thread safety and corruption prevention
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
            int index = 0;
            while ((line = reader.readLine()) != null && index < allSettings.size()) {
                if (line.startsWith("//")) continue;

                String[] parts = line.split(":", 3);
                if (parts.length < 3) continue;

                Setting s = allSettings.get(index++);
                try {
                    int type = Integer.parseInt(parts[1]);
                    switch (type) {
                        case 1 -> s.setValue(Boolean.parseBoolean(parts[2]));
                        case 2 -> s.setValue(parts[2]);
                        case 3 -> s.setValue(Integer.parseInt(parts[2]));
                        case 4 -> s.setValue(Float.parseFloat(parts[2]));
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException ignored) {}
    }

    public boolean edit(String title, Object value) {
        for (Setting s : allSettings) {
            if (s.getTitle().equals(title)) {
                s.setValue(value);
                InternalConfigChange.CONFIG.invoker().onConfig(title);
                scheduleWrite();
                return true;
            }
        }
        return false;
    }

    public Setting getSetting(String name) {
        for (Setting s : allSettings) {
            if (s.getTitle().equals(name)) {
                return s;
            }
        }
        return null;
    }
}