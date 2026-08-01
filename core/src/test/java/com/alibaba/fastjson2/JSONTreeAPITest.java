package com.alibaba.fastjson2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSONObject / JSONArray 树 API 全面测试。
 * 覆盖:数字/布尔/字符串/null/数组/对象 全部类型、多层嵌套、序列化回环、
 * 转义字符、数字边界、工厂方法、集合操作、PrettyFormat。
 */
@Tag("json")
public class JSONTreeAPITest {

    // ---------- 1. 基础标量类型 ----------

    @Test
    public void parseBasicTypes() {
        JSONObject obj = JSON.parseObject("{\"int\":42,\"long\":9223372036854775807,\"double\":3.14,\"bool\":true,\"str\":\"hello\",\"neg\":-17,\"null\":null}");

        assertEquals(42, obj.getInteger("int"));
        assertEquals(42, obj.getIntValue("int"));
        assertEquals(9223372036854775807L, obj.getLong("long"));
        assertEquals(9223372036854775807L, obj.getLongValue("long"));
        assertEquals(3.14, obj.getDouble("double"));
        assertEquals(3.14, obj.getDoubleValue("double"));
        assertTrue(obj.getBoolean("bool"));
        assertTrue(obj.getBooleanValue("bool"));
        assertEquals("hello", obj.getString("str"));
        assertEquals(-17, obj.getIntValue("neg"));
        assertNull(obj.get("null"));

        // 类型应为精确的装箱类型
        assertEquals(Integer.class, obj.get("int").getClass());
        assertEquals(Long.class, obj.get("long").getClass());
        assertEquals(BigDecimal.class, obj.get("double").getClass());
        assertEquals(Boolean.class, obj.get("bool").getClass());
        assertEquals(String.class, obj.get("str").getClass());
    }

    @Test
    public void parseArrayBasicTypes() {
        JSONArray arr = JSON.parseArray("[1,2.5,\"x\",true,null,[3],{\"k\":\"v\"}]");
        assertEquals(7, arr.size());
        assertEquals(1, arr.getIntValue(0));
        assertEquals(2.5, arr.getDouble(1));
        assertEquals("x", arr.getString(2));
        assertTrue(arr.getBooleanValue(3));
        assertNull(arr.get(4));
        assertEquals(3, arr.getJSONArray(5).getIntValue(0));
        assertEquals("v", arr.getJSONObject(6).getString("k"));
    }

    // ---------- 2. 数字边界与精度 ----------

    @Test
    public void parseNumberBoundaries() {
        JSONObject obj = JSON.parseObject("{\"min\":-9223372036854775808,\"max\":9223372036854775807,\"d\":0.1,\"big\":123456789012345678901234567890,\"sci\":1e10,\"neg\":-0.001}");

        assertEquals(Long.MIN_VALUE, obj.getLong("min"));
        assertEquals(Long.MAX_VALUE, obj.getLong("max"));
        assertEquals(0.1, obj.getDouble("d"));
        // 超出 long 范围的整数 -> BigInteger
        assertEquals(new BigInteger("123456789012345678901234567890"), obj.getBigInteger("big"));
        assertEquals(1e10, obj.getDouble("sci"));
        assertEquals(-0.001, obj.getDouble("neg"));
    }

    @Test
    public void parseBigDecimalPrecision() {
        JSONObject obj = JSON.parseObject("{\"v\":123.45678901234567890}");
        BigDecimal decimal = obj.getBigDecimal("v");
        assertEquals("123.45678901234567890", decimal.toString());

        JSONObject obj2 = JSON.parseObject("{\"v\":\"9007199254740993\"}");
        // 字符串形式的大整数保持精度
        assertEquals(new BigInteger("9007199254740993"), obj2.getBigInteger("v"));
    }

    @Test
    public void floatAndByteShorts() {
        JSONObject obj = JSON.parseObject("{\"b\":127,\"s\":32767,\"f\":1.5}");
        assertEquals((byte) 127, obj.getByteValue("b"));
        assertEquals(Byte.valueOf((byte) 127), obj.getByte("b"));
        assertEquals((short) 32767, obj.getShortValue("s"));
        assertEquals(Short.valueOf((short) 32767), obj.getShort("s"));
        assertEquals(1.5f, obj.getFloat("f"));
        assertEquals(1.5f, obj.getFloatValue("f"));
    }

