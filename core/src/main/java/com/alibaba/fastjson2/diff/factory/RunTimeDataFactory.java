package com.alibaba.fastjson2.diff.factory;

import com.alibaba.fastjson2.diff.path.CurrentPath;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;
import com.alibaba.fastjson2.diff.path.JsonComparedOption;
import com.alibaba.fastjson2.diff.path.JsonComparedTempData;

import static com.alibaba.fastjson2.diff.JsonDiffConstants.ROOT_PATH;

/**
 * Runtime Data Factory
 */
public class RunTimeDataFactory {

    private final static ThreadLocal<JsonComparedOption> optionThreadLocal = new ThreadLocal<>();

    private static final ThreadLocal<JsonCompareResult> resultThreadLocal = new ThreadLocal<>();

    /**
     * Compare the temporary auxiliary data area. Data isolation according to currentPath
     */
    private static final ThreadLocal<JsonComparedTempData> jsonComparedTempDataThreadLocal = new ThreadLocal<>();

    /**
     * Path currently traversed
     */
    private static final ThreadLocal<CurrentPath> currentPathThreadLocal = new ThreadLocal<>();

    public static JsonComparedOption getOptionInstance() {
        if (optionThreadLocal.get() == null) {
            optionThreadLocal.set(new JsonComparedOption());
        }
        return optionThreadLocal.get();
    }

    public static void setOptionInstance(JsonComparedOption jsonComparedOption) {
        optionThreadLocal.remove();
        optionThreadLocal.set(jsonComparedOption);
    }

    public static JsonCompareResult getResultInstance() {
        if(resultThreadLocal.get() == null) {
            JsonCompareResult jsonCompareResult = new JsonCompareResult();
            resultThreadLocal.set(jsonCompareResult);
        }
        return resultThreadLocal.get();
    }

    public static JsonComparedTempData getTempDataInstance() {
        if(jsonComparedTempDataThreadLocal.get() == null) {
            JsonComparedTempData jsonComparedTempData = new JsonComparedTempData();
            jsonComparedTempDataThreadLocal.set(jsonComparedTempData);
        }
        return jsonComparedTempDataThreadLocal.get();
    }


    public static CurrentPath getCurrentPathInstance() {
        if(currentPathThreadLocal.get() == null) {
            currentPathThreadLocal.set(new CurrentPath());
            currentPathThreadLocal.get().push(ROOT_PATH);
        }
        return currentPathThreadLocal.get();
    }

    public static JsonCompareResult remove() {
        JsonCompareResult jsonCompareResult = resultThreadLocal.get();
        optionThreadLocal.remove();
        resultThreadLocal.remove();
        currentPathThreadLocal.remove();
        jsonComparedTempDataThreadLocal.remove();
        return jsonCompareResult;
    }


}
