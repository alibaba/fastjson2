package com.alibaba.fastjson2.diff.handle;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.Defects;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;

public abstract class AbstractArrayHandle extends AbstractDiffHandle implements ArrayHandle {


    @Override
    public JsonCompareResult handle(JSONArray expectArray, JSONArray actualArray) {
        int expectLen = expectArray.size();
        int actualLen = actualArray.size();

        if (actualLen != expectLen) {
            Defects defects = new Defects()
                .setExpect(expectLen)
                .setActual(actualLen)
                .setIllustrate("Array length is inconsistent")
                .setIndexPath(getCurrentPath());
            RunTimeDataFactory.getResultInstance().addDefects(defects);
            return RunTimeDataFactory.getResultInstance();
        }
        if (actualLen == 0) {
            return RunTimeDataFactory.getResultInstance();
        }

        Object[] expect = expectArray.toArray();
        Object[] actual = actualArray.toArray();

        if (RunTimeDataFactory.getOptionInstance().isIgnoreOrder()) {
            compareIgnoreOrder(expect, actual);
        } else {
            compareKeepOrder(expect, actual);
        }

        return RunTimeDataFactory.getResultInstance();
    }

    protected abstract void compareKeepOrder(Object[] expect, Object[] actual);

    protected abstract void compareIgnoreOrder(Object[] expect, Object[] actual);

}
