package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.Int1;
import com.alibaba.fastjson2_vo.IntField1;
import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTest3 {
    @Test
    public void test_0() {
        Integer[] values = new Integer[]{1, 2, 3};

        assertEquals(values[0], JSONPath.of("$[0]").eval(values));
        assertEquals("[1,2]",
                JSONPath
                        .of("$[0,1]")
                        .eval(values)
                        .toString());
        assertEquals("[1,2]",
                JSONPath
                        .of("$[:1]")
                        .eval(values)
                        .toString());
        assertEquals(3,
                JSONPath
                        .of("$.length()")
                        .eval(values));
    }

    @Test
    public void test_length() {
        Map values = Collections.singletonMap("a", 1);
        assertEquals(1,
                JSONPath
                        .of("$.length()")
                        .eval(values));
    }

    @Test
    public void test_isRef() {
        assertFalse(JSONPath
                .of("$.length()")
                .isRef());
        assertTrue(JSONPath
                .of("$.a.b")
                .isRef());
        assertFalse(JSONPath
                .of("$..b")
                .isRef());
    }

    @Test
    public void test_contains() {
        Int1 vo = new Int1();
        vo.setV0000(101);

        assertTrue(JSONPath
                .of("$.v0000")
                .contains(vo));
        assertFalse(JSONPath
                .of("$.v0001")
                .contains(vo));
    }

    @Test
    public void test_filters_intOp() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", 101)));
        array.add(new JSONObject(Collections.singletonMap("val", 102)));

        assertEquals("[{\"val\":101}]", JSONPath
                .of("$[?(@.val=101)]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":101}]", JSONPath
                .of("$[?(@.val<=101)]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":101}]", JSONPath
                .of("$[?(@.val<102)]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":102}]", JSONPath
                .of("$[?(@.val>=102)]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":102}]", JSONPath
                .of("$[?(@.val>101)]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":102}]", JSONPath
                .of("$[?(@.val!=101)]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_1() {
        Integer[] values = new Integer[]{1, 2, 3};
        byte[] jsonbBytes = JSONB.toBytes(values);

        assertEquals(values[0], JSONPath.of("$[0]").extract(JSONReader.ofJSONB(jsonbBytes)));
        assertEquals("[1,2]",
                JSONPath
                        .of("$[0,1]")
                        .extract(JSONReader.ofJSONB(jsonbBytes))
                        .toString());
        assertEquals("[1]",
                JSONPath
                        .of("$[:1]")
                        .extract(JSONReader.ofJSONB(jsonbBytes))
                        .toString());
    }

    @Test
    public void test_2() {
        Integer[] values = new Integer[]{1, 2, 3};
        String jsonString = JSON.toJSONString(values);

        assertEquals(values[0], JSONPath.of("$[0]").extract(JSONReader.of(jsonString)));
        assertEquals("[1,2]",
                JSONPath
                        .of("$[0,1]")
                        .extract(JSONReader.of(jsonString))
                        .toString());
        assertEquals("[1]",
                JSONPath
                        .of("$[:1]")
                        .extract(JSONReader.of(jsonString))
                        .toString());
    }

    @Test
    public void test_range() {
        String jsonString = "[1,2,true,\"a\",false,[],{},null]";
        assertEquals("[1,2,true,\"a\",false,[],{},null]",
                JSONPath
                        .of("$[0:]")
                        .extract(JSONReader.of(jsonString))
                        .toString());
    }

    @Test
    public void test_multi_index() {
        String jsonString = "[1,2,true,\"a\",false,[],{},null]";
        assertEquals("[1,2,true,\"a\",false,[],{},null]",
                JSONPath
                        .of("$[0,1,2,3,4,5,6,7]")
                        .extract(JSONReader.of(jsonString))
                        .toString());
    }

    @Test
    public void test_gt() {
        String jsonString = "[1,2,3,4,5]";
        assertEquals("[4,5]",
                JSONPath
                        .of("$[?(@>3)]")
                        .extract(JSONReader.of(jsonString))
                        .toString());
    }

    @Test
    public void test_filters_StringOp() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", "abc")));
        array.add(new JSONObject(Collections.singletonMap("val", "abd")));

        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val='abc')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abd\"}]", JSONPath
                .of("$[?(@.val>'abc')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"},{\"val\":\"abd\"}]", JSONPath
                .of("$[?(@.val>='abc')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val<'abd')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val<='abc')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val!='abd')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val<>'abd')]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_filters_StringOp_1() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", "abc")));
        array.add(new JSONObject(Collections.singletonMap("val", "abd")));

        assertEquals("[]", JSONPath
                .of("$[?(@.val =~ /.*REES/i)]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val =~ /ABC/i)]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_filters_StringOp_rlike() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", "abc")));
        array.add(new JSONObject(Collections.singletonMap("val", "abd")));

        assertEquals("[]", JSONPath
                .of("$[?(@.val rlike '.*REES')]")
                .eval(array)
                .toString());
        assertEquals("[{\"val\":\"abc\"}]", JSONPath
                .of("$[?(@.val rlike 'abc')]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_in_0() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", 101)));
        array.add(new JSONObject(Collections.singletonMap("val", 102)));

        assertEquals("[{\"val\":101}]", JSONPath
                .of("$[?(@.val in (101))]")
                .eval(array)
                .toString());

        assertEquals("[{\"val\":102}]", JSONPath
                .of("$[?(@.val not in (101))]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_in_1() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", 101D)));
        array.add(new JSONObject(Collections.singletonMap("val", 102D)));

        assertEquals("[{\"val\":101.0}]", JSONPath
                .of("$[?(@.val in (101))]")
                .eval(array)
                .toString());

        assertEquals("[{\"val\":102.0}]", JSONPath
                .of("$[?(@.val not in (101))]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_in_2() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", BigDecimal.valueOf(101))));
        array.add(new JSONObject(Collections.singletonMap("val", BigDecimal.valueOf(102))));

        assertEquals("[{\"val\":101}]", JSONPath
                .of("$[?(@.val in (101))]")
                .eval(array)
                .toString());

        assertEquals("[{\"val\":102}]", JSONPath
                .of("$[?(@.val not in (101))]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_in_3() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject(Collections.singletonMap("val", BigInteger.valueOf(101))));
        array.add(new JSONObject(Collections.singletonMap("val", BigInteger.valueOf(102))));

        assertEquals("[{\"val\":101}]", JSONPath
                .of("$[?(@.val in (101))]")
                .eval(array)
                .toString());

        assertEquals("[{\"val\":102}]", JSONPath
                .of("$[?(@.val not in (101))]")
                .eval(array)
                .toString());
    }

    @Test
    public void test_contains_0() {
        assertTrue(
                JSONPath.of("$.id")
                        .contains(
                                JSONObject.of("id", 123)
                        )
        );
        assertFalse(
                JSONPath.of("$.xx")
                        .contains(
                                JSONObject.of("id", 123)
                        )
        );
        assertTrue(
                JSONPath.of("$[0]")
                        .contains(
                                JSONArray.of("id")
                        )
        );
        assertFalse(
                JSONPath.of("$[1]")
                        .contains(
                                JSONArray.of("id")
                        )
        );
    }

    @Test
    public void test_contains_1() {
        assertFalse(
                JSONPath.of("$[0].v0000")
                        .contains(
                                new Object[]{Arrays.asList(new Integer1())}
                        )
        );
        assertFalse(
                JSONPath.of("$[0].v0000")
                        .contains(
                                Arrays.asList(Arrays.asList(new Integer1()))
                        )
        );
        assertFalse(
                JSONPath.of("$[0].v0000")
                        .contains(
                                new JSONArray().fluentAdd(new Object[]{new Integer1()})
                        )
        );

        Integer1 vo = new Integer1();
        vo.setV0000(1001);
        assertTrue(
                JSONPath.of("$[0].v0000")
                        .contains(
                                Arrays.asList(Arrays.asList(null, vo))
                        )
        );
        assertTrue(
                JSONPath.of("$[0].v0000")
                        .contains(
                                Arrays.asList(new Object[]{vo})
                        )
        );
        assertTrue(
                JSONPath.of("$[0].v0000")
                        .contains(
                                new JSONArray().fluentAdd(new Object[]{vo})
                        )
        );
    }

    @Test
    public void test_contains_2() {
        assertTrue(
                JSONPath.of("$[0].v0000")
                        .contains(
                                new JSONArray()
                                        .fluentAdd(new Object[]{null, JSONObject.of("v0000", 1001)
                                        })
                        )
        );
    }

    @Test
    public void test_contains_3() {
        assertTrue(
                JSONPath.of("$[0][0]")
                        .contains(
                                new JSONArray()
                                        .fluentAdd(new int[]{123})
                        )
        );
    }

    @Test
    public void test_arrayAdd_0() {
        JSONArray root = new JSONArray().fluentAdd(new JSONArray());
        JSONPath.of("$[0]").arrayAdd(root, 0);
        assertEquals("[[0]]", root.toString());
    }

    @Test
    public void test_extractScalar_0() {
        assertEquals("123",
                JSONPath.of("$.id")
                        .extractScalar(
                                JSONReader.of("{\"id\":123}")
                        )
        );
        assertEquals("123",
                JSONPath.of("$[0]")
                        .extractScalar(
                                JSONReader.of("[123]")
                        )
        );

        assertEquals("$[0]", JSONPath.of("$[0]").toString());
    }

    @Test
    public void test_size_0() {
        assertEquals(1,

                JSONPath.of("$.size()")
                        .eval(
                                JSONObject.of("id", 123))
        );
        assertEquals(1,

                JSONPath.of("$[0].size()")
                        .eval(
                                JSONArray.of(
                                        JSONObject.of("id", 123)
                                )
                        )
        );
        assertEquals(1,

                JSONPath.of("$.child.size()")
                        .eval(
                                JSONObject.of("child",
                                        JSONObject.of("id", 123))
                        )
        );
    }

    @Test
    public void test_set_0() {
        JSONObject root = JSONObject.of("id", 123);
        JSONPath.of("$.id")
                .setInt(root, 101);
        assertEquals(101, root.get("id"));
    }

    @Test
    public void test_set_1() {
        JSONArray root = JSONArray.of(123);
        JSONPath.of("$[0]")
                .setInt(root, 101);
        assertEquals(101, root.get(0));
        JSONPath.of("$[-1]")
                .setInt(root, 102);
        assertEquals(102, root.get(0));
        JSONPath.of("$[-1]")
                .setLong(root, 103);
        assertEquals(103L, root.get(0));
    }

    @Test
    public void test_set_2() {
        Object[] root = new Object[]{123};
        JSONPath.of("$[0]")
                .setInt(root, 101);
        assertEquals(101, root[0]);
        JSONPath.of("$[-1]")
                .setInt(root, 102);
        assertEquals(102, root[0]);
        JSONPath.of("$[-1]")
                .setLong(root, 103);
        assertEquals(103L, root[0]);
    }

    @Test
    public void test_set_3() {
        int[] root = new int[]{123};
        JSONPath.of("$[0]")
                .setInt(root, 101);
        assertEquals(101, root[0]);
        JSONPath.of("$[-1]")
                .setInt(root, 102);
        assertEquals(102, root[0]);
        JSONPath.of("$[-2]")
                .setInt(root, 103);
        assertEquals(102, root[0]);
        JSONPath.of("$[2]")
                .setInt(root, 103);
        assertEquals(102, root[0]);
    }

    @Test
    public void test_set_4() {
        long[] root = new long[]{123};
        JSONPath.of("$[0]")
                .setInt(root, 101);
        assertEquals(101L, root[0]);
        JSONPath.of("$[-1]")
                .setInt(root, 102);
        assertEquals(102L, root[0]);
        JSONPath.of("$[-2]")
                .setInt(root, 103);
        assertEquals(102L, root[0]);
        JSONPath.of("$[2]")
                .setInt(root, 103);
        assertEquals(102L, root[0]);
    }

    @Test
    public void test_set_5() {
        long[] root = new long[]{123};
        JSONPath.of("$[0]")
                .setLong(root, 101);
        assertEquals(101L, root[0]);
        JSONPath.of("$[-1]")
                .setLong(root, 102);
        assertEquals(102L, root[0]);
        JSONPath.of("$[-2]")
                .setLong(root, 103);
        assertEquals(102L, root[0]);
        JSONPath.of("$[2]")
                .setLong(root, 103);
        assertEquals(102L, root[0]);
    }

    @Test
    public void test_set_6() {
        int[] root = new int[]{123};
        JSONPath.of("$[0]")
                .setLong(root, 101);
        assertEquals(101, root[0]);
        JSONPath.of("$[-1]")
                .setLong(root, 102);
        assertEquals(102, root[0]);
        JSONPath.of("$[-2]")
                .setLong(root, 103);
        assertEquals(102, root[0]);
        JSONPath.of("$[2]")
                .setLong(root, 103);
        assertEquals(102, root[0]);
    }

    @Test
    public void test_set_7() {
        JSONArray root = JSONArray.of(
                JSONObject.of("id", 123)
        );

        JSONPath.of("$..id")
                .setLong(root, 101);
        assertEquals(101L, root.getJSONObject(0).get("id"));

        JSONPath.of("$..id")
                .setInt(root, 102);
        assertEquals(102, root.getJSONObject(0).get("id"));
    }

    @Test
    public void test_set_int_0() {
        IntField1 vo = new IntField1();
        JSONPath.of("$.v0000")
                .setInt(vo, 101);
        assertEquals(101, vo.v0000);

        JSONPath.of("$.id")
                .setInt(vo, 102);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_remove_0() {
        JSONObject root = JSONObject
                .of("child", JSONObject.of("id", 123));
        JSONPath.of("$.child.id")
                .remove(root);
        assertEquals(null, root.getJSONObject("child").get("id"));
    }

    @Test
    public void test_remove_error_0() {
        JSONException error = null;
        try {
            JSONPath.of("$.size()")
                    .remove(null);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_set_error_0() {
        JSONException error = null;
        try {
            JSONPath.of("$.size()")
                    .set(null, null);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_paths() {
        Map<String, Object> paths = JSONPath.paths(JSONArray.of(101, UUID.randomUUID()));
        assertEquals(3, paths.size());
    }

    @Test
    public void test_paths_1() {
        Map<String, Object> paths = JSONPath.paths(JSONArray.of(101, 102, UUID.randomUUID()));
        assertEquals(4, paths.size());
    }

    @Test
    public void test_paths_2() {
        Map<String, Object> paths = JSONPath.paths(UUID.randomUUID());
        assertEquals(1, paths.size());
    }

    @Test
    public void test_seg_toString() throws Exception {
        JSONPathMulti path = (JSONPathMulti) JSONPath.of("$..book[?(@.isbn)][0]");
        assertEquals("$..book[?(@.isbn)][0]", path.toString());

        List segments = path.segments;
        assertEquals(3, segments.size());
        assertEquals("..book", segments.get(0).toString());
        assertEquals("?isbn", segments.get(1).toString());
    }

    @Test
    public void test_seg_toString_1() throws Exception {
        JSONPathMulti path = (JSONPathMulti) JSONPath.of("$.book.author.id");
        assertEquals("$.book.author.id", path.toString());

        List segments = path.segments;
        assertEquals(3, segments.size());
        assertEquals("book", segments.get(0).toString());
        assertEquals("author", segments.get(1).toString());
    }
}
