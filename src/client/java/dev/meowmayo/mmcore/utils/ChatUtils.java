package dev.meowmayo.mmcore.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {
    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    public static void allChat(String message) {
        if (player() != null) player().connection.sendChat("/ac " + message);
    }

    public static void guildChat(String message) {
        if (player() != null) player().connection.sendChat("/gc " + message);
    }

    public static void partyChat(String message) {
        if (player() != null) player().connection.sendChat("/pc " + message);
    }

    public static void command(String command) {
        if (player() != null) player().connection.sendCommand(command);
    }

    public static void message(String message) {
        if (player() != null) player().connection.sendChat(message);
    }

    public static void system(String message) {
        if (player() == null) return;
        Minecraft.getInstance().execute(() -> {
            MutableComponent prefix = Component.literal("")
                    .append(Component.literal("Meow").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .append(Component.literal("Mayo").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .append(Component.literal(" > ").withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(message).withStyle(ChatFormatting.RESET, ChatFormatting.WHITE));

            player().sendSystemMessage(prefix);
        });
    }

    public static boolean isSystemMessage(String message) {
        return (!(message.startsWith("Party >") || message.startsWith("Guild >") || message.startsWith("From ") || message.startsWith("To ")));
    }


    private static final Pattern rank = Pattern.compile("\\[.{3,8}]");
    public static String stripRank(String name) {
        Matcher removed = rank.matcher(name);
        return removed.replaceAll("").replace(" ", "");
    }

    public static String formatTime(double seconds) {
        if (seconds < 60.0) {
            return String.format("%.2fs", seconds);
        }

        double secs = seconds % 60;
        int minutes = (int) Math.floor(seconds / 60.0);

        if (minutes > 60) {
            int hours = minutes / 60;
            minutes = minutes % 60;

            if (hours > 24) {
                int days = hours / 24;
                hours = hours % 24;
                return String.format("%dd %dh %dm %.2fs", days, hours, minutes, secs);
            }

            return String.format("%dh %dm %.2fs", hours, minutes, secs);
        }

        return String.format("%dm %.2fs", minutes, secs);
    }
}