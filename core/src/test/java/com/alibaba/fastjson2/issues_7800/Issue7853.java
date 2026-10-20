package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7853 {
    public static class PagerBean {
        public String name;
    }

    public static class PagerDataBean<T> {
        public List<T> rows;
        public long total;
        public PagerBean pager;
    }

    public static class ResponseResult<T> {
        public int code;
        public String msg;
        public T result;
    }

    @Test
    public void test() {
        // first call binds the cached writer of the "result" field to PagerDataBean
        PagerDataBean<String> pagerData = new PagerDataBean<>();
        pagerData.rows = Collections.singletonList("x");
        pagerData.total = 1;

        ResponseResult<PagerDataBean<String>> pagerResult = new ResponseResult<>();
        pagerResult.code = 0;
        pagerResult.msg = "ok";
        pagerResult.result = pagerData;
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":{\"rows\":[\"x\"],\"total\":1}}",
                JSON.toJSONString(pagerResult));
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":{\"rows\":[\"x\"],\"total\":1}}",
                JSON.toJSON(pagerResult).toString());

        // then a different generic argument must not reuse the cached writer (ClassCastException)
        ResponseResult<Boolean> boolResult = new ResponseResult<>();
        boolResult.code = 0;
        boolResult.msg = "ok";
        boolResult.result = Boolean.TRUE;
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":true}",
                JSON.toJSON(boolResult).toString());
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":true}",
                JSON.toJSONString(boolResult));

        // a PagerDataBean with a different row type still serializes correctly
        PagerDataBean<Integer> intPagerData = new PagerDataBean<>();
        intPagerData.rows = Collections.singletonList(1);

        ResponseResult<PagerDataBean<Integer>> intResult = new ResponseResult<>();
        intResult.code = 0;
        intResult.msg = "ok";
        intResult.result = intPagerData;
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":{\"rows\":[1],\"total\":0}}",
                JSON.toJSON(intResult).toString());
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":{\"rows\":[1],\"total\":0}}",
                JSON.toJSONString(intResult));
    }

    public static class Base {
        public String a;
    }

    public static class Sub extends Base {
        public String b;
    }

    public static class Holder {
        public Object value;
    }

    /**
     * The guard must apply the same rule as the write path (exact class match, widened only for
     * writeUsing / Map / List). A looser isAssignableFrom check would reuse the superclass writer
     * for a subclass value and silently drop the subclass-only field.
     */
    @Test
    public void testSubclassFieldsAreNotDropped() {
        Base base = new Base();
        base.a = "A";
        Holder baseHolder = new Holder();
        baseHolder.value = base;
        assertEquals("{\"value\":{\"a\":\"A\"}}", JSON.toJSONString(baseHolder));

        Sub sub = new Sub();
        sub.a = "A";
        sub.b = "B";
        Holder subHolder = new Holder();
        subHolder.value = sub;

        String expected = JSON.toJSONString(subHolder);
        assertTrue(expected.contains("\"b\":\"B\""), expected);
        assertEquals(expected, JSON.toJSON(subHolder).toString());
    }

    public static class ScalarFirstResult<T> {
        public int code;
        public String msg;
        public T result;
    }

    /**
     * The reverse cache-poisoning order: the field is first bound to a scalar (Boolean) writer,
     * then written with a bean value. The bean must still be converted to a JSONObject, otherwise
     * the caller's cast throws ClassCastException.
     */
    @Test
    public void testScalarFirstThenBean() {
        ScalarFirstResult<Boolean> boolResult = new ScalarFirstResult<>();
        boolResult.code = 0;
        boolResult.msg = "ok";
        boolResult.result = Boolean.TRUE;
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":true}", JSON.toJSONString(boolResult));
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":true}", JSON.toJSON(boolResult).toString());

        PagerDataBean<String> pagerData = new PagerDataBean<>();
        pagerData.rows = Collections.singletonList("x");
        pagerData.total = 1;

        ScalarFirstResult<PagerDataBean<String>> pagerResult = new ScalarFirstResult<>();
        pagerResult.code = 0;
        pagerResult.msg = "ok";
        pagerResult.result = pagerData;

        JSONObject json = (JSONObject) JSON.toJSON(pagerResult);
        assertEquals("{\"code\":0,\"msg\":\"ok\",\"result\":{\"rows\":[\"x\"],\"total\":1}}", json.toString());
        assertTrue(json.get("result") instanceof JSONObject);
    }
}
