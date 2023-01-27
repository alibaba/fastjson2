package com.alibaba.fastjson2.diff.path;


import com.alibaba.fastjson2.diff.function.Function;

import java.util.*;

public class JsonComparedOption {

    /**
     * Ignore array order
     */
    private boolean ignoreOrder;

    /**
     * The key is actual
     * V alue is an expect map
     */
    private Map<String, String> mapping;

    /**
     * Ignored path With To distinguish json levels; It will accurately match the path
     */
    private List<String> ignorePath;

    /**
     * Ignored key. Fields in actual but not in expect will be ignored
     */
    private List<String> ignoreKey;

    /**
     * When ignoreOrder=true, when the array is an element object, specify which keys to contact the object according to
     */
    private Function<String, Stack<String>> keyFunction;


    public JsonComparedOption() {
    }

    public boolean isIgnoreOrder() {
        return ignoreOrder;
    }

    public JsonComparedOption setIgnoreOrder(boolean ignoreOrder) {
        this.ignoreOrder = ignoreOrder;
        return this;
    }

    public Map<String, String> getMapping() {
        if (mapping == null) {
            mapping = new HashMap<>();
        }
        return mapping;
    }

    public JsonComparedOption setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
        return this;
    }

    public List<String> getIgnorePath() {
        if (ignorePath == null) {
            ignorePath = new ArrayList<>();
        }
        return ignorePath;
    }

    public JsonComparedOption setIgnorePath(List<String> ignorePath) {
        this.ignorePath = ignorePath;
        return this;
    }

    public List<String> getIgnoreKey() {
        if (ignoreKey == null) {
            ignoreKey = new ArrayList<>();
        }
        return ignoreKey;
    }

    public JsonComparedOption setIgnoreKey(List<String> ignoreKey) {
        this.ignoreKey = ignoreKey;
        return this;
    }

    public Function<String, Stack<String>> getKeyFunction() {
        return keyFunction;
    }

    public JsonComparedOption setKeyFunction(Function<String, Stack<String>> keyFunction) {
        this.keyFunction = keyFunction;
        return this;
    }
}
