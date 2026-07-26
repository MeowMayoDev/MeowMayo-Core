package dev.meowmayo.mmcore.config.settings;

public class TextSetting extends Setting {
    private String value;

    public TextSetting(String title, String description, String subcategory, String defaultValue) {
        super(title, description, subcategory);
        this.value = defaultValue;
    }

    @Override
    public String getValue() { return value; }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) this.value = (String) value;
    }
}
