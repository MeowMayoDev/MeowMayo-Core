package dev.meowmayo.mmcore.gui;

import dev.meowmayo.mmcore.config.ChatMacro;
import dev.meowmayo.mmcore.config.ChatMacroManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MacroScreen extends Screen {
    private final List<MacroEntry> entries = new ArrayList<>();
    private MacroEntry listeningEntry = null;

    private double scrollAmount = 0;
    private double BUTTON_HEIGHT = 30;

    public MacroScreen() {
        super(Component.literal("Chat Macros"));
    }

    @Override
    protected void init() {
        entries.clear();
        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
            this.minecraft.setScreen(new MainGui());
        }).bounds(width - 25, 5, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Add New Macro"), b -> {
            ChatMacroManager.addMacro(new ChatMacro(GLFW.GLFW_KEY_UNKNOWN, false, false, ""));
            this.rebuildWidgets();
        }).bounds(5, 5, 100, 20).build());

        int yOffset = 40;

        for (ChatMacro macro : ChatMacroManager.getMacros()) {
            entries.add(new MacroEntry(macro, width / 2 - 150, yOffset));
            yOffset += 30;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        graphics.fill(10, 30, this.width - 10, 31, 0xFFAAAAAA);

        graphics.enableScissor(0, 39, this.width, this.height);

        for (MacroEntry entry : entries) {
            int originalY = entry.y;
            entry.y -= (int) scrollAmount;

            if (entry.y > 30 && entry.y < this.height) {
                entry.render(graphics, mouseX, mouseY);
            }

            entry.y = originalY;
        }

        graphics.disableScissor();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (listeningEntry != null) {
            if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
                listeningEntry.macro.key = GLFW.GLFW_KEY_UNKNOWN;
                listeningEntry = null;
                ChatMacroManager.scheduleWrite();
                return true;
            }
            if (isModifierKey(keyEvent.key())) return false;

            listeningEntry.macro.key = keyEvent.key();
            listeningEntry.macro.useShift = (keyEvent.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
            listeningEntry.macro.useCtrl = (keyEvent.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;

            listeningEntry = null;
            ChatMacroManager.scheduleWrite();
            return true;
        }

        for (MacroEntry entry : entries) {
            if (entry.commandField.keyPressed(keyEvent.key())) {
                if (!entry.macro.command.equals(entry.commandField.getText())) {
                    entry.macro.command = entry.commandField.getText();
                    ChatMacroManager.scheduleWrite();
                }
                return true;
            }
        }

        if (keyEvent.key() == GLFW.GLFW_KEY_ENTER || keyEvent.key() == GLFW.GLFW_KEY_KP_ENTER || keyEvent.key() == GLFW.GLFW_KEY_TAB) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private boolean isModifierKey(int key) {
        return key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT ||
                key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL ||
                key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT ||
                key == GLFW.GLFW_KEY_LEFT_SUPER || key == GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        scrollAmount -= (float) (g * 10.0F);
        scrollAmount = Math.max(0, Math.min(scrollAmount, (entries.size() * BUTTON_HEIGHT) - this.height));
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        for (MacroEntry entry : entries) {
            if (entry.commandField.charTyped((char)(characterEvent.codepoint()))) {
                if (!entry.macro.command.equals(entry.commandField.getText())) {
                    entry.macro.command = entry.commandField.getText();
                    ChatMacroManager.scheduleWrite();
                }
                return true;
            }
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        for (MacroEntry entry : entries) {
            if (entry.mouseClicked(event.x(), event.y(), event.button())) {
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    private class MacroEntry {
        ChatMacro macro;
        TextField commandField;
        int x, y;

        MacroEntry(ChatMacro macro, int x, int y) {
            this.macro = macro;
            this.x = x;
            this.y = y;
            this.commandField = new TextField(x + 110, y, 150, 20);
            this.commandField.setText(macro.command);
        }

        void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            boolean hoveringKey = mouseX >= x && mouseX < x + 100 && mouseY >= y && mouseY < y + 20;
            int keyColor = listeningEntry == this ? 0xFF444444 : (hoveringKey ? 0xFFAAAAAA : 0xFF555555);
            graphics.fill(x, y, x + 100, y + 20, keyColor);
            graphics.centeredText(font, listeningEntry == this ? "> PRESS KEY <" : macro.getKeyName(), x + 50, y + 6, 0xFFFFFFFF);

            commandField.setPosition(x+110, y);

            commandField.render(graphics);

            graphics.fill(x + 265, y, x + 285, y + 20, 0xFF444444);
            graphics.centeredText(font, "X", x + 275, y + 6, 0xFFFFFFFF);
        }

        boolean mouseClicked(double mouseX, double mouseY, int button) {
            commandField.mouseClicked(mouseX, mouseY, button);

            if (mouseX >= x && mouseX < x + 100 && mouseY >= y && mouseY < y + 20) {
                listeningEntry = this;
                return true;
            }

            if (mouseX >= x + 265 && mouseX < x + 285 && mouseY >= y && mouseY < y + 20) {
                ChatMacroManager.removeMacro(this.macro);
                rebuildWidgets();
                return true;
            }
            return false;
        }
    }
}