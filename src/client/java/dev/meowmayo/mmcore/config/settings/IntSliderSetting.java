package dev.meowmayo.mmcore.config.settings;

public class IntSliderSetting extends Setting {
    private int value;
    private final int min;
    private final int max;

    public IntSliderSetting(String title, String description, String subcategory, int defaultValue, int min, int max) {
        super(title, description, subcategory);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    @Override
    public Integer getValue() { return value; }

    @Override
    public void setValue(Object value) {
        if (value instanceof Integer) {
            int f = (Integer) value;
            if (f < min) f = min;
            if (f > max) f = max;
            this.value = f;
        }
    }

    public int getMin() { return min; }
    public int getMax() { return max; }
}
