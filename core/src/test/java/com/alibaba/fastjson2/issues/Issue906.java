package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue906 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.type = Type.Big;

        ValueFilter valueFilter = (Object object, String name, Object value) -> {
            if (value instanceof Type) {
                switch ((Type) value) {
                    case Big:
                        return "大";
                    case Small:
                        return "小";
                    default:
                        break;
                }
            }
            return value;
        };
        String json = JSON.toJSONString(bean, valueFilter);
        assertEquals("{\"type\":\"大\"}", json);
    }

    public enum Type {
        Big(101),
        Small(102);

        public final int code;

        Type(int code) {
            this.code = code;
        }
    }

    public static class Bean {
        public Type type;
    }
}
