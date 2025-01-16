package com.alibaba.fastjson.serializer.filters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class PropertyPrefFilterTest_IntegerKey
        extends TestCase {
    public void test_namefilter() throws Exception {
        PropertyPreFilter filter = (serializer, object, name) -> name.equals("1001");

        Map map = new HashMap();
        map.put(1001, 0);
        map.put(1002, 1);

        String text = JSON.toJSONString(map, filter);

        Assert.assertEquals("{1001:0}", text);
    }
}
