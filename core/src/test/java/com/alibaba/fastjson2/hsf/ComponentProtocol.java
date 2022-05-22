package com.alibaba.fastjson2.hsf;

import com.alibaba.fastjson.JSONObject;

public class ComponentProtocol {
    private JSONObject endpoint;
    private JSONObject data;

    public JSONObject getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(JSONObject endpoint) {
        this.endpoint = endpoint;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
