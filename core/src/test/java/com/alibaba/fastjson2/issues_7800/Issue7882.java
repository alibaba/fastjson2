package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("issues")
public class Issue7882 {
    // declaration order (to, from, amount) differs from alphabetical order (amount, from, to)
    @JSONType(alphabetic = false)
    public static class Transfer {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    @Test
    public void textBeanToArrayRoundTrip() {
        Transfer t = new Transfer();
        String json = JSON.toJSONString(t, JSONWriter.Feature.BeanToArray);
        assertEquals("[1001,2002,300]", json);

        Transfer back = JSON.parseObject(json, Transfer.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(1001, back.to);
        assertEquals(2002, back.from);
        assertEquals(300, back.amount);
    }

    @Test
    public void jsonbBeanToArrayRoundTrip() {
        Transfer t = new Transfer();
        byte[] bytes = JSONB.toBytes(t, JSONWriter.Feature.BeanToArray);
        assertEquals("[\n\t1001,\n\t2002,\n\t300\n]", JSONB.toJSONString(bytes));

        Transfer back = JSONB.parseObject(bytes, Transfer.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(1001, back.to);
        assertEquals(2002, back.from);
        assertEquals(300, back.amount);
    }

    @Test
    public void namedModeStillSupportedWhenArrayMappingEnabled() {
        Transfer t = new Transfer();
        String json = JSON.toJSONString(t);
        Transfer back = JSON.parseObject(json, Transfer.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(1001, back.to);
        assertEquals(2002, back.from);
        assertEquals(300, back.amount);
    }

    // field-backed properties: the fields walk fixes positions on both sides
    // even when accessors are merged in for some of the fields
    @JSONType(alphabetic = false)
    public static class Accessors {
        public long zz = 1;
        public long aa = 2;

        public long getZz() {
            return zz;
        }

        public void setZz(long v) {
            this.zz = v;
        }
    }

    @Test
    public void accessorsBeanToArrayRoundTrip() {
        Accessors t = new Accessors();
        String json = JSON.toJSONString(t, JSONWriter.Feature.BeanToArray);
        assertEquals("[1,2]", json);

        Accessors back = JSON.parseObject(json, Accessors.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(1, back.getZz());
        assertEquals(2, back.aa);
    }

    @JSONType(alphabetic = false)
    public static class Base {
        public long zeta = 11;
    }

    @JSONType(alphabetic = false)
    public static class Sub extends Base {
        public long alpha = 22;
    }

    @Test
    public void inheritedFieldsBeanToArrayRoundTrip() {
        Sub t = new Sub();
        String json = JSON.toJSONString(t, JSONWriter.Feature.BeanToArray);
        assertEquals("[11,22]", json);

        Sub back = JSON.parseObject(json, Sub.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(11, back.zeta);
        assertEquals(22, back.alpha);
    }

    // default alphabetic = true beans keep alphabetical positions on both sides
    public static class DefaultOrder {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    @Test
    public void defaultAlphabeticBeanToArrayRoundTrip() {
        DefaultOrder t = new DefaultOrder();
        String json = JSON.toJSONString(t, JSONWriter.Feature.BeanToArray);
        assertEquals("[300,2002,1001]", json);

        DefaultOrder back = JSON.parseObject(json, DefaultOrder.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(1001, back.to);
        assertEquals(2002, back.from);
        assertEquals(300, back.amount);
    }
}
