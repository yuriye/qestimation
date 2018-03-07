package com.ys.mfc;

import java.awt.*;

public class Label {
    private Rectangle rectangle;
    private String text;
    private Font font;

    public Label(Rectangle rectangle, String text, Font font) {
        this.rectangle = rectangle;
        this.text = text;
        this.font = font;
    }

    public Label(Rectangle rectangle, String text) {
        this.rectangle = rectangle;
        this.text = text;
        this.font = font;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int calculateFontSize() {


        return 0;
    }
}
