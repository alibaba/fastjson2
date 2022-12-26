package com.alibaba.fastjson2.diff.handle;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.function.Function;
import com.alibaba.fastjson2.diff.factory.HandleExampleFactory;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.object.AbstractObjectHandle;
import com.alibaba.fastjson2.diff.path.Defects;
import com.alibaba.fastjson2.diff.utils.ComparedUtil;
import com.alibaba.fastjson2.diff.utils.JsonDiffUtil;

import java.util.Arrays;
import java.util.Stack;

public class ObjectArrayHandle extends AbstractArrayHandle {


    /**
     * ignore order
     * @param expect
     * @param actual
     */
    @Override
    public void compareKeepOrder(Object[] expect, Object[] actual) {
        for (int i = 0; i < expect.length; i++) {
            JSONObject expectItem = (JSONObject)expect[i];
            JSONObject actualItem = (JSONObject)actual[i];
            try {
                RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
                AbstractObjectHandle handle = (AbstractObjectHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getObjectHandleClass(expectItem, actualItem));
                handle.handle(expectItem , actualItem);
            }catch (Exception e) {
                Defects defects = new Defects()
                        .setActual(actualItem)
                        .setExpect(expectItem)
                        .setIllustrate(String.format("The %d element is inconsistent", i))
                        .setIndexPath(String.format("%s[%d]", getCurrentPath(), i));
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }finally {
                RunTimeDataFactory.getCurrentPathInstance().pop();
            }
        }
    }

    @Override
    public void compareIgnoreOrder(Object[] expect, Object[] actual) {

        boolean[] actualSign = new boolean[actual.length];
        boolean[] expectSign = new boolean[expect.length];

        Function<String, Stack<String>> keyFunction = RunTimeDataFactory.getOptionInstance().getKeyFunction();
        Stack<String> keys = null;
        if (keyFunction != null) {
            keys = keyFunction.apply(JsonDiffUtil.convertPath(getCurrentPath(), RunTimeDataFactory.getTempDataInstance().getPath()));
        }

        for (int i = 0; i < expect.length; i++) {
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            int index = getCompareObject(expect[i], actual, Arrays.copyOf(actualSign, actualSign.length), keys);
            if (index >= 0) {
                actualSign[index] = true;
                expectSign[i] = true;
            }
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }

        int i,j = 0;
        for (i = 0; i < expectSign.length; i++) {
            if (expectSign[i]) {
                continue;
            }
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            for (j = 0; j < actualSign.length; j++) {
                if (!actualSign[j]) {
                    break;
                }
            }
            if (j >= actualSign.length) {
                break;
            }
            try {
                actualSign[j] = true;
                expectSign[i] = true;
                AbstractObjectHandle handle = (AbstractObjectHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getObjectHandleClass((JSONObject) expect[i], (JSONObject) actual[j]));
                handle.handle((JSONObject) expect[i], (JSONObject) actual[j]);
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
    }

    private int getCompareObject(Object expect, Object[] actuals, boolean[] actualSign, Stack<String> keys) {
        int index = -1;
        RunTimeDataFactory.getTempDataInstance().setAddDiff(false);
        for (int i = 0; i < actuals.length; i++) {
            if (isMatchComparison((JSONObject) expect, (JSONObject) actuals[i], actualSign[i], keys)) {
                continue;
            }
            try {
                RunTimeDataFactory.getTempDataInstance().clear();
                AbstractObjectHandle handle = (AbstractObjectHandle) HandleExampleFactory.getHandle(JsonDiffUtil.getObjectHandleClass((JSONObject) expect, (JSONObject) actuals[i]));
                handle.handle((JSONObject) expect, (JSONObject) actuals[i]);
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
    private boolean isMatchComparison(JSONObject expect, JSONObject actual, boolean actualSign, Stack<String> keys) {
        if (keys == null || keys.size() == 0) {
            return actualSign;
        }
        return ComparedUtil.isItWorthComparing(expect, actual, keys);
    }

}
