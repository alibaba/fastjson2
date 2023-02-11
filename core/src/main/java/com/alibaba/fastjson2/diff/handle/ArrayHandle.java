package com.alibaba.fastjson2.diff.handle;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;

public interface ArrayHandle
    extends Handle {
    JsonCompareResult handle(JSONArray expectArray, JSONArray actualArray);
}
