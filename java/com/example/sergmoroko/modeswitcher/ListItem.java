package com.example.sergmoroko.modeswitcher;

/**
 * Created by ssss on 17.11.2016.
 */

public class ListItem {

        private String title = null;
        private String value = null;

        ListItem() {
        }

        ListItem(String title, String value) {
            setTitle(title);
            setValue(value);
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }
    }

