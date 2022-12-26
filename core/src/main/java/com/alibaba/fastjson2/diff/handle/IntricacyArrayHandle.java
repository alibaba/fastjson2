package com.alibaba.fastjson2.diff.handle;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.function.Function;
import com.alibaba.fastjson2.diff.JsonDiffException;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.Defects;
import com.alibaba.fastjson2.diff.utils.ComparedUtil;
import com.alibaba.fastjson2.diff.utils.JsonDiffUtil;

import java.util.Arrays;
import java.util.Stack;

/**
 * Complex type processor, with different types of elements,
 */
public class IntricacyArrayHandle extends AbstractArrayHandle {


    /**
     * Don't ignore order
     * @param expect
     * @param actual
     */
    @Override
    public void compareKeepOrder(Object[] expect, Object[] actual) {
        for (int i = 0; i < expect.length; i++) {
            Class<?> expectClass = JsonDiffUtil.parseItemClass(expect[i]);
            Class<?> actualClass = JsonDiffUtil.parseItemClass(actual[i]);
            try {
                RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
                if (!expectClass.equals(actualClass)) {
                    throw new JsonDiffException("diff");
                }
                ComparedUtil.notSureAboutComparison(expect[i], actual[i]);
            }catch (Exception e) {
                Defects defects = new Defects()
                        .setActual(actual[i])
                        .setExpect(expect[i])
                        .setIllustrate(String.format("The %d element is inconsistent", i))
                        .setIndexPath(String.format("%s[%d]", getCurrentPath(), i));
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }finally {
                RunTimeDataFactory.getCurrentPathInstance().pop();
            }
        }
    }


    /**
     * Ignore Order
     * 1.  It can be matched according to the fields specified by the user
     * 2.  When the sorting field is not specified, the time complexity is 2n. Need n * n comparison
     * @param expect
     * @param actual
     */
    @Override
    public void compareIgnoreOrder(Object[] expect, Object[] actual) {

        /**
         * 1. Traverse the expected array. Find in the actual array.
         * If found. Label the element; Subsequent array matching is not allowed
         * If none is found. Record the error value
         * 2. The actual that is not matched. One by one comparison with those not matched with expect
         */
        boolean[] actualSign = new boolean[actual.length];
        boolean[] expectSign = new boolean[expect.length];

        // Get the keys specified by the user
        Function<String, Stack<String>> keyFunction = RunTimeDataFactory.getOptionInstance().getKeyFunction();
        Stack<String> keys = null;
        if (keyFunction != null) {
            keys = keyFunction.apply(JsonDiffUtil.convertPath(getCurrentPath(), RunTimeDataFactory.getTempDataInstance().getPath()));
        }

        // Compare and find data with the same type and object
        for (int i = 0; i < expect.length; i++) {
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            int index = getCompareObject(expect[i], actual, Arrays.copyOf(actualSign, actualSign.length), keys);
            if (index >= 0) {
                actualSign[index] = true;
                expectSign[i] = true;
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }

        // Compare data of the same type
        int i,j = 0;
        for (i = 0; i < expectSign.length; i++) {
            if (expectSign[i]) {
                continue;
            }
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            // lookup j position
            for (j = 0; j < actualSign.length; j++) {
                if (actualSign[j]) {
                    continue;
                }
                Class<?> expectClass = JsonDiffUtil.parseItemClass(expect[i]);
                Class<?> actualClass = JsonDiffUtil.parseItemClass(actual[j]);
                if (expectClass.equals(actualClass)) {
                    break;
                }
            }
            if (j >= actualSign.length) {
                break;
            }
            try {
                actualSign[j] = true;
                expectSign[i] = true;
                ComparedUtil.notSureAboutComparison(expect[i], actual[j]);
            }catch (Exception ignored) {
                Defects defects = new Defects()
                        .setActual(actual[j])
                        .setExpect(expect[i])
                        .setIllustrate(String.format("The %d element is inconsistent", i))
                        .setIndexPath(getCurrentPath());
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }

        // Compare data of different types
        for (i = 0; i < expectSign.length; i++) {
            if (expectSign[i]) {
                continue;
            }
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            // lookup j position
            for (j = 0; j < actualSign.length; j++) {
                if (!actualSign[j]) {
                    break;
                }
            }
            if (j >= actualSign.length) {
                break;
            }
            Defects defects = new Defects()
                    .setActual(actual[j])
                    .setExpect(expect[i])
                    .setIllustrate("Inconsistent comparison object types")
                    .setIndexPath(getCurrentPath());
            RunTimeDataFactory.getResultInstance().addDefects(defects);
        }
    }


    /**
     * Find the matches in actual according to expect.
     * @param expect
     * @param actuals
     * @return
     */
    private int getCompareObject(Object expect, Object[] actuals, boolean[] actualSign, Stack<String> keys) {
        int index = -1;
        RunTimeDataFactory.getTempDataInstance().setAddDiff(false);
        for (int i = 0; i < actuals.length; i++) {
            if (isMatchComparison(expect, actuals[i], actualSign[i], keys)) {
                continue;
            }
            try {
                RunTimeDataFactory.getTempDataInstance().clear();
                ComparedUtil.notSureAboutComparison(expect, actuals[i]);
                if (RunTimeDataFactory.getTempDataInstance().isDefectsEmpty()) {
                    index = i;
                    break;
                }
            }catch (Exception ignored) {

            }finally {
                RunTimeDataFactory.getTempDataInstance().clear();
            }
        }
        RunTimeDataFactory.getTempDataInstance().setAddDiff(true);
        return index;
    }

    /**
     * Judge whether two objects meet the comparison conditions
     * @param expect
     * @param actual
     * @return
     */
    private boolean isMatchComparison(Object expect, Object actual, boolean actualSign, Stack<String> keys) {
        if (actualSign) {
            return true;
        }
        Class<?> expectClass = JsonDiffUtil.parseItemClass(expect);
        Class<?> actualClass = JsonDiffUtil.parseItemClass(actual);
        // The two types are different
        if (!expectClass.equals(actualClass)) {
            return true;
        }
        // Element is not an object
        if (!ObjectArrayHandle.class.equals(expectClass)) {
            return false;
        }

        // The element is JSONObject and has not been compared
        JSONObject expectObject = (JSONObject) expect;
        JSONObject actualObject = (JSONObject) actual;
        if (keys == null || keys.size() == 0) {
            return false;
        }
        return ComparedUtil.isItWorthComparing(expectObject, actualObject, keys);
    }


}
