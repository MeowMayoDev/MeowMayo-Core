package dev.meowmayo.mmcore.config;

import dev.meowmayo.mmcore.config.settings.HudElementSetting;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final List<MeowModule> MODULES = new ArrayList<>();

    public static void register(MeowModule module) {
        MODULES.add(module);
    }

    public static List<MeowModule> getModules() {
        return MODULES;
    }

    public static List<ConfigSettings> getSettings() {
        List<ConfigSettings> allConfigs = new ArrayList<>();
        for (MeowModule module : MODULES) {
            allConfigs.add(module.getConfig());
        }

        return allConfigs;
    }

    public static List<HudElementSetting> getLocations() {
        List<HudElementSetting> allHuds = new ArrayList<>();
        for (MeowModule module : MODULES) {
            allHuds.addAll(module.getHud().getElements());
        }

        return allHuds;
    }

    public static boolean edit(String title, Object value) {
        List<ConfigSettings> allConfigs = getSettings();

        for (ConfigSettings settings : allConfigs) {
            if (settings.edit(title, value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean editLocation(String title, int x, int y) {
        List<HudManager> allHuds = new ArrayList<>();
        for (MeowModule module : MODULES) {
            allHuds.add(module.getHud());
        }

        for (HudManager settings : allHuds) {
            if (settings.edit(title, x, y)) {
                return true;
            }
        }

        return false;
    }

    public static boolean editScale(String title, float scale) {
        List<HudManager> allHuds = new ArrayList<>();
        for (MeowModule module : MODULES) {
            allHuds.add(module.getHud());
        }

        for (HudManager settings : allHuds) {
            if (settings.edit(title, scale)) {
                return true;
            }
        }

        return false;
    }
}