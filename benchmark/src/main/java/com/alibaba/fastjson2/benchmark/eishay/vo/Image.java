package com.alibaba.fastjson2.benchmark.eishay.vo;

import java.util.Objects;

public class Image
        implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public enum Size {
        SMALL, LARGE
    }

    public int height;
    public Size size;
    public String title; // Can be null
    public String uri;
    public int width;

    public Image() {
    }

    public Image(String uri, String title, int width, int height, Size size) {
        this.height = height;
        this.title = title;
        this.uri = uri;
        this.width = width;
        this.size = size;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Image image = (Image) o;
        return height == image.height && width == image.width && size == image.size && Objects.equals(title, image.title) && Objects.equals(uri, image.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, size, title, uri, width);
    }
}
