package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * https://github.com/alibaba/fastjson2/issues/7730
 *
 * With Feature.ErrorOnNotSupportAutoType enabled and an autoType whitelist configured
 * (addAutoTypeAccept), deserializing a single object with "@type" works, but the same
 * object nested inside an array throws "autoType not support". The array element path in
 * JSONReader.readArray fell through to the generic readObject() branch, which throws
 * unconditionally without consulting the accept whitelist, whereas the single-object path
 * (ObjectReaderImplObject) does consult it.
 */
@Tag("regression")
public class Issue7730 {
    public static class Bean {
        public String id;

        public Bean() {
        }

        public Bean(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Bean)) {
                return false;
            }
            String a = this.id;
            String b = ((Bean) o).id;
            return a == null ? b == null : a.equals(b);
        }

        @Override
        public int hashCode() {
            return id == null ? 0 : id.hashCode();
        }
    }

    private static final String ACCEPT_PREFIX = "com.alibaba.fastjson2.issues.";

    private static JSONReader.Context context() {
        JSONReader.Context context = JSONFactory.createReadContext(JSONReader.Feature.ErrorOnNotSupportAutoType);
        context.getProvider().addAutoTypeAccept(ACCEPT_PREFIX);
        // container types carry their own @type when written with WriteClassName
        // (e.g. java.util.LinkedHashMap for a root Map), so they must be whitelisted too
        context.getProvider().addAutoTypeAccept("java.util.");
        return context;
    }

    @Test
    public void singleObject() {
        Bean bean = new Bean("test-data");
        String json = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Object parsed = JSON.parse(json, context());
        assertInstanceOf(Bean.class, parsed);
        assertEquals("test-data", ((Bean) parsed).id);
    }

    @Test
    public void list() {
        List<Bean> list = new ArrayList<>(Arrays.asList(new Bean("test-data")));
        String json = JSON.toJSONString(list, JSONWriter.Feature.WriteClassName);
        Object parsed = JSON.parse(json, context());
        assertInstanceOf(List.class, parsed);
        Object element = ((List<?>) parsed).get(0);
        assertInstanceOf(Bean.class, element);
        assertEquals("test-data", ((Bean) element).id);
    }

    @Test
    public void nestedList() {
        List<List<Bean>> nested = new ArrayList<>(
                Arrays.asList(new ArrayList<>(Arrays.asList(new Bean("test-data")))));
        String json = JSON.toJSONString(nested, JSONWriter.Feature.WriteClassName);
        Object parsed = JSON.parse(json, context());
        assertInstanceOf(List.class, parsed);
        Object inner = ((List<?>) parsed).get(0);
        assertInstanceOf(List.class, inner);
        Object element = ((List<?>) inner).get(0);
        assertInstanceOf(Bean.class, element);
        assertEquals("test-data", ((Bean) element).id);
    }

    @Test
    public void listInMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("items", new ArrayList<>(Arrays.asList(new Bean("test-data"))));
        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);
        Object parsed = JSON.parse(json, context());
        assertInstanceOf(Map.class, parsed);
        Object items = ((Map<?, ?>) parsed).get("items");
        assertInstanceOf(List.class, items);
        Object element = ((List<?>) items).get(0);
        assertInstanceOf(Bean.class, element);
        assertEquals("test-data", ((Bean) element).id);
    }

    @Test
    public void notAcceptedTypeInListStillThrows() {
        // The fix must not relax the whitelist: a type outside the accept prefix must still
        // be rejected under ErrorOnNotSupportAutoType, just like the single-object path.
        String json = "[{\"@type\":\"java.io.File\",\"path\":\"/tmp/x\"}]";
        assertThrows(JSONException.class, () -> JSON.parse(json, context()));
    }

    @Test
    public void whitelistedListVsRejectedListConsistency() {
        // Two-sided consistency: a whitelisted element restores, and a non-whitelisted
        // element in an array is rejected. Both paths must apply the accept whitelist
        // the same way — the rejection path is what makes ErrorOnNotSupportAutoType
        // meaningful, so guard it alongside the accept path.
        String accepted = JSON.toJSONString(
                new ArrayList<>(Arrays.asList(new Bean("x"))), JSONWriter.Feature.WriteClassName);
        Object parsed = JSON.parse(accepted, context());
        assertInstanceOf(Bean.class, ((List<?>) parsed).get(0));

        String rejected = "[{\"@type\":\"java.io.File\",\"path\":\"/tmp/x\"}]";
        assertThrows(JSONException.class, () -> JSON.parse(rejected, context()));
    }

    @Test
    public void refInArrayUnderErrorOnNotSupportAutoType() {
        // $ref array element under ErrorOnNotSupportAutoType must still resolve when
        // the caller uses an API that runs handleResolveTasks (e.g. parseObject with
        // a Type). Routing array elements through ObjectReaderImplObject for the
        // autoType whitelist must not bypass the isReference() check — otherwise
        // {"$ref":"$[0]"} would become a literal JSONObject instead of a reference.
        JSONReader.Context ctx = JSONFactory.createReadContext(JSONReader.Feature.ErrorOnNotSupportAutoType);
        ctx.getProvider().addAutoTypeAccept("com.alibaba.fastjson2.issues.");
        ctx.getProvider().addAutoTypeAccept("java.util.");

        String json = "[{\"id\":\"a\"},{\"$ref\":\"$[0]\"}]";
        Object parsed = JSON.parseObject(json, Object.class, ctx);
        List<?> list = (List<?>) parsed;
        assertEquals(list.get(0), list.get(1));
    }
}
