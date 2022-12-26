package com.alibaba.fastjson2.diff.object;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.handle.Handle;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;

public interface ObjectHandle extends Handle {

    JsonCompareResult handle(JSONObject expectObject, JSONObject actualObject);

}
