package dev.meowmayo.mmcore.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ChatMacroManager {
    private static File configFile;
    private static ArrayList<ChatMacro> macros = new ArrayList<>();

    private static long lastWriteTime = 0;
    private static ScheduledFuture<?> scheduledWrite;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long WRITE_DELAY_MS = 10_000;

    public static void init() {
        File modSubDir = FabricLoader.getInstance().getConfigDir().resolve("meowmayo").toFile();
        if (!modSubDir.exists()) modSubDir.mkdirs();

        configFile = new File(modSubDir, "macros.meow");

        read();
    }

    public static void addMacro(ChatMacro macro) {
        macros.add(macro);
        scheduleWrite();
    }

    public static void removeMacro(ChatMacro macro) {
        macros.remove(macro);
        scheduleWrite();
    }

    public static ArrayList<ChatMacro> getMacros() {
        return macros;
    }

    public static void setMacros(ArrayList<ChatMacro> macros) {
        ChatMacroManager.macros = macros;
    }

    public static synchronized void scheduleWrite() {
        long now = System.currentTimeMillis();
        long timeSinceLastWrite = now - lastWriteTime;

        if (timeSinceLastWrite >= WRITE_DELAY_MS) {
            write();
        } else {
            if (scheduledWrite != null && !scheduledWrite.isDone()) {
                scheduledWrite.cancel(false);
            }
            long delay = WRITE_DELAY_MS - timeSinceLastWrite;
            scheduledWrite = scheduler.schedule(ChatMacroManager::write, delay, TimeUnit.MILLISECONDS);
        }
    }

    public static synchronized void write() {
        lastWriteTime = System.currentTimeMillis();
        File tempFile = new File(configFile.getAbsolutePath() + ".tmp");

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (ChatMacro m : macros) {
                    writer.write(m.key + ":;:" + m.useShift + ":;:" + m.useCtrl + ":;:" +m.command + "\n");
                }
            }

            Files.move(tempFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            if (tempFile.exists()) tempFile.delete();
        }
    }

    private static void read() {
        if (!configFile.exists()) return;
        ArrayList<ChatMacro> loaded = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":;:");
                if (parts.length != 4) continue;
                try {
                    int key = Integer.parseInt(parts[0]);
                    boolean shift = Boolean.parseBoolean(parts[1]);
                    boolean ctrl = Boolean.parseBoolean(parts[2]);
                    String cmd = parts[3];
                    loaded.add(new ChatMacro(key, shift, ctrl, cmd));
                } catch (NumberFormatException ignored) {}
            }
            setMacros(loaded);
        } catch (IOException ignored) {}
    }
}
