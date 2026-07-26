package dev.meowmayo.mmcore.config.settings;

// why am i doing this, was it too hard to just use a premade lib for guis?

public abstract class Setting {
    protected final String title;
    protected final String description;
    protected final String subcategory;

    public Setting(String title, String description, String subcategory) {
        this.title = title;
        this.description = description;
        this.subcategory = subcategory;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubcategory() { return subcategory; }

    public abstract Object getValue();
    public abstract void setValue(Object value);
}