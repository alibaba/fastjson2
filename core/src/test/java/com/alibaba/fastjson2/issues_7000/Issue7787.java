package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7787 {
    @Test
    public void stringSetterFirst() throws Exception {
        assertUnchanged(new OrderedCreator().parse(AjaxResult.class, String.class, Boolean.class));
    }

    @Test
    public void booleanSetterFirst() throws Exception {
        assertUnchanged(new OrderedCreator().parse(AjaxResult.class, Boolean.class, String.class));
    }

    @Test
    public void roundTrip() {
        AjaxResult value = new AjaxResult();
        assertUnchanged(JSON.parseObject(JSON.toJSONString(value), AjaxResult.class));
        assertUnchanged(JSONB.parseObject(JSONB.toBytes(value), AjaxResult.class));
    }

    @Test
    public void explicitAnnotationKeepsPriority() throws Exception {
        OrderedCreator creator = new OrderedCreator();
        assertTrue(creator.parse(AnnotatedResult.class, String.class, Boolean.class).success);
        assertTrue(creator.parse(AnnotatedResult.class, Boolean.class, String.class).success);
    }

    @Test
    public void mixinReaderKeepsPriority() throws Exception {
        for (Class<?>[] order : new Class<?>[][]{
                {String.class, Boolean.class}, {Boolean.class, String.class}
        }) {
            ObjectReaderProvider provider = new ObjectReaderProvider();
            provider.mixIn(AjaxResult.class, ResultMixin.class);
            AjaxResult value = new OrderedCreator().parse(provider, AjaxResult.class, order);
            assertTrue(value.getSuccess());
            assertEquals("custom:false", value.getMsg());
        }
    }

    private static void assertUnchanged(AjaxResult value) {
        assertFalse(value.getSuccess());
        assertEquals("insufficient balance", value.getMsg());
        assertEquals("4102", value.getCode());
    }

    static class OrderedCreator extends ObjectReaderCreator {
        <T> T parse(Class<T> type, Class<?>... setterTypes) throws Exception {
            return parse(new ObjectReaderProvider(), type, setterTypes);
        }

        <T> T parse(ObjectReaderProvider provider, Class<T> type, Class<?>... setterTypes) throws Exception {
            Map<String, List<FieldReader>> fields = new LinkedHashMap<>();
            BeanInfo beanInfo = new BeanInfo(provider);
            for (Class<?> setterType : setterTypes) {
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.init();
                createFieldReader(type, type, null, null, beanInfo, fieldInfo,
                        type.getMethod("setSuccess", setterType), fields, provider);
            }
            FieldReader[] readers = fields.get("success").toArray(new FieldReader[0]);
            provider.register(type, createObjectReader(type, readers));
            return JSON.parseObject("{\"success\":false}", type, new JSONReader.Context(provider));
        }
    }

    public static class AjaxResult {
        private Boolean success = false;
        private String msg = "insufficient balance";
        private String code = "4102";

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public void setSuccess(String msg) {
            this.success = true;
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class AnnotatedResult {
        private Boolean success;

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        @JSONField
        public void setSuccess(String success) {
            this.success = true;
        }
    }

    public interface ResultMixin {
        @JSONField(deserializeUsing = SuccessReader.class)
        void setSuccess(String msg);
    }

    public static class SuccessReader implements ObjectReader<String> {
        @Override
        public String readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return "custom:" + jsonReader.readString();
        }
    }
}
