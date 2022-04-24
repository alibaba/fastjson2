package com.alibaba.fastjson.issue_3800;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * https://github.com/alibaba/fastjson/issues/3810
 *
 * @author hnyyghk
 * @date 2021/06/30 18:40
 */
public class Issue3810 {
    @Data
    static class TestA<T> {
        T a;
    }

    @Data
    static class TestB {
        String b;
    }

    private static final String json = "{\"a\":[{\"b\":\"b\"}]}";

    @Test
    public void test_for_issue() throws Exception {
        ParameterizedTypeImpl inner = new ParameterizedTypeImpl(new Type[]{TestB.class}, List.class, List.class);
        ParameterizedTypeImpl outer = new ParameterizedTypeImpl(new Type[]{inner}, TestA.class, TestA.class);
        JSONObject jo = JSONObject.parseObject(json);
        TestA<List<?>> ret = jo.toJavaObject(outer);

        assertTrue(List.class.isInstance(ret.getA()));
        assertEquals(TestB.class.getName(), ret.getA().get(0).getClass().getName());
    }
}
