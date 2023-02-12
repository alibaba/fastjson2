package com.alibaba.fastjson2.android.eishay;

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
}
