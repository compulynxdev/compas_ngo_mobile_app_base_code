package com.compastbc.core.data.network.model;

public class HomeBean {
    private int pos;
    private int icon;
    private String title;

    public HomeBean(int pos, int icon, String title) {
        this.pos = pos;
        this.icon = icon;
        this.title = title;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
