package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.*;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * @author 张治保
 * @since 2024/3/13
 */
public class EmptyStringAsNullTest {
    @Test
    void testString() {
        String json = "\"\"";
        //""
        String value = JSON.parseObject(json, String.class);
        Assertions.assertEquals("", value);
        //null
        value = JSON.parseObject(json, String.class, JSONReader.Feature.EmptyStringAsNull);
        Assertions.assertNull(value);

        //reset json
        json = "\"  \"";
        //"  "
        value = JSON.parseObject(json, String.class);
        Assertions.assertEquals("  ", value);
        //"  "
        value = JSON.parseObject(json, String.class, JSONReader.Feature.EmptyStringAsNull);
        Assertions.assertEquals("  ", value);

        //""
        value = JSON.parseObject(json, String.class, JSONReader.Feature.TrimString);
        Assertions.assertEquals("", value);

        //null
        value = JSON.parseObject(json, String.class, JSONReader.Feature.TrimString, JSONReader.Feature.EmptyStringAsNull);
        Assertions.assertNull(value);
    }

    @Test
    void testBean() {
        //test String field& map field
        String beanJson = "{\"name\":\"\",\"map\":{\"emptyValue\":\"\"}}";
        StringBean bean = JSON.parseObject(beanJson, StringBean.class, JSONReader.Feature.EmptyStringAsNull);
        Assertions.assertNotNull(bean);
        Assertions.assertNull(bean.getName());
        Map<String, String> map = bean.getMap();
        Assertions.assertFalse(map == null || map.isEmpty());
        Assertions.assertNull(map.get("emptyValue"));

        //test list field
        //todo collection 是否需要过滤空值
        beanJson = "{\"list\":[\"emptyValue\",\"\"]}";
        bean = JSON.parseObject(beanJson, StringBean.class, JSONReader.Feature.EmptyStringAsNull);
        Assertions.assertNotNull(bean);
        List<String> list = bean.getList();
        Assertions.assertFalse(list == null || list.size() != 2);
        Assertions.assertEquals("emptyValue", list.get(0));
        Assertions.assertNull(list.get(1));

        //test set field
        beanJson = "{\"set\":[\"emptyValue\",\"\"]}";
        bean = JSON.parseObject(beanJson, StringBean.class, JSONReader.Feature.EmptyStringAsNull);
        Assertions.assertNotNull(bean);
        Set<String> set = bean.getSet();
        Assertions.assertFalse(set == null || set.size() != 1);
        Assertions.assertTrue(set.contains("emptyValue"));
        System.out.println(1);

        beanJson = "{\"treeSet\":[\"emptyValue\",\"\"]}";
        bean = JSON.parseObject(beanJson, StringBean.class, JSONReader.Feature.EmptyStringAsNull);

        Assertions.assertNotNull(bean);
        Set<String> treeSet = bean.getTreeSet();
        Assertions.assertFalse(treeSet == null || treeSet.size() != 1);
        Assertions.assertTrue(treeSet.contains("emptyValue"));
    }

    @Test
    void testJSONB() {
        byte[] jsonbBytes = JSONObject.of("value", "   ").toJSONBBytes();
        Assertions.assertNull(
                JSONB.parseObject(
                        new ByteArrayInputStream(jsonbBytes),
                        new JSONReader.Context(JSONReader.Feature.TrimString, JSONReader.Feature.EmptyStringAsNull)
                ).getString("value")
        );

        jsonbBytes = JSONObject.of("value", "").toJSONBBytes();
        Assertions.assertNull(
                JSONB.parseObject(
                        new ByteArrayInputStream(jsonbBytes),
                        new JSONReader.Context(JSONReader.Feature.EmptyStringAsNull)
                ).getString("value")
        );
    }

    @Data
    private static class StringBean {
        private String name;
        private Map<String, String> map;
        private List<String> list;
        private Set<String> set;
        private TreeSet<String> treeSet;
    }
}
