package com.alibaba.fastjson2.diff.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * temp data
 */
public class TempData {
    /**
     * Temporary defects not recorded in the result
     */
    private List<Defects> defectsList;

    /**
     * @param ignoreOrder
     * @return
     */
    private int addDiffCount = 0;

    /**
     * Temporary Path not recorded in the result
     */
    private Stack<String> tempPath = new Stack<>();

    public boolean isAddDiff() {
        return addDiffCount == 0;
    }

    public void setAddDiff(boolean addDiff) {
        if (addDiff) {
            this.addDiffCount--;
        } else {
            this.addDiffCount++;
        }
    }

    public void clear() {
        if (defectsList != null) {
            defectsList.clear();
        }
    }

    public boolean isDefectsEmpty() {
        if (defectsList == null) {
            return true;
        }
        return defectsList.size() == 0;
    }

    public void addDefects(Defects defects) {
        if (defectsList == null) {
            defectsList = new ArrayList<>();
        }
        defectsList.add(defects);
    }


    public void push(String key) {
        if (tempPath == null) {
            tempPath = new Stack<>();
        }
        tempPath.push(key);
    }

    public void pop() {
        try {
            tempPath.pop();
        } catch (Exception ignored) {

        }
    }

    public Stack<String> getPath() {
        if (tempPath == null) {
            tempPath = new Stack<>();
        }
        return this.tempPath;
    }
}
