package com.alibaba.fastjson2.diff.handle;


import com.alibaba.fastjson2.diff.JsonDiffException;
import com.alibaba.fastjson2.diff.factory.RunTimeDataFactory;
import com.alibaba.fastjson2.diff.path.Defects;

import java.util.Arrays;

/**
 * Processor of simple type, array element is the basic type
 */
public class SimpleArrayHandle extends AbstractArrayHandle {


    /**
     * Don't ignore order
     *
     * @param expect
     * @param actual
     */
    @Override
    protected void compareKeepOrder(Object[] expect, Object[] actual) {
        for (int i = 0; i < expect.length; i++) {
            if (actual[i] == null && expect[i] == null) {
                continue;
            }
            try {
                if (actual[i] == null || !actual[i].equals(expect[i])) {
                    throw new JsonDiffException("diff");
                }
            } catch (Exception e) {
                Defects defects = new Defects()
                    .setActual(actual[i])
                    .setExpect(expect[i])
                    .setIllustrate(String.format("The %d element is inconsistent", i))
                    .setIndexPath(String.format("%s[%d]", getCurrentPath(), i));
                RunTimeDataFactory.getResultInstance().addDefects(defects);
            }
        }
    }


    /**
     * Ignore Order
     *
     * @param expect
     * @param actual
     */
    @Override
    protected void compareIgnoreOrder(Object[] expect, Object[] actual) {

        boolean[] actualSign = new boolean[actual.length];
        boolean[] expectSign = new boolean[expect.length];

        for (int i = 0; i < expect.length; i++) {
            RunTimeDataFactory.getCurrentPathInstance().push(String.format("[%d]", i));
            int index = getCompareItem(expect[i], actual, Arrays.copyOf(actualSign, actualSign.length));
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
            for (j = 0; j < actualSign.length; j++) {
                if (!actualSign[j]) {
                    break;
                }
            }
            Defects defects = new Defects()
                .setActual(actual[j])
                .setExpect(expect[i])
                .setIllustrate(String.format("The %d element is inconsistent", i))
                .setIndexPath(getCurrentPath());
            RunTimeDataFactory.getResultInstance().addDefects(defects);
            RunTimeDataFactory.getCurrentPathInstance().pop();
        }

    }

    private int getCompareItem(Object expect, Object[] actuals, boolean[] actualSign) {
        int index = -1;
        for (int i = 0; i < actuals.length; i++) {
            if (actualSign[i]) {
                continue;
            }
            if (expect.equals(actuals[i])) {
                return i;
            }
        }
        return index;
    }

}
