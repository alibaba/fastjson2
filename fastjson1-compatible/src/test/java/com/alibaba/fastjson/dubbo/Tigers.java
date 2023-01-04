package com.alibaba.fastjson.dubbo;

import java.util.HashMap;
import java.util.Map;

public class Tigers {
    private Tiger tiger;
    private Map<String, Tiger> map;

    public Tigers() {
    }

    public Tigers(Tiger tiger) {
        this.map = new HashMap<String, Tiger>();
        this.map.put("1st", tiger);
        this.tiger = tiger;
    }

    public Map<String, Tiger> getMap() {
        return map;
    }

    public void setMap(Map<String, Tiger> map) {
        this.map = map;
    }

    public Tiger getTiger() {
        return tiger;
    }

    public void setTiger(Tiger tiger) {
        this.tiger = tiger;
    }
}
