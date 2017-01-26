package com.example.sergmoroko.profileSwitcher;

// this object stores listView items data
class ListItem {

    private String title = null;
    private String value = null;
    private String description = null;
    private boolean isEnabled;


    ListItem(String title, String value) {
        setTitle(title);
        setValue(value);
    }


    ListItem(String title, String value, String description, boolean isEnabled) {
        setTitle(title);
        setValue(value);
        setDescription(description);
        setEnabled(isEnabled);
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setValue(String value) {
        this.value = value;
    }

    private void setDescription(String value) {
        this.description = value;
    }

    void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    String getTitle() {
        return title;
    }

    String getValue() {
        return value;
    }

    String getDescription() {
        return description;
    }

    boolean getEnabled() {
        return isEnabled;
    }
}

