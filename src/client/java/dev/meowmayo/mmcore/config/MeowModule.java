package dev.meowmayo.mmcore.config;

import dev.meowmayo.mmcore.config.settings.HudElementSetting;
import dev.meowmayo.mmcore.config.settings.Setting;
import dev.meowmayo.mmcore.config.settings.SettingSubcategory;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.List;

public class MeowModule {
    private final String name;
    private final ConfigSettings config;
    private final HudManager hud;

    private List<Setting> miscSettings;
    private List<SettingSubcategory> subcategories;

    public List<Setting> getMiscSettings() { return miscSettings; }
    public List<SettingSubcategory> getSubcategories() { return subcategories; }

    public void register(HudElementSetting location) {
        hud.register(location);
    }

    public MeowModule(String name) {
        this.name = name;
        File modSubDir = FabricLoader.getInstance().getConfigDir().resolve("meowmayo").toFile();
        if (!modSubDir.exists()) modSubDir.mkdirs();

        this.config = new ConfigSettings(modSubDir, name.toLowerCase() + "_config");
        this.hud = new HudManager(modSubDir, name.toLowerCase() + "_hud");
    }

    public void init() {
        this.config.init();
        this.hud.init();

        miscSettings = this.config.getMiscSettings();
        subcategories = this.config.getSortedSubcategories();
    }

    public String getName() { return name; }
    public ConfigSettings getConfig() { return config; }
    public HudManager getHud() { return hud; }
}