    // ---------- 3. 字符串转义 ----------

    @Test
    public void parseStringEscapes() {
        assertEquals("a\"b", JSON.parseObject("{\"s\":\"a\\\"b\"}").getString("s"));
        assertEquals("line1\nline2", JSON.parseObject("{\"s\":\"line1\\nline2\"}").getString("s"));
        assertEquals("tab\there", JSON.parseObject("{\"s\":\"tab\\there\"}").getString("s"));
        assertEquals("C:\\path", JSON.parseObject("{\"s\":\"C:\\\\path\"}").getString("s"));
        assertEquals("中文", JSON.parseObject("{\"s\":\"\\u4e2d\\u6587\"}").getString("s"));
        assertEquals("中文", JSON.parseObject("{\"s\":\"中文\"}").getString("s"));
        assertEquals("back\\slash", JSON.parseObject("{\"s\":\"back\\\\slash\"}").getString("s"));
        assertEquals("", JSON.parseObject("{\"s\":\"\"}").getString("s"));
    }

    @Test
    public void serializeStringEscapes() {
        JSONObject obj = JSONObject.of("s", "a\"b\nc\\d\te");
        String json = JSON.toJSONString(obj);
        // 序列化 -> 反序列化 回环一致
        assertEquals("a\"b\nc\\d\te", JSON.parseObject(json).getString("s"));
    }

    // ---------- 4. 多层嵌套 ----------

    @Test
    public void parseNestedObject() {
        JSONObject obj = JSON.parseObject("{\"a\":{\"b\":{\"c\":{\"d\":42}}}}");
        assertEquals(42, obj.getJSONObject("a").getJSONObject("b").getJSONObject("c").getIntValue("d"));
        assertNotNull(obj.getJSONObject("a").getJSONObject("b"));
        assertNull(obj.getJSONObject("a").getJSONObject("missing"));
    }

    @Test
    public void parseNestedArrays() {
        JSONObject obj = JSON.parseObject("{\"matrix\":[[1,2],[3,4]],\"items\":[{\"id\":1},{\"id\":2}],\"mixed\":[1,[2,[3]]]}");
        JSONArray matrix = obj.getJSONArray("matrix");
        assertEquals(1, matrix.getJSONArray(0).getIntValue(0));
        assertEquals(4, matrix.getJSONArray(1).getIntValue(1));
        assertEquals(2, obj.getJSONArray("items").getJSONObject(1).getIntValue("id"));
        assertEquals(3, obj.getJSONArray("mixed").getJSONArray(1).getJSONArray(1).getIntValue(0));
    }

    @Test
    public void parseDeepMixedNesting() {
        // 4 层混合嵌套:对象 -> 数组 -> 对象 -> 数组
        JSONObject root = JSON.parseObject("{\"level1\":{\"level2\":[{\"level3\":{\"level4\":[true,false,null]}}]}}");
        JSONObject l1 = root.getJSONObject("level1");
        JSONArray l2 = l1.getJSONArray("level2");
        JSONObject l3 = l2.getJSONObject(0).getJSONObject("level3");
        JSONArray l4 = l3.getJSONArray("level4");
        assertEquals(3, l4.size());
        assertTrue(l4.getBooleanValue(0));
        assertFalse(l4.getBooleanValue(1));
        assertNull(l4.get(2));
    }

    // ---------- 5. JSONObject 集合操作 ----------

    @Test
    public void jsonObjectOps() {
        JSONObject obj = new JSONObject();
        assertTrue(obj.isEmpty());
        assertEquals(0, obj.size());

        obj.put("a", 1);
        obj.put("b", "x");
        obj.put("c", true);
        assertEquals(3, obj.size());
        assertFalse(obj.isEmpty());
        assertTrue(obj.containsKey("a"));
        assertFalse(obj.containsKey("z"));
        assertEquals(1, obj.get("a"));
        assertEquals("x", obj.get("b"));

        // keySet / values / entrySet
        Set<String> keys = new HashSet<>(obj.keySet());
        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")), keys);
        assertEquals(3, obj.values().size());
        assertEquals(3, obj.entrySet().size());
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            assertEquals(entry.getValue(), obj.get(entry.getKey()));
        }

