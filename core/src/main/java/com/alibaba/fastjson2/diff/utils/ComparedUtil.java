package com.alibaba.fastjson2.diff.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.factory.HandleExampleFactory;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.handle.AbstractArrayHandle;
import com.alibaba.fastjson2.diff.object.AbstractObjectHandle;
import com.alibaba.fastjson2.diff.path.Defects;

import java.util.Stack;

public class ComparedUtil {


    /**
     * When the element is of indeterminate type
     *
     * @param expect
     * @param actual
     * @throws IllegalAccessException
     */
    public static void notSureAboutComparison(Object expect, Object actual) throws IllegalAccessException {
        if (JsonDiffUtil.isPrimitiveType(expect)) {
            if (!expect.equals(actual)) {
                Defects defects = new Defects()
                    .setActual(actual)
                    .setExpect(expect)
                    .setIndexPath(JsonDiffUtil.getCurrentPath(RunTimeDataFactory.getCurrentPathInstance().getPath()))
                    .setIllustrate("properties are different");
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
        } else if (expect instanceof JSONArray) {
            AbstractArrayHandle handle = (AbstractArrayHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getArrayHandleClass((JSONArray) expect, (JSONArray) actual));
            handle.handle((JSONArray) expect, (JSONArray) actual);
        } else if (expect instanceof JSONObject) {
            AbstractObjectHandle handle = (AbstractObjectHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getObjectHandleClass((JSONObject) expect, (JSONObject) actual));
            handle.handle((JSONObject) expect, (JSONObject) actual);
        }
    }


    /**
     * Compare whether two objects are worth comparing
     *
     * @param expect
     * @param actual
     * @param keys
     * @return
     */
    public static boolean isItWorthComparing(JSONObject expect, JSONObject actual, Stack<String> keys) {
        boolean flag = true;
        for (String key : keys) {
            if (expect.get(key) != null && actual.get(key) != null) {
                if (JsonDiffUtil.isPrimitiveType(expect.get(key)) && JsonDiffUtil.isPrimitiveType(actual.get(key))) {
                    flag = !expect.get(key).equals(actual.get(key));
                }
            }
            if (!flag) {
                return false;
            }
        }
        return true;
    }

}
