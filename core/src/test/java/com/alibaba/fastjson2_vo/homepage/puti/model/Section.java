package com.alibaba.fastjson2_vo.homepage.puti.model;

import java.util.List;
import java.util.Map;

public class Section {
    public boolean tracked;
    public String template;
    public String templateUrl;
    public int version;
    public String hideOnError;

    public List<Item> items; // 活动集合
    public String type;
    public String group;
    public List<Section> sections;

    public int way;
    public String id;
    public Map<String, String> trackParam;
    public String preload;
    public String deleteId;
}
