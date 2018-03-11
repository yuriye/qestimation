package com.ys.mfc;

public class AnswerVariant {
    private String id;
    private String title;
    private String altTitle;

    public AnswerVariant(String id, String title, String altTitle) {
        this.id = id;
        this.title = title;
        this.altTitle = altTitle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAltTitle() {
        return altTitle;
    }

    public void setAltTitle(String altTitle) {
        this.altTitle = altTitle;
    }
}
