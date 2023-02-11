package com.alibaba.fastjson2.diff;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;

public interface JSONDiff {
    JsonCompareResult detectDiff(JSONObject expect, JSONObject actual) throws JSONException, IllegalAccessException;

    JsonCompareResult detectDiff(JSONArray expect, JSONArray actual) throws JSONException, IllegalAccessException;
}
