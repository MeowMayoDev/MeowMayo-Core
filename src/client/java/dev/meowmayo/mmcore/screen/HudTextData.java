package dev.meowmayo.mmcore.screen;

public class HudTextData {
    public int x, y;
    public float scale;
    public String text;

    public HudTextData(int x, int y, float scale, String text) {
        this.x = x; this.y = y;
        this.scale = scale;
        this.text = text;
    }
}
