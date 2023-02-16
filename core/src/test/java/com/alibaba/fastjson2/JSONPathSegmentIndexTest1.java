package com.alibaba.fastjson2;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * description: setCallback method test
 * fix: bug when index less than 0
 */
public class JSONPathSegmentIndexTest1 {
    @Test
    public void test_setCallback_given_array_with_index_eq_minus1() {
        //init
        int[] data = new int[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.LAST;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[0] + (int) v;
        });
        //test
        assertEquals(data[4], 6);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[4] + (int) v;
        });
        //test
        assertEquals(data[4], 12);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_objectArray_with_index_eq_minus1() {
        //init
        Integer[] data = new Integer[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.LAST;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[0] + (Integer) v;
        });
        //test
        assertEquals(data[4], 6);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[4] + (Integer) v;
        });
        //test
        assertEquals(data[4], 12);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_list_with_index_eq_minus1() {
        //init
        List<Integer> data = Lists.newArrayList(1, 2, 3, 4, 5);
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.LAST;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(0) + (Integer) v;
        });
        //test
        assertEquals(data.get(4), 6);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(4) + (Integer) v;
        });
        //test
        assertEquals(data.get(4), 12);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_array_with_index_eq_0() {
        //init
        int[] data = new int[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.ZERO;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[0] + (int) v;
        });
        //test
        assertEquals(data[0], 2);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[4] + (int) v;
        });
        //test
        assertEquals(data[0], 7);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_objectArray_with_index_eq_0() {
        //init
        Integer[] data = new Integer[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.ZERO;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[0] + (Integer) v;
        });
        //test
        assertEquals(data[0], 2);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[4] + (Integer) v;
        });
        //test
        assertEquals(data[0], 7);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_list_with_index_eq_0() {
        //init
        List<Integer> data = Lists.newArrayList(1, 2, 3, 4, 5);
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.ZERO;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(0) + (Integer) v;
        });
        //test
        assertEquals(data.get(0), 2);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(4) + (Integer) v;
        });
        //test
        assertEquals(data.get(0), 7);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_array_with_index_eq_1() {
        //init
        int[] data = new int[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.ONE;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[0] + (int) v;
        });
        //test
        assertEquals(data[1], 3);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[4] + (int) v;
        });
        //test
        assertEquals(data[1], 8);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_objectArray_with_index_eq_1() {
        //init
        Integer[] data = new Integer[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.ONE;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[0] + (Integer) v;
        });
        //test
        assertEquals(data[1], 3);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[4] + (Integer) v;
        });
        //test
        assertEquals(data[1], 8);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_list_with_index_eq_1() {
        //init
        List<Integer> data = Lists.newArrayList(1, 2, 3, 4, 5);
        JSONPathSegmentIndex segment = JSONPathSegmentIndex.ONE;
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(0) + (Integer) v;
        });
        //test
        assertEquals(data.get(1), 3);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(4) + (Integer) v;
        });
        //test
        assertEquals(data.get(1), 8);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_array_with_index_eq_minus2() {
        //init
        int[] data = new int[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(-2);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[0] + (int) v;
        });
        //test
        assertEquals(data[3], 5);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[4] + (int) v;
        });
        //test
        assertEquals(data[3], 10);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_objectArray_with_index_eq_minus2() {
        //init
        Integer[] data = new Integer[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(-2);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[0] + (Integer) v;
        });
        //test
        assertEquals(data[3], 5);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((Integer[]) obj)[4] + (Integer) v;
        });
        //test
        assertEquals(data[3], 10);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_list_with_index_eq_minus2() {
        //init
        List<Integer> data = Lists.newArrayList(1, 2, 3, 4, 5);
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(-2);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(0) + (Integer) v;
        });
        //test
        assertEquals(data.get(3), 5);

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(4) + (Integer) v;
        });
        //test
        assertEquals(data.get(3), 10);

        //clear
        context = null;
        data = null;
    }

    @Test
    public void test_setCallback_given_array_with_index_eq_6() {
        //init
        int[] data = new int[]{1, 2, 3, 4, 5};
        int[] compare = new int[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(6);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[0] + (int) v;
        });
        //test
        assertArrayEquals(data, compare);

        //clear
        context = null;
        data = null;
        compare = null;
    }

    @Test
    public void test_setCallback_given_objectArray_with_index_eq_6() {
        //init
        Integer[] data = new Integer[]{1, 2, 3, 4, 5};
        Integer[] compare = new Integer[]{1, 2, 3, 4, 5};
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(6);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((int[]) obj)[0] + (Integer) v;
        });
        //test
        assertArrayEquals(data, compare);

        //clear
        context = null;
        data = null;
        compare = null;
    }

    @Test
    public void test_setCallback_given_list_with_index_eq_6() {
        //init
        List<Integer> data = Lists.newArrayList(1, 2, 3, 4, 5);
        List<Integer> compare = Lists.newArrayList(1, 2, 3, 4, 5);
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(6);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //call
        segment.setCallback(context, (obj, v) -> {
            return ((List<Integer>) obj).get(0) + (Integer) v;
        });
        //test
        assertTrue(data.equals(compare));

        //clear
        context = null;
        data = null;
        compare = null;
    }

    @Test
    public void test_setCallback_given_unsupportedOperation() {
        //init
        Object data = new Object();
        JSONPathSegmentIndex segment = new JSONPathSegmentIndex(6);
        final JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", Integer.TYPE), null, segment, null, 0);
        context.root = data;

        //test
        assertThrows(JSONException.class, () -> {
            //call
            segment.setCallback(context, (obj, v) -> {
                return v;
            });
        });

        //clear
        data = null;
    }
}
