package dev.meowmayo.mmcore.rendering;

public class TextData {
    public double x, y, z;
    public float scale;
    public String text;

    public TextData(double x, double y, double z, float scale, String text) {
        this.x = x; this.y = y; this.z = z;
        this.scale = scale;
        this.text = text;
    }
}