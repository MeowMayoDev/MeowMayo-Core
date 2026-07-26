package dev.meowmayo.mmcore.screen;

import dev.meowmayo.mmcore.MeowMayoCore;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MayoScreenRenderer {
    private static final ConcurrentHashMap<Integer, HudTextData> HUD_TEXTS = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(10000);

    public static int addText(String text, int x, int y, float scale) {
        int id = ID_GENERATOR.getAndIncrement();
        HUD_TEXTS.put(id, new HudTextData(x, y, scale, text));
        return id;
    }

    public static void updateText(int id, String newText) {
        HudTextData data = HUD_TEXTS.get(id);
        if (data != null) data.text = newText;
    }

    public static void moveText(int id, int x, int y) {
        HudTextData data = HUD_TEXTS.get(id);
        if (data != null) {
            data.x = x;
            data.y = y;
        }
    }

    public static void scaleText(int id, float scale) {
        HudTextData data = HUD_TEXTS.get(id);
        if (data != null) data.scale = scale;
    }

    public static void removeText(int id) {
        HUD_TEXTS.remove(id);
    }

    public static void clearAll() {
        HUD_TEXTS.clear();
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MeowMayoCore.MOD_ID, "before_chat"),
                MayoScreenRenderer::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (HUD_TEXTS.isEmpty()) return;

        Font font = Minecraft.getInstance().font;

        for (HudTextData textData : HUD_TEXTS.values()) {
            if (textData.text == null) continue;

            var poseStack = graphics.pose();
            poseStack.pushMatrix();

            poseStack.translate(textData.x, textData.y);
            poseStack.scale(textData.scale, textData.scale);

            String[] lines = textData.text.split("\n");

            int yOffset = 0;
            for (String line : lines) {
                if (line != null && !line.isEmpty()) {
                    graphics.text(font, line, 0, yOffset, 0xffffffff, true);
                    yOffset += font.lineHeight + 2;
                }
            }

            poseStack.popMatrix();
        }
    }
}
