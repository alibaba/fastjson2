package com.alibaba.fastjson2.example.graalvm_native.vo;

@com.alibaba.fastjson2.annotation.JSONCompiled
public class Image
        implements java.io.Serializable {
    private int height;
    private Size size;
    private String title;
    private String uri;
    private int width;

    public Image() {
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Size getSize() {
        return size;
    }

    public enum Size {
        SMALL, LARGE
    }
}