        // remove
        assertEquals(1, obj.remove("a"));
        assertNull(obj.remove("a"));
        assertEquals(2, obj.size());

        // getOrDefault
        assertEquals("x", obj.getOrDefault("b", "d"));
        assertEquals("d", obj.getOrDefault("missing", "d"));

        // clear
        obj.clear();
        assertTrue(obj.isEmpty());
        assertEquals(0, obj.size());
    }

    @Test
    public void jsonObjectFluentPut() {
        JSONObject obj = new JSONObject().fluentPut("a", 1).fluentPut("b", 2);
        assertEquals(2, obj.size());
        assertEquals(1, obj.getIntValue("a"));
        assertEquals(2, obj.getIntValue("b"));
    }

    @Test
    public void jsonObjectPutArrayPutObject() {
        JSONObject obj = new JSONObject();
        JSONArray list = obj.putArray("list");
        list.add(1);
        list.add(2);
        obj.putObject("sub").put("k", "v");
        assertEquals(2, obj.size());
        assertEquals(2, obj.getJSONArray("list").size());
        assertEquals("v", obj.getJSONObject("sub").getString("k"));

        JSONArray arr = new JSONArray();
        arr.addArray().add("x");
        arr.addObject().put("y", 1);
        assertEquals(2, arr.size());
        assertEquals("x", arr.getJSONArray(0).getString(0));
        assertEquals(1, arr.getJSONObject(1).getIntValue("y"));
    }

    // ---------- 6. JSONArray 集合操作 ----------

    @Test
    public void jsonArrayOps() {
        JSONArray arr = new JSONArray();
        assertTrue(arr.isEmpty());
        arr.add(1);
        arr.add("s");
        arr.add(true);
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0));
        assertEquals("s", arr.get(1));
        assertTrue(arr.getBoolean(2));

        // set
        arr.set(0, 100);
        assertEquals(100, arr.getIntValue(0));

        // remove
        assertEquals("s", arr.remove(1));
        assertEquals(2, arr.size());

        // fluent
        arr.fluentAdd(3).fluentAdd(4);
        assertEquals(4, arr.size());
        arr.fluentRemove(0);
        assertEquals(3, arr.size());
        arr.fluentSet(0, 999);
        assertEquals(999, arr.getIntValue(0));

        // clear
        arr.clear();
        assertTrue(arr.isEmpty());
    }

    // ---------- 7. 类型化 getter 与转换 ----------

    @Test
    public void typedGetters() {
        JSONObject obj = JSON.parseObject("{\"i\":42,\"l\":99,\"d\":2.5,\"b\":true,\"s\":\"7\",\"big\":12345678901234567890123}");

        // getString 对数字/布尔转换为字符串
        assertEquals("42", obj.getString("i"));
        assertEquals("99", obj.getString("l"));
        assertEquals("2.5", obj.getString("d"));
        assertEquals("true", obj.getString("b"));

        // 字符串数字 -> 数值
        assertEquals(7, obj.getInteger("s"));
        assertEquals(7L, obj.getLongValue("s"));

        // 数值 -> 其他数值类型
        assertEquals(42L, obj.getLongValue("i"));
        assertEquals(42.0, obj.getDoubleValue("i"));
        assertEquals(99, obj.getIntValue("l"));
        assertEquals(2.5, obj.getFloatValue("d"));

        // BigInteger / BigDecimal
        assertEquals(new BigInteger("12345678901234567890123"), obj.getBigInteger("big"));
        assertEquals(new BigDecimal("42"), obj.getBigDecimal("i"));
    }

    @Test
    public void getWithDefaultValues() {
        JSONObject obj = JSON.parseObject("{\"a\":5}");
        assertEquals(0, obj.getIntValue("missing", 0));
        assertEquals(100, obj.getIntValue("missing", 100));
        assertEquals(5, obj.getIntValue("a", 100));
        assertEquals("d", obj.getString("missing", "d"));
        assertFalse(obj.getBooleanValue("missing", false));
    }

    // ---------- 8. 序列化与回环 ----------

    @Test
    public void serializeRoundTrip() {
        JSONObject obj = new JSONObject();
        obj.put("id", 1);
        obj.put("name", "张三");
        obj.put("score", 98.5);
        obj.put("pass", true);
        obj.put("tags", JSONArray.of("a", "b", "c"));
        obj.put("address", JSONObject.of("city", "北京", "zip", "100000"));
        obj.put("nothing", null);

        String json = JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue);
        assertFalse(json.contains("\n"));
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"张三\""));
        assertTrue(json.contains("\"nothing\":null"));

        // 回环
        JSONObject back = JSON.parseObject(json);
        assertEquals(1, back.getIntValue("id"));
        assertEquals("张三", back.getString("name"));
        assertEquals(98.5, back.getDoubleValue("score"));
        assertTrue(back.getBooleanValue("pass"));
        assertEquals("c", back.getJSONArray("tags").getString(2));
        assertEquals("北京", back.getJSONObject("address").getString("city"));
        assertTrue(back.containsKey("nothing"));
        assertNull(back.get("nothing"));
    }

    @Test
    public void arraySerializeRoundTrip() {
        JSONArray arr = new JSONArray();
        arr.add(1);
        arr.add("x");
        arr.add(true);
        arr.add(JSONObject.of("k", "v"));
        arr.add(JSONArray.of(1, 2, 3));

        String json = JSON.toJSONString(arr);
        JSONArray back = JSON.parseArray(json);
        assertEquals(5, back.size());
        assertEquals(1, back.getIntValue(0));
        assertEquals("x", back.getString(1));
        assertTrue(back.getBooleanValue(2));
        assertEquals("v", back.getJSONObject(3).getString("k"));
        assertEquals(3, back.getJSONArray(4).getIntValue(2));
    }

    @Test
    public void nestedSerializeRoundTrip() {
        // 构造 4 层嵌套,序列化后回环,逐层验证
        JSONObject root = JSONObject.of("user", JSONObject.of(
                "name", "Alice",
                "profile", JSONObject.of(
                        "contact", JSONObject.of(
                                "emails", JSONArray.of("a@x.com", "b@x.com"),
                                "phones", JSONArray.of(JSONObject.of("type", "home", "num", "111"), JSONObject.of("type", "work", "num", "222"))
                        )
                ),
                "roles", JSONArray.of("admin", "dev")
        ));

        JSONObject back = JSON.parseObject(JSON.toJSONString(root));
        assertEquals("Alice", back.getJSONObject("user").getString("name"));
        assertEquals("b@x.com", back.getJSONObject("user")
                .getJSONObject("profile").getJSONObject("contact")
                .getJSONArray("emails").getString(1));
        assertEquals("work", back.getJSONObject("user")
                .getJSONObject("profile").getJSONObject("contact")
                .getJSONArray("phones").getJSONObject(1).getString("type"));
        assertEquals("222", back.getJSONObject("user")
                .getJSONObject("profile").getJSONObject("contact")
                .getJSONArray("phones").getJSONObject(1).getString("num"));
        assertEquals("dev", back.getJSONObject("user").getJSONArray("roles").getString(1));
    }

    // ---------- 9. PrettyFormat ----------

    @Test
    public void prettyFormat() {
        JSONObject obj = JSONObject.of("a", 1, "b", JSONArray.of(1, 2), "c", JSONObject.of("d", true));
        String pretty = JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);

        assertTrue(pretty.contains("\n"), "PrettyFormat 应包含换行: " + pretty);
        assertTrue(pretty.contains("\"a\":1"));

        // 回环一致性
        JSONObject back = JSON.parseObject(pretty);
        assertEquals(1, back.getIntValue("a"));
        assertEquals(2, back.getJSONArray("b").getIntValue(1));
        assertTrue(back.getJSONObject("c").getBooleanValue("d"));
    }

    // ---------- 10. 空结构 ----------

    @Test
    public void emptyStructures() {
        JSONObject empty = JSON.parseObject("{}");
        assertTrue(empty.isEmpty());
        assertEquals("{}", JSON.toJSONString(empty));

        JSONArray emptyArr = JSON.parseArray("[]");
        assertTrue(emptyArr.isEmpty());
        assertEquals("[]", JSON.toJSONString(emptyArr));

        JSONObject nested = JSON.parseObject("{\"a\":{},\"b\":[],\"c\":null}");
        assertTrue(nested.getJSONObject("a").isEmpty());
        assertTrue(nested.getJSONArray("b").isEmpty());
        assertNull(nested.get("c"));
    }

    // ---------- 11. 工厂方法 ----------

    @Test
    public void factoryMethods() {
        assertEquals(1, JSONObject.of("a", 1).getIntValue("a"));
        assertEquals(2, JSONObject.of("a", 1, "b", 2).getIntValue("b"));
        assertEquals(3, JSONObject.of("a", 1, "b", 2, "c", 3).getIntValue("c"));

        JSONArray arr = JSONArray.of(1, "x", true);
        assertEquals(3, arr.size());
        assertEquals("x", arr.getString(1));
        assertTrue(arr.getBooleanValue(2));

        JSONArray arr2 = JSONArray.of(1, 2);
        assertEquals(2, arr2.size());
    }

    // ---------- 12. null 与 missing 语义 ----------

    @Test
    public void nullAndMissing() {
        JSONObject obj = JSON.parseObject("{\"a\":null,\"b\":0,\"c\":false,\"d\":\"\"}");

        assertNull(obj.get("a"));
        assertNull(obj.getInteger("a"));
        assertNull(obj.getString("a"));

        assertEquals(0, obj.getIntValue("b"));
        assertFalse(obj.getBooleanValue("c"));
        assertEquals("", obj.getString("d"));

        // 缺失 key
        assertNull(obj.get("missing"));
        assertNull(obj.getInteger("missing"));
        assertNull(obj.getJSONObject("missing"));
        assertNull(obj.getJSONArray("missing"));
        assertNull(obj.getString("missing"));
        assertEquals(0, obj.getIntValue("missing"));
    }

    // ---------- 13. 单引号与宽容解析 ----------

    @Test
    public void singleQuoteParse() {
        JSONObject obj = JSON.parseObject("{'name':'John','age':30}");
        assertEquals("John", obj.getString("name"));
        assertEquals(30, obj.getIntValue("age"));
    }

    // ---------- 14. JSON.parse 通用入口 ----------

    @Test
    public void parseGeneric() {
        Object o1 = JSON.parse("{\"k\":\"v\"}");
        assertTrue(o1 instanceof JSONObject);
        Object o2 = JSON.parse("[1,2]");
        assertTrue(o2 instanceof JSONArray);
        Object o3 = JSON.parse("\"str\"");
        assertEquals("str", o3);
        Object o4 = JSON.parse("123");
        assertEquals(123, o4);
        Object o5 = JSON.parse("true");
        assertEquals(true, o5);
        Object o6 = JSON.parse("null");
        assertNull(o6);
    }

    // ---------- 15. JSONArray 类型化 getter 全量 ----------

    @Test
    public void arrayTypedGetters() {
        JSONArray arr = JSON.parseArray("[\"42\",2.5,true,7,12345678901234567890123]");
        assertEquals(42, arr.getInteger(0));
        assertEquals(2.5, arr.getDouble(1));
        assertTrue(arr.getBoolean(2));
        assertEquals(7L, arr.getLongValue(3));
        assertEquals(new BigInteger("12345678901234567890123"), arr.getBigInteger(4));
        assertEquals("42", arr.getString(0));
    }

    // ---------- 16. clone 与引用语义 ----------

    @Test
    public void cloneBehavior() {
        JSONObject obj = JSONObject.of("a", 1, "b", JSONArray.of(1, 2));
        JSONObject clone = obj.clone();
        assertEquals(1, clone.getIntValue("a"));
        assertEquals(2, clone.getJSONArray("b").getIntValue(1));
        // 修改原对象不影响 clone 的 Map 结构
        obj.put("c", 3);
        assertFalse(clone.containsKey("c"));
    }

    // ---------- 17. toString 与 toJSONString ----------

    @Test
    public void toStringEqualsToJSONString() {
        JSONObject obj = JSONObject.of("a", 1, "b", "x");
        assertEquals(obj.toString(), JSON.toJSONString(obj));
        assertEquals(obj.toJSONString(), JSON.toJSONString(obj));

        JSONArray arr = JSONArray.of(1, "x");
        assertEquals(arr.toString(), JSON.toJSONString(arr));
    }
}
