package com.alibaba.fastjson2.android.eishay;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

@SuppressWarnings("serial")
public class Media
        implements java.io.Serializable {
    public enum Player {
        JAVA, FLASH
    }

    public int bitrate;
    public long duration;
    public String format;
    public int height;
    public List<String> persons;
    public Player player;
    public long size;
    public String title;
    public String uri;
    public int width;
    public String copyright;
}
