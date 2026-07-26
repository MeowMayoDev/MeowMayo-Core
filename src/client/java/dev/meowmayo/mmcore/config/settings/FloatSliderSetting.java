package dev.meowmayo.mmcore.config.settings;

public class FloatSliderSetting extends Setting {
    private float value;
    private final float min;
    private final float max;

    public FloatSliderSetting(String title, String description, String subcategory, float defaultValue, float min, float max) {
        super(title, description, subcategory);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    @Override
    public Float getValue() { return value; }

    @Override
    public void setValue(Object value) {
        if (value instanceof Float) {
            float f = (Float) value;
            if (f < min) f = min;
            if (f > max) f = max;
            this.value = f;
        }
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
}
