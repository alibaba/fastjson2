package com.alibaba.fastjson2.diff.path;


import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.utils.JsonDiffUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Temporary configurations used in comparison
 */
public class JsonComparedTempData {
    /**
     * Isolate data according to currentPath
     */
    private Map<String, TempData> nodeDataMap = new HashMap<>();


    /**
     * clear
     */
    public void clear() {
        getTempData().clear();
    }

    /**
     * Whether the comparison information is empty
     */
    public boolean isDefectsEmpty() {
        return getTempData().isDefectsEmpty();
    }


    /**
     * Add comparison information
     *
     * @param defects
     */
    public void addDefects(Defects defects) {
        getTempData().addDefects(defects);
    }


    /**
     * Get TempData of the current node
     *
     * @return
     */
    private TempData getTempData() {
        String currentPath = JsonDiffUtil.getCurrentPath(RunTimeDataFactory.getCurrentPathInstance().getPath());
        if (nodeDataMap.get(currentPath) == null) {
            nodeDataMap.put(currentPath, new TempData());
        }
        return nodeDataMap.get(currentPath);
    }

    public boolean isAddDiff() {
        return getTempData().isAddDiff();
    }

    public void setAddDiff(boolean addDiff) {
        getTempData().setAddDiff(addDiff);
    }


    /**
     * temp path
     *
     * @param key
     */

    public void push(String key) {
        getTempData().push(key);
    }

    public void pop() {
        getTempData().pop();
    }

    public Stack<String> getPath() {
        return getTempData().getPath();
    }
}
