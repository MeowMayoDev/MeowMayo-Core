package dev.meowmayo.mmcore.gui;

import dev.meowmayo.mmcore.config.MeowModule;
import dev.meowmayo.mmcore.config.ModConfig;
import dev.meowmayo.mmcore.config.settings.*;
import dev.meowmayo.mmcore.gui.SettingsRow.*;
import dev.meowmayo.mmcore.gui.SettingsRow.ISettingComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SettingsGui extends Screen {
    private int selectedCategory = 0;
    private float scrollAmount = 0;
    private int maxScroll = 0;

    private TextField searchField;

    private final List<ISettingComponent> activeRows = new ArrayList<>();

    public SettingsGui() {
        super(Component.literal("Settings"));
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
            this.minecraft.setScreen(new MainGui());
        }).bounds(width - 25, 5, 20, 20).build());

        int y = 35;
        List<MeowModule> modules = ModConfig.getModules();
        for (int i = 0; i < modules.size(); i++) {
            int finalI = i;
            this.addRenderableWidget(Button.builder(Component.literal(modules.get(i).getName()), button -> {
                selectedCategory = finalI;

                searchField.setText("");
                searchField.setFocused(false);

                this.scrollAmount = 0;

                init();
            }).bounds(5, y, 90, 20).build());
            y += 30;
        }

        this.searchField = new TextField(5, 5, 200, 20);
        this.searchField.setFocused(true);

        rebuildSettingsList();
    }

    private void rebuildSettingsList() {
        activeRows.clear();
        this.maxScroll = 0;

        SettingsRow.currentQuery = searchField.getText();
        String query = searchField.getText().toLowerCase();

        int totalHeight = 40;

        List<MeowModule> categoriesToSearch = new ArrayList<>();

        if (!query.isEmpty()) {
            categoriesToSearch.addAll(ModConfig.getModules());
        } else {
            categoriesToSearch.add(ModConfig.getModules().get(selectedCategory));
        }
        for (MeowModule cat : categoriesToSearch) {
            if (!query.isEmpty()) {
                activeRows.add(new HeaderRow("Category: " + cat.getName()));
                totalHeight += 20;
            }

            // Filter Misc Settings
            for (Setting s : cat.getMiscSettings()) {
                if (s.getTitle().toLowerCase().contains(query)) {
                    activeRows.add(createRow(s));
                    totalHeight += 85;
                }
            }

            // Filter Subcategories
            for (SettingSubcategory sub : cat.getSubcategories()) {
                List<ISettingComponent> tempResults = new ArrayList<>();
                for (Setting s : sub.getSettings()) {
                    if (s.getTitle().toLowerCase().contains(query)) {
                        tempResults.add(createRow(s));
                    }
                }

                if (!tempResults.isEmpty() || sub.getName().toLowerCase().contains(query)) {
                    activeRows.add(new HeaderRow(sub.getName()));
                    totalHeight += 20;
                    activeRows.addAll(tempResults);
                    totalHeight += (tempResults.size() * 85);
                }
            }
        }

        this.maxScroll = Math.max(0, totalHeight - this.height);
    }

    private ISettingComponent createRow(Setting s) {
        if (s instanceof ToggleSetting) return new ToggleRow((ToggleSetting) s);
        if (s instanceof IntSliderSetting || s instanceof FloatSliderSetting) return new SliderRow(s);
        if (s instanceof TextSetting) return new TextRow((TextSetting) s);
        return new HeaderRow(s.getTitle()); // Fallback
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        String title = "MeowMayo Settings";

        context.centeredText(font, title, this.width / 2, 12, 0xFFFFFFFF);

        searchField.render(context);

        context.fill(10, 30, this.width - 10, 31, 0xFFAAAAAA);
        context.fill(100, 30, 101, height - 10, 0xFFAAAAAA);

        int x1 = 105;
        int y1 = 40;
        int x2 = width - 10;
        int y2 = height - 10;

        context.enableScissor(x1, y1, x2, y2);

        int currY = 40 - (int) scrollAmount;
        for (ISettingComponent row : activeRows) {
            if (currY + row.getHeight() > 30 && currY < height) {
                row.render(context, font,110, currY, width - 120, mouseX, mouseY);
            }
            currY += row.getHeight();
        }

        context.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        scrollAmount -= (float) (g * 10.0F);
        scrollAmount = Math.clamp(scrollAmount, 0, maxScroll);
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        String textBefore = searchField.getText();
        searchField.mouseClicked(event.x(), event.y(), event.button());
        if (!searchField.getText().equals(textBefore)) {
            scrollAmount = 0;
            rebuildSettingsList();
        }

        int currY = 40 - (int) scrollAmount;

        for (ISettingComponent row : activeRows) {
            if (event.y() >= currY && event.y() <= currY + row.getHeight()) {
                row.mouseDown(event.x(), event.y(), width, height, event.button());
                break;
            }
            currY += row.getHeight();
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        String textBefore = searchField.getText();
        searchField.mouseClicked(event.x(), event.y(), event.button());
        if (!searchField.getText().equals(textBefore)) {
            scrollAmount = 0;
            rebuildSettingsList();
        }

        int currY = 40 - (int) scrollAmount;

        for (ISettingComponent row : activeRows) {
            if (event.y() >= currY && event.y() <= currY + row.getHeight()) {
                row.mouseUp(event.x(), event.y(), width, height, event.button());
                break;
            }
            currY += row.getHeight();
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        String oldText = searchField.getText();
        searchField.keyPressed(keyEvent.key());

        if (!searchField.getText().equals(oldText)) {
            scrollAmount = 0;
            rebuildSettingsList();
        }

        if (keyEvent.key() == 1) this.minecraft.setScreen(new MainGui());

        for (ISettingComponent row : activeRows) {
            row.keyPressed(keyEvent.key());
        }

        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        String oldText = searchField.getText();
        searchField.charTyped((char) characterEvent.codepoint());

        if (!searchField.getText().equals(oldText)) {
            scrollAmount = 0;
            rebuildSettingsList();
        }

        for (ISettingComponent row : activeRows) {
            row.charTyped((char) characterEvent.codepoint());
        }

        return super.charTyped(characterEvent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
