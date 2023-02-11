package com.alibaba.fastjson2.diff.handle;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.diff.factory.HandleExampleFactory;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.Defects;
import com.alibaba.fastjson2.diff.utils.JsonDiffUtil;

import java.util.Arrays;

/**
 * Processor of multidimensional array.
 */
public class MultidimensionalArrayHandle
        extends AbstractArrayHandle {
    @Override
    protected void compareKeepOrder(Object[] expect, Object[] actual) {
        for (int i = 0; i < expect.length; i++) {
            JSONArray expectItem = (JSONArray) expect[i];
            JSONArray actualItem = (JSONArray) actual[i];
            try {
                RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
                AbstractArrayHandle handle = (AbstractArrayHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getArrayHandleClass(expectItem, actualItem));
                handle.handle(expectItem, actualItem);
            } catch (Exception e) {
                Defects defects = new Defects()
                        .setActual(actualItem)
                        .setExpect(expectItem)
                        .setIllustrate(String.format("The %d element is inconsistent", i))
                        .setIndexPath(String.format("%s[%d]", getCurrentPath(), i));
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            } finally {
                RunTimeDataFactory.getCurrentPathInstance().pop();
            }
        }
    }

    /**
     * Ignore Order
     * 1.  It can be matched according to the fields specified by the user
     * 2.  When the sorting field is not specified, the time complexity is 2n. Need n * n comparison
     *
     * @param expect
     * @param actual
     */
    @Override
    public void compareIgnoreOrder(Object[] expect, Object[] actual) {
        /**
         * 1. Traverse the expected array. Find in the actual array.
         * If found. Label the element; Subsequent array matching is not allowed
         * If none is found. Record the error value
         * 2.  The actual that is not matched. One by one comparison with those not matched with expect
         */
        boolean[] actualSign = new boolean[actual.length];
        boolean[] expectSign = new boolean[expect.length];

        for (int i = 0; i < expect.length; i++) {
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            int index = getCompareObject(expect[i], actual, Arrays.copyOf(actualSign, actualSign.length));
            if (index >= 0) {
                actualSign[index] = true;
                expectSign[i] = true;
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }

        // Traverse unmatched elements
        int i, j = 0;
        for (i = 0; i < expectSign.length; i++) {
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            if (expectSign[i]) {
                RunTimeDataFactory.getCurrentPathInstance().pop();
                continue;
            }
            // lookup j position
            for (j = 0; j < actualSign.length; j++) {
                if (!actualSign[j]) {
                    break;
                }
            }
            try {
                actualSign[j] = true;
                expectSign[i] = true;
                AbstractArrayHandle handle = (AbstractArrayHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getArrayHandleClass((JSONArray) expect[i], (JSONArray) actual[j]));
                handle.handle((JSONArray) expect[i], (JSONArray) actual[j]);
            } catch (Exception ignored) {
                Defects defects = new Defects()
                        .setActual(actual[j])
                        .setExpect(expect[i])
                        .setIllustrate(String.format("The %d element is inconsistent", i))
                        .setIndexPath(getCurrentPath());
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }
    }

    /**
     * Find the matches in actual according to expect.
     *
     * @param expect
     * @param actuals
     * @return not matched to return - 1
     */
    private int getCompareObject(Object expect, Object[] actuals, boolean[] actualSign) {
        int index = -1;
        RunTimeDataFactory.getTempDataInstance().setAddDiff(false);
        for (int i = 0; i < actuals.length; i++) {
            if (actualSign[i]) {
                continue;
            }
            try {
                RunTimeDataFactory.getTempDataInstance().clear();
                AbstractArrayHandle handle = (AbstractArrayHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getArrayHandleClass((JSONArray) expect, (JSONArray) actuals[i]));
                handle.handle((JSONArray) expect, (JSONArray) actuals[i]);
                if (RunTimeDataFactory.getTempDataInstance().isDefectsEmpty()) {
                    index = i;
                    break;
                }
            } catch (Exception ignored) {
                // ignored
            } finally {
                RunTimeDataFactory.getTempDataInstance().clear();
            }
        }
        RunTimeDataFactory.getTempDataInstance().setAddDiff(true);
        return index;
    }
}
