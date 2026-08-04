package dev.meowmayo.mmcore.gui;

import dev.meowmayo.mmcore.gui.componenets.MeowScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class MainGui extends MeowScreen {
    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath("meowmayo-core", "textures/meowmayo.png");
    private final List<Ripple> activeRipples = new ArrayList<>();

    public MainGui() {
        super(Component.literal("MeowMayo Main Menu"), "Main Menu");
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
            this.minecraft.setScreen(null);
        }).bounds(width - 25, 5, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Edit Config"), button -> {
            this.minecraft.setScreen(new SettingsGui());
        }).bounds(5, 40, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Edit Gui"), button -> {
            this.minecraft.setScreen(new HudLocations());
        }).bounds(5, 70, 150, 20).build());

        List<Supplier<MeowScreen>> screens = ScreenHandler.getScreens();
        int y = 100;
        for (Supplier<MeowScreen> sc : screens) {
            this.addRenderableWidget(Button.builder(Component.literal("Edit " + sc.get().name), button -> {
                this.minecraft.setScreen(sc.get());
            }).bounds(5, y, 150, 20).build());
            y += 30;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        String title = "MeowMayo";

        context.centeredText(this.font, title, this.width / 2, 12, 0xFFFFFFFF);
        context.fill(10, 30, this.width - 10, 31, 0xFFAAAAAA);

        drawWarholGrid(context, mouseX, mouseY);

        renderRipples(context);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    // I used ai to create this because it was funny
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int gridSize = 6;
        int totalGridSize = Math.min(width / 2 - 40, height - 80);
        int tileSize = (totalGridSize / gridSize) - 2; // -2 for padding
        int startX = width - totalGridSize - 20;
        int startY = 45;

        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();

        if (mouseButtonEvent.button() == 0) { // Left click only
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int x = startX + (col * (tileSize + 2));
                    int y = startY + (row * (tileSize + 2));

                    if (mouseX >= x && mouseX < x + tileSize && mouseY >= y && mouseY < y + tileSize) {
//                        playCustomSound();

                        int[] randomPalette = {0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0xFF55FF, 0x55FFFF};
                        int randomColor = randomPalette[(int)(Math.random() * randomPalette.length)];

                        activeRipples.add(new Ripple(mouseX, mouseY, tileSize, randomColor));
                    }
                }
            }
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

//    @Override
//    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
//        super.mouseClicked(mouseX, mouseY, mouseButton);
//
//        // Grid coordinates (ensure these match your drawing math exactly)
//        int gridSize = 6;
//        int totalGridSize = Math.min(width / 2 - 40, height - 80);
//        int tileSize = (totalGridSize / gridSize) - 2; // -2 for padding
//        int startX = width - totalGridSize - 20;
//        int startY = 45;
//
//        if (mouseButton == 0) { // Left click only
//            for (int row = 0; row < gridSize; row++) {
//                for (int col = 0; col < gridSize; col++) {
//                    int x = startX + (col * (tileSize + 2));
//                    int y = startY + (row * (tileSize + 2));
//
//                    if (mouseX >= x && mouseX < x + tileSize && mouseY >= y && mouseY < y + tileSize) {
//                        playCustomSound();
//
//                        int[] randomPalette = {0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0xFF55FF, 0x55FFFF};
//                        int randomColor = randomPalette[(int)(Math.random() * randomPalette.length)];
//
//                        activeRipples.add(new Ripple(mouseX, mouseY, x, y, tileSize, randomColor));
//                    }
//                }
//            }
//        }
//    }

    private void drawWarholGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int gridSize = 6;
        int padding = 2;

        int totalGridSize = Math.min(width / 2 - 40, height - 80);
        int tileSize = (totalGridSize / gridSize) - padding;

        int startX = width - totalGridSize - 20;
        int startY = 45;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int x = startX + (col * (tileSize + padding));
                int y = startY + (row * (tileSize + padding));

                boolean isHovered = mouseX >= x && mouseX < x + tileSize && mouseY >= y && mouseY < y + tileSize;

                graphics.pose().pushMatrix();

                if (isHovered) {
                    // POP EFFECT: Scale and Random Warhol Color
                    graphics.pose().translate(x + tileSize/2f, y + tileSize/2f);
                    graphics.pose().scale(1.1f, 1.1f);
                    graphics.pose().translate(-(x + tileSize/2f), -(y + tileSize/2f));
                }

                int color = isHovered ? getWarholColor(row * gridSize + col) : 0xFFFFFF;
                int tint = (0xFF << 24) | (color & 0xFFFFFF); // Add full alpha (FF)

                graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, x, y, 0, 0, tileSize, tileSize, tileSize, tileSize, tint);
                graphics.pose().popMatrix();
            }
        }
    }

    private void renderRipples(GuiGraphicsExtractor graphics) {
        long now = System.currentTimeMillis();
        Iterator<Ripple> it = activeRipples.iterator();

        while (it.hasNext()) {
            Ripple r = it.next();
            float progress = (now - r.startTime) / 800f;

            if (progress >= 1.0f) {
                it.remove();
                continue;
            }

            // Fading alpha (Starts at 180, goes to 0)
            int alpha = (int) ((1.0f - progress) * 180);
            int colorWithAlpha = (alpha << 24) | (r.color & 0x00FFFFFF);
            float currentRadius = r.maxRadius * progress;

            // Draw expanding square as the ripple
            graphics.fill(
                    (int)(r.x - currentRadius), (int)(r.y - currentRadius),
                    (int)(r.x + currentRadius), (int)(r.y + currentRadius),
                    colorWithAlpha
            );
        }
    }

    private int getWarholColor(int index) {
        int[] warholPalette = {
                0xFF5555, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF,
                0xFF8800, 0xFF0088, 0x00FF88, 0x8800FF, 0x0088FF, 0x88FF00
        };
        return warholPalette[index % warholPalette.length];
    }

//    private void playCustomSound() {
//        if (this.client == null) return;
//        Identifier soundId = Identifier.of("meowmayo", "boom_sound");
//        this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(soundId), 1.0f));
//    }

    public static class Ripple {
        public double x, y;
        public int color;
        public long startTime;
        public float maxRadius;
        public Ripple(double x, double y, float maxRadius, int color) {
            this.x = x; this.y = y; this.color = color;
            this.startTime = System.currentTimeMillis();
            this.maxRadius = maxRadius;
        }
    }
}

