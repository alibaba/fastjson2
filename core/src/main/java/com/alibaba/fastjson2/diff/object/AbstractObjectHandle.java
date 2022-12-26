package com.alibaba.fastjson2.diff.object;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.handle.AbstractDiffHandle;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;

import java.util.Set;

/**
 * Abstract array processor
 */
public abstract class AbstractObjectHandle extends AbstractDiffHandle implements ObjectHandle {


    @Override
    public JsonCompareResult handle(JSONObject expectObject, JSONObject actualObject) {
        // 两个都为null
        if (expectObject == null && actualObject == null) {
            return RunTimeDataFactory.getResultInstance();
        }
        Set<String> expectKeys = expectObject.keySet();
        Set<String> actualKeys = actualObject.keySet();
        // 空对象
        if (expectKeys.size() == 0 && actualKeys.size() == 0) {
            return RunTimeDataFactory.getResultInstance();
        }
        doHandle(expectObject, actualObject);
        return RunTimeDataFactory.getResultInstance();
    }

    protected abstract void doHandle(JSONObject expectObject, JSONObject actualObject);
}
