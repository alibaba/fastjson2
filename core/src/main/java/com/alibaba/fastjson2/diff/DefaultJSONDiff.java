package com.alibaba.fastjson2.diff;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.factory.HandleExampleFactory;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.handle.AbstractArrayHandle;
import com.alibaba.fastjson2.diff.object.AbstractObjectHandle;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;
import com.alibaba.fastjson2.diff.path.JsonComparedOption;
import com.alibaba.fastjson2.diff.utils.JsonDiffUtil;

public class DefaultJSONDiff
    implements JSONDiff {
    @Override
    public JsonCompareResult detectDiff(JSONObject expect, JSONObject actual) {
        AbstractObjectHandle handle = (AbstractObjectHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getObjectHandleClass(expect, actual));
        handle.handle(expect, actual);
        return RunTimeDataFactory.remove();
    }

    @Override
    public JsonCompareResult detectDiff(JSONArray expect, JSONArray actual) {
        AbstractArrayHandle handle = (AbstractArrayHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getArrayHandleClass(expect, actual));
        handle.handle(expect, actual);
        return RunTimeDataFactory.remove();
    }

    public DefaultJSONDiff option(JsonComparedOption option) {
        RunTimeDataFactory.setOptionInstance(option);
        return this;
    }
}
