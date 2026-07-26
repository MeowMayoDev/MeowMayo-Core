package dev.meowmayo.mmcore.config.settings;

public class HudElementSetting {
    public String name;
    public int x, y;
    public float scale;

    public String placeholder;

    public HudElementSetting(String name, String ph) {
        this.name = name;
        this.x = 10;
        this.y = 10;
        this.scale = 1.0f;

        this.placeholder = ph;
    }

    public String getTitle() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public float getScale() { return scale; }

    public String getPlaceholder() { return placeholder; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setScale(float scale) { this.scale = scale; }
}
