package com.alibaba.fastjson2.diff.object;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.Defects;
import com.alibaba.fastjson2.diff.utils.ComparedUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleObjectHandle
        extends AbstractObjectHandle {
    @Override
    protected void doHandle(JSONObject expectObject, JSONObject actualObject) {
        Set<String> expectKeys = expectObject.keySet();
        Set<String> actualKeys = actualObject.keySet();

        // Finding the Difference Set of Two Sets
        Set<String> conditionsActual = conditionsActual(expectKeys, actualKeys);
        Set<String> conditionsExpect = conditionsExpect(expectKeys, actualKeys);

        // Contrast intersection
        Set<String> intersect = intersect(expectKeys, actualKeys);

        // Ergodic intersection
        for (String key : intersect) {
            RunTimeDataFactory.getCurrentPathInstance().push(key);
            try {
                compared(expectObject.get(key), actualObject.get(key));
            } catch (Exception e) {
                Defects defects = new Defects()
                        .setActual(key)
                        .setExpect(key)
                        .setIndexPath(getCurrentPath())
                        .setIllustrate("field parsing error");
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }

        // Traverse the mapped set
        Map<String, String> mapping = RunTimeDataFactory.getOptionInstance().getMapping();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            RunTimeDataFactory.getCurrentPathInstance().push(entry.getKey());
            try {
                compared(expectObject.get(entry.getValue()), actualObject.get(entry.getKey()));
            } catch (Exception e) {
                Defects defects = new Defects()
                        .setActual(entry.getKey())
                        .setExpect(entry.getValue())
                        .setIndexPath(getCurrentPath())
                        .setIllustrate("field parsing error");
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }
    }

    /**
     * ActualKeys have but expectKeys do not
     * First, match the field information that actually exists but is not expected to exist. And these fields are not mapped and ignored
     *
     * @param expectKeys
     * @param actualKeys
     */
    private Set<String> conditionsActual(Set<String> expectKeys, Set<String> actualKeys) {
        Set<String> conditions = new HashSet<>(actualKeys);
        conditions.removeAll(expectKeys);
        Map<String, String> mapping = RunTimeDataFactory.getOptionInstance().getMapping();
        List<String> ignoreKey = RunTimeDataFactory.getOptionInstance().getIgnoreKey();
        for (String key : conditions) {
            if (mapping.get(key) == null && !ignoreKey.contains(key)) {
                Defects defects = new Defects()
                        .setActual(key)
                        .setIndexPath(getCurrentPath())
                        .setIllustrate(String.format("extra field '%s'", key));
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
        }
        return conditions;
    }

    /**
     * ExpectKeys but actualKeys do not
     * The expected field has field information, but the actual information does not. And these field information is not ignored
     *
     * @param expectKeys
     * @param actualKeys
     */
    private Set<String> conditionsExpect(Set<String> expectKeys, Set<String> actualKeys) {
        Set<String> conditions = new HashSet<>(expectKeys);
        conditions.removeAll(actualKeys);
        List<String> ignoreKey = RunTimeDataFactory.getOptionInstance().getIgnoreKey();
        for (String key : conditions) {
            if (!ignoreKey.contains(key)) {
                Defects defects = new Defects()
                        .setActual(key)
                        .setIndexPath(getCurrentPath())
                        .setIllustrate(String.format("missing field '%s'", key));
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
        }
        return conditions;
    }

    /**
     * Intersection
     *
     * @param expectKeys
     * @param actualKeys
     * @return
     */
    private Set<String> intersect(Set<String> expectKeys, Set<String> actualKeys) {
        Set<String> intersect = new HashSet<>(expectKeys);
        intersect.retainAll(actualKeys);
        return intersect;
    }

    private void compared(Object expect, Object actual) throws IllegalAccessException {
        ComparedUtil.notSureAboutComparison(expect, actual);
    }
}
