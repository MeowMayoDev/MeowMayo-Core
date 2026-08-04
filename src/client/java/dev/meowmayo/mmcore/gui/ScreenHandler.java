package dev.meowmayo.mmcore.gui;

import dev.meowmayo.mmcore.gui.componenets.MeowScreen;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ScreenHandler {
    private static final List<Supplier<MeowScreen>> screenFactory = new ArrayList<>();

    public static void openMain() {
        Minecraft.getInstance().gui.setScreen(new MainGui());

    }

    // ScreenHandler.addScreen(Screen::new);
    public static void addScreen(Supplier<MeowScreen> supplier) {
        screenFactory.add(supplier);
    }

    public static List<Supplier<MeowScreen>> getScreens() {
        return screenFactory;
    }
}
