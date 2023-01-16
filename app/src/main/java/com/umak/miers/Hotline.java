package com.umak.miers;

public class Hotline {

    public String source, hotline;
    public String type, image;

    public Hotline() {
    }

    public Hotline(String source, String hotline) {
        this.source = source;
        this.hotline = hotline;
    }

    public String getSource() {
        return source;
    }

    public String getHotline() {
        return hotline;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }
}
