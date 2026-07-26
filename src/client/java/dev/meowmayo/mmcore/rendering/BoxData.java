package dev.meowmayo.mmcore.rendering;

public class BoxData {
    public double x, y, z;
    public float size;
    public final int color;
    public final boolean filled, esp;

    public BoxData(double x, double y, double z, float size, int color, boolean filled, boolean esp) {
        this.x = x; this.y = y; this.z = z;
        this.size = size; this.color = color;
        this.filled = filled; this.esp = esp;
    }
}