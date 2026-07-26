package dev.meowmayo.mmcore.gui;

import dev.meowmayo.mmcore.config.ModConfig;
import dev.meowmayo.mmcore.config.settings.HudElementSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GuiHudEditor extends Screen {
    private final HudElementSetting element;

    // dragging state
    private boolean dragging = false;
    private double dragOffsetX, dragOffsetY;

    public GuiHudEditor(HudElementSetting element) {
        super(Component.literal("Editing: " + element.getTitle()));
        this.element = element;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, 25, 0x88000000);

        context.centeredText(font,
                this.title.getString() + " | Drag to move, Scroll to scale, ESC to go back", // lowkey this esc to go back doesnt work, ill fix later
                this.width / 2, 8, 0xFFFFFFFF);

        String previewText = element.getPlaceholder();
        String[] lines = previewText.split("\n");

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }
        int totalHeight = lines.length * (font.lineHeight + 2);

        context.pose().pushMatrix();
        context.pose().translate(element.getX(), element.getY());
        context.pose().scale(element.getScale(), element.getScale());

        context.fill(-2, -2, maxWidth + 2, totalHeight + 2, 0x55FFFFFF);

        int yOffset = 0;
        for (String line : lines) {
            context.text(font, line, 0, yOffset, 0xFFFFFF00, false);
            yOffset += font.lineHeight + 2;
        }

        context.pose().popMatrix();

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.button() == 0) { // left click
            if (isHovering(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                this.dragging = true;
                this.dragOffsetX = mouseButtonEvent.x() - element.getX();
                this.dragOffsetY = mouseButtonEvent.y() - element.getY();
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (this.dragging) {
            this.dragging = false;
            ModConfig.editLocation(element.name, (int) (mouseButtonEvent.x() - this.dragOffsetX), (int) (mouseButtonEvent.y() - this.dragOffsetY));
        }
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        float currentScale = element.getScale();
        ModConfig.editScale(element.name, g > 0 ? currentScale + 0.05F : Math.max(0.1F, currentScale - 0.05F));
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == 1) {
            this.minecraft.setScreen(new HudLocations());
        }
        return super.keyPressed(keyEvent);
    }

    private boolean isHovering(double mouseX, double mouseY) {
        String[] lines = element.getPlaceholder().split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            int w = font.width(line);
            if (w > maxWidth) maxWidth = w;
        }
        int totalHeight = lines.length * (font.lineHeight + 2);

        float scaledW = (maxWidth + 4) * element.getScale();
        float scaledH = (totalHeight + 4) * element.getScale();

        return mouseX >= element.getX() && mouseX <= element.getX() + scaledW &&
                mouseY >= element.getY() && mouseY <= element.getY() + scaledH;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}