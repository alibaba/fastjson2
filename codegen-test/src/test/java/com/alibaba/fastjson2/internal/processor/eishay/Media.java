package com.alibaba.fastjson2.internal.processor.eishay;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

import java.util.List;

@JSONCompiled
@Data
public class Media
        implements java.io.Serializable {
    public enum Player {
        JAVA, FLASH
    }

    private int bitrate;
    private long duration;
    private String format;
    private int height;
    private List<String> persons;
    private Player player;
    private long size;
    private String title;
    private String uri;
    private int width;
    private String copyright;
}
