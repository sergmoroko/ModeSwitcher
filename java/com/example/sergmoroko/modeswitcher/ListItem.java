package com.example.sergmoroko.modeswitcher;

/**
 * Created by ssss on 17.11.2016.
 */

public class ListItem {

    private String title = null;
    private String value = null;
    private String description = null;
    private boolean isEnabled;

    ListItem() {
    }

    ListItem(String title, String value) {
        setTitle(title);
        setValue(value);
    }

    ListItem(String title, String value, boolean isEnabled) {
        setTitle(title);
        setValue(value);
        setEnabled(isEnabled);
    }

    ListItem(String title, String value, String description, boolean isEnabled) {
        setTitle(title);
        setValue(value);
        setDescription(description);
        setEnabled(isEnabled);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public boolean getEnabled() {
        return isEnabled;
    }
}

