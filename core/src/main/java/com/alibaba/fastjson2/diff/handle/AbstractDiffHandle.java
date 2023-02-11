package com.alibaba.fastjson2.diff.handle;

import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;
import com.alibaba.fastjson2.diff.utils.JsonDiffUtil;

public abstract class AbstractDiffHandle
        implements Handle {
    protected String getCurrentPath() {
        return JsonDiffUtil.getCurrentPath(RunTimeDataFactory.getCurrentPathInstance().getPath());
    }

    @Override
    public JsonCompareResult getResult() {
        return RunTimeDataFactory.getResultInstance();
    }

    public JsonCompareResult handle() {
        return RunTimeDataFactory.getResultInstance();
    }
}
