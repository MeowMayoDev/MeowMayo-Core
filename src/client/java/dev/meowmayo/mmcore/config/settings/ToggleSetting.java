package dev.meowmayo.mmcore.config.settings;

public class ToggleSetting extends Setting {
    private boolean value;

    public ToggleSetting(String title, String description, String subcategory, boolean defaultValue) {
        super(title, description, subcategory);
        this.value = defaultValue;
    }

    @Override
    public Boolean getValue() { return value; }

    @Override
    public void setValue(Object value) {
        if (value instanceof Boolean) this.value = (Boolean) value;
    }

    public void toggle() { value = !value; }
}