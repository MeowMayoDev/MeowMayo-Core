package dev.meowmayo.mmcore.gui.componenets;

import dev.meowmayo.mmcore.config.ModConfig;
import dev.meowmayo.mmcore.config.settings.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class SettingsRow {
    public interface ISettingComponent {
        void render(GuiGraphicsExtractor graphics, Font tr, int x, int y, int width, int mouseX, int mouseY);
        void mouseDown(double mouseX, double mouseY, int screenWidth, int screenHeight, int button);
        void mouseUp(double mouseX, double mouseY, int screenWidth, int screenHeight, int button);
        void keyPressed(KeyEvent keyEvent);
        void charTyped(char chr);
        int getHeight();
    }

    public static String currentQuery = "";

    private static void drawTitle(GuiGraphicsExtractor context, Font tr, String title, int x, int y) {
        if (currentQuery.isEmpty() || !title.toLowerCase().contains(currentQuery.toLowerCase())) {
            context.text(tr, title, x, y, 0xFFFFFFFF);
            return;
        }

        String lowerTitle = title.toLowerCase();
        String lowerQuery = currentQuery.toLowerCase();

        int start = lowerTitle.indexOf(lowerQuery);
        int end = start + currentQuery.length();

        String pre = title.substring(0, start);
        String match = title.substring(start, end);
        String post = title.substring(end);

        context.text(tr, pre, x, y, 0xFFFFFFFF);
        int xOffset = tr.width(pre);

        int matchWidth = tr.width(match);
        context.fill(x + xOffset, y - 1, x + xOffset + matchWidth, y + tr.lineHeight, 0x99FFAA00);

        context.text(tr, match, x + xOffset, y, 0xFFFFFFFF);
        xOffset += matchWidth;

        context.text(tr, post, x + xOffset, y, 0xFFFFFFFF);
    }

    private static void drawDescription(GuiGraphicsExtractor context, Font tr, int x, int y, int width, String desc) {
        if (desc == null || desc.isEmpty()) return;

        int maxWidth = (int) (width * 0.6f);
        List<FormattedCharSequence> lines = tr.split(FormattedText.of(desc), maxWidth);

        int lineY = y + 22;
        for (FormattedCharSequence line : lines) {
            context.pose().pushMatrix();
            context.pose().scale(1.2f, 1.2f);
            context.text(tr, line, (int) ((x + 8) / 1.2f), (int) (lineY / 1.2f), 0xFFAAAAAA);
            context.pose().popMatrix();
            lineY += 15;
        }
    }

    public static class HeaderRow implements ISettingComponent {
        private final String title;
        public HeaderRow(String title) { this.title = title; }

        @Override
        public void render(GuiGraphicsExtractor graphics, Font tr, int x, int y, int width, int mouseX, int mouseY) {
            graphics.text(tr, title, x, y + 5, 0xFFFFFFFF);
        }

        @Override
        public void mouseDown(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
        }

        @Override
        public void mouseUp(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
        }

        @Override
        public void charTyped(char chr) {
        }

        @Override public int getHeight() { return 20; }
    }

    public static class ToggleRow implements ISettingComponent {
        private final ToggleSetting setting;
        public ToggleRow(ToggleSetting setting) { this.setting = setting; }

        @Override
        public void render(GuiGraphicsExtractor graphics, Font tr, int x, int y, int width, int mouseX, int mouseY) {
            graphics.fill(x, y, x + width, y + getHeight() - 5, 0xFF333333);

            graphics.pose().pushMatrix();
            graphics.pose().scale(1.5f, 1.5f);
            drawTitle(graphics, tr, setting.getTitle(), (int)((x + 4)/1.5f), (int)((y + 4)/1.5f));
            graphics.pose().popMatrix();

            drawDescription(graphics, tr, x, y, width, setting.getDescription());

            boolean val = setting.getValue();
            graphics.fill (x + width - 45, y + 22, x + width - 15, y + 52, val ? 0xdd328046 : 0xddd44848);
        }

        @Override
        public void mouseDown(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
            if (mouseX >= (screenWidth - 55) && mouseX <= (screenWidth - 25)) {
                ModConfig.edit(setting.getTitle(), !((Boolean) setting.getValue()));
            }
        }

        @Override
        public void mouseUp(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
        }

        @Override
        public void charTyped(char chr) {
        }

        @Override public int getHeight() { return 85; }
    }

    public static class SliderRow implements ISettingComponent {
        private final Setting setting;
        private boolean dragging = false;

        public SliderRow(Setting setting) { this.setting = setting; }

        @Override
        public void render(GuiGraphicsExtractor graphics, Font tr, int x, int y, int width, int mouseX, int mouseY) {
            graphics.fill(x, y, x + width, y + getHeight() - 5, 0xFF333333);

            graphics.pose().pushMatrix();
            graphics.pose().scale(1.5f, 1.5f);
            drawTitle(graphics, tr, setting.getTitle(), (int)((x + 4)/1.5f), (int)((y + 4)/1.5f));
            graphics.pose().popMatrix();

            drawDescription(graphics, tr, x, y, width, setting.getDescription());

            int barWidth = (int)(width * 0.25f) - 15;
            int barX = x + width - barWidth - 15;
            int barY = y + 30;

            float min = (setting instanceof IntSliderSetting) ? ((IntSliderSetting)setting).getMin() : ((FloatSliderSetting)setting).getMin();
            float max = (setting instanceof IntSliderSetting) ? ((IntSliderSetting)setting).getMax() : ((FloatSliderSetting)setting).getMax();
            float val = Float.parseFloat(setting.getValue().toString());

            if (this.dragging) {
                float percent = Math.min(1, Math.max(0, (float)(mouseX - barX) / barWidth));
                if (setting instanceof IntSliderSetting) {
                    ModConfig.edit(setting.getTitle(), Math.round((max - min) * percent) + (int)min);
                } else {
                    float newVal = ((max - min) * percent) + min;
                    ModConfig.edit(setting.getTitle(), Math.round(newVal * 10) / 10f);
                }
            }

            float renderPercent = (val - min) / (max - min);
            graphics.fill(barX, barY, barX + barWidth, barY + 5, 0xFF555555);
            int handleX = barX + (int)(renderPercent * barWidth);
            graphics.fill(handleX - 5, barY - 3, handleX + 5, barY + 11, 0xFFAAAAAA);

            String valStr = (setting instanceof IntSliderSetting) ? String.valueOf((int)val) : String.valueOf(Math.round(val * 10) / 10f);
            graphics.text(tr, valStr, handleX - tr.width(valStr)/2, barY - 12, 0xFFAAAAAA);
        }

        @Override
        public void mouseDown(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
            if (button == 0) {
                int barWidth = (int)((screenWidth - 120) * 0.25f) - 15;

                int barX = screenWidth - barWidth - 25;

                if (mouseX >= barX && mouseX <= barX + barWidth) {
                    this.dragging = true;
                }
            }

        }

        @Override
        public void mouseUp(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
            this.dragging = false;
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
        }

        @Override
        public void charTyped(char chr) {
        }

        @Override public int getHeight() { return 85; }
    }

    public static class TextRow implements ISettingComponent {
        private final TextSetting setting;
        private final TextField textField;

        public TextRow(TextSetting setting) {
            this.setting = setting;
            this.textField = new TextField(0, 0, 120, 25);
            this.textField.setText((String) setting.getValue());
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, Font tr, int x, int y, int width, int mouseX, int mouseY) {
            graphics.fill(x, y, x + width, y + getHeight() - 5, 0xFF333333);

            graphics.pose().pushMatrix();
            graphics.pose().scale(1.5f, 1.5f);
            drawTitle(graphics, tr, setting.getTitle(), (int)((x + 4)/1.5f), (int)((y + 4)/1.5f));
            graphics.pose().popMatrix();

            drawDescription(graphics, tr, x, y, width, setting.getDescription());

            textField.setPosition(x + width - (int)(width * 0.25f), y + 25);
            textField.setSize((int)(width * 0.25f) - 15, 25);
            textField.render(graphics);
        }

        @Override
        public void mouseDown(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
            textField.mouseClicked(mouseX, mouseY, button);

            String old = textField.getText();
            if (!textField.getText().equals(old)) {
                ModConfig.edit(setting.getTitle(), textField.getText());
            }
        }

        @Override
        public void mouseUp(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (textField.isFocused()) {
                textField.keyPressed(keyEvent);
                ModConfig.edit(setting.getTitle(), textField.getText());
            }
        }

        @Override
        public void charTyped(char chr) {
            if (textField.isFocused()) {
                textField.charTyped(chr);
                ModConfig.edit(setting.getTitle(), textField.getText());
            }
        }

        @Override public int getHeight() { return 85; }
    }
}