package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue139 {
    @Test
    public void test() {
        String str = "{\n" + "  \"code\": 1,\n" + "  \"message\": \"success\",\n" + "  \"data\": [\n" + "    {\n" + "      \"date\": \"2022-04-29T00:00:00+08:00\",\n" + "      \"value\": 1.01\n" + "    },\n" + "    {\n" + "      \"date\": \"2022-04-28T00:00:00+08:00\",\n" + "      \"value\": 1.02\n" + "    },\n" + "    {\n" + "      \"date\": \"2022-04-27T00:00:00+08:00\",\n" + "      \"value\": 1.03\n" + "    },\n" + "    {\n" + "      \"date\": \"2022-04-26T00:00:00+08:00\",\n" + "      \"value\": 1.03\n" + "    }\n" + "  ]\n" + "}";

        JSONObject jsonObject = JSON.parseObject(str);

        Bean bean = jsonObject.toJavaObject(Bean.class);
        Bean bean1 = JSON.toJavaObject(jsonObject, Bean.class);

        assertNotNull(bean);

        // 可以打印对象tostring
//        System.out.println(bean.toString());
//        System.out.println(bean1.toString());

        //可以输出对象中的list的size
        assertEquals(4, bean.getData().size());

        //不能foreach java.lang.ClassCastException: class com.alibaba.fastjson2.JSONObject cannot be cast to class
        //Issue128$Item are in unnamed module of loader 'app'
        bean1.getData().forEach(k -> {
            assertTrue(k.getValue() instanceof BigDecimal);
        });

        List<Item> items = bean.getData();

        items.forEach(item -> {
            assertTrue(item instanceof Item);
        });
    }

    public static class Bean {
        public int code;
        public String message;
        List<Item> data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Item> getData() {
            return data;
        }

        public void setData(List<Item> data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Bean{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
        }
    }

    public static class Item {
        private Date date;
        private BigDecimal value;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Item{" + "date=" + date + ", value=" + value + '}';
        }
    }
}
