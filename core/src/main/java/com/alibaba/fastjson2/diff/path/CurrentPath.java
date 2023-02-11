package com.alibaba.fastjson2.diff.path;

import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;

import java.util.Stack;

public class CurrentPath {
    private Stack<String> path;

    public void push(String key) {
        if (path == null) {
            path = new Stack<>();
        }
        if (!RunTimeDataFactory.getTempDataInstance().isAddDiff()) {
            RunTimeDataFactory.getTempDataInstance().push(key);
            return;
        }
        path.push(key);
    }

    public void pop() {
        if (!RunTimeDataFactory.getTempDataInstance().isAddDiff()) {
            RunTimeDataFactory.getTempDataInstance().pop();
            return;
        }
        try {
            path.pop();
        } catch (Exception ignored) {
            // ignored
        }
    }

    public Stack<String> getPath() {
        if (path == null) {
            path = new Stack<>();
        }
        return this.path;
    }
}
