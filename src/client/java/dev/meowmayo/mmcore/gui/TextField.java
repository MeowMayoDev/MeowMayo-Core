package dev.meowmayo.mmcore.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class TextField {
    private int x, y, width, height;
    private String text = "";
    private int cursorPosition = 0;
    private int renderOffset = 0;
    private boolean focused = false;

    private final Font fontRenderer = Minecraft.getInstance().font;

    public TextField(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setText(String text) {
        this.text = text;
        this.cursorPosition = text.length();
        renderOffset = 0;
    }

    public String getText() {
        return text;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focus) {
        this.focused = focus;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphicsExtractor context) {
        int borderColor = focused ? 0xFFAAAAAA : 0xFF555555;
        int bgColor = 0xFF222222;
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, borderColor);
        context.fill(x, y, x + width, y + height, bgColor);

        String visible = fontRenderer.plainSubstrByWidth(text.substring(renderOffset), width - 6);
        context.text(fontRenderer, visible, x + 3, y + (height - 8) / 2, 0xFFFFFFFF);

        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = x + 3 + fontRenderer.width(text.substring(renderOffset, cursorPosition));
            if (cursorX <= x + width - 2) {
                context.fill(cursorX, y + 3, cursorX + 1, y + height - 3, 0xFFFFFFFF);
            }
        }
    }

    public boolean keyPressed(int keyCode) {
        if (!focused) return false;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty() && cursorPosition > 0) {
            text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
            cursorPosition--;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPosition > 0) {
            cursorPosition--; return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPosition < text.length()) {
            cursorPosition++; return true;
        }
        return false;
    }

    public boolean charTyped(char chr) {
        if (!focused) return false;
        text = text.substring(0, cursorPosition) + chr + text.substring(cursorPosition);
        cursorPosition++;
        return true;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        boolean hovered = mouseX >= x && mouseX < x + width &&
                mouseY >= y && mouseY < y + height;

        focused = hovered;

        if (hovered && button == 1) {
            this.text = "";
            this.cursorPosition = 0;
            this.renderOffset = 0;
        }
    }
}
