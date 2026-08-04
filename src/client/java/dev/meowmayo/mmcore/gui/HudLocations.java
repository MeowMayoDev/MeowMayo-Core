package dev.meowmayo.mmcore.gui;

import dev.meowmayo.mmcore.config.ModConfig;
import dev.meowmayo.mmcore.config.settings.HudElementSetting;
import dev.meowmayo.mmcore.gui.componenets.MeowScreen;
import dev.meowmayo.mmcore.gui.componenets.TextField;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HudLocations extends MeowScreen {
    private TextField searchField;

    private int scrollOffset = 0;

    private final int BUTTON_WIDTH = 200;
    private final int BUTTON_HEIGHT = 20;
    private final int SPACING = 5;

    List<HudElementSetting> currentLocations = new ArrayList<>();

    public HudLocations() {
        super(Component.literal("Hud Editor"), "Hud Editor");
    }

    @Override
    public void init() {
        searchField = new TextField(5, 5, BUTTON_WIDTH, BUTTON_HEIGHT);
        searchField.setFocused(true);

        updateButtons();
    }

    public void updateButtons() {
        this.clearWidgets();

        String query = searchField.getText().toLowerCase();

        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
            this.minecraft.gui.setScreen(new MainGui());
        }).bounds(width - 25, 5, 20, 20).build());

        List<HudElementSetting> locations = ModConfig.getLocations();

        currentLocations.clear();

        for (HudElementSetting setting : locations) {
            if (setting.getTitle().toLowerCase().contains(query)) {
                currentLocations.add(setting);
            }
        }

        for (int i = 0; i < currentLocations.size(); i++) {
            HudElementSetting setting = currentLocations.get(i);

            int yPos = 50 + (i * (BUTTON_HEIGHT + SPACING)) - scrollOffset;

            if (yPos > 30 && yPos < height - 50) {
                this.addRenderableWidget(Button.builder(Component.literal(setting.getTitle()), button -> {
                    this.minecraft.gui.setScreen(new GuiHudEditor(setting));
                }).bounds(width / 2 - 100, yPos, 200, 20).build());
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        String textBefore = searchField.getText();
        searchField.mouseClicked(event.x(), event.y(), event.button());
        if (!searchField.getText().equals(textBefore)) {
            scrollOffset = 0;
            updateButtons();
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (searchField.isFocused()) {
            String oldText = searchField.getText();
            searchField.keyPressed(keyEvent);

            if (!searchField.getText().equals(oldText)) {
                scrollOffset = 0;
                updateButtons();
            }
            return true;
        }

        if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
            ScreenHandler.openMain();
            return true;
        }

        if (keyEvent.key() == GLFW.GLFW_KEY_ENTER || keyEvent.key() == GLFW.GLFW_KEY_KP_ENTER || keyEvent.key() == GLFW.GLFW_KEY_TAB) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        String oldText = searchField.getText();
        searchField.charTyped((char) characterEvent.codepoint());

        if (!searchField.getText().equals(oldText)) {
            scrollOffset = 0;
            updateButtons();
        }

        return super.charTyped(characterEvent);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        scrollOffset -= (float) (g * 10.0F);
        scrollOffset = Math.max(0, Math.min(scrollOffset, (currentLocations.size() * (BUTTON_HEIGHT + SPACING)) - this.height));
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        String title = "Select HUD Element to edit";

        context.centeredText(font, title, this.width / 2, 12, 0xFFFFFFFF);

        searchField.render(context);

        context.fill(10, 30, this.width - 10, 31, 0xFFAAAAAA);
    }
}