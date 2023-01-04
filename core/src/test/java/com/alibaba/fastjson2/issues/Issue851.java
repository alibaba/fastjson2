package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue851 {
    @Test
    public void testUser() {
        JSON.register(UserType.class, new ObjectWriter<UserType>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                jsonWriter.writeInt32(((UserType) object).getValue());
            }
        });

        User user = new User();
        user.setName("张三");
        user.setUserType(UserType.BOY);
        user.setAge(20);

        assertEquals("{\"age\":20,\"name\":\"张三\",\"userType\":1}", JSON.toJSONString(user));
    }

    @RequiredArgsConstructor
    @Getter
    public enum UserType implements FierceEnum {
        BOY(1, "男孩"),
        GIRL(2, "女孩");
        private final Integer value;
        private final String name;
    }

    @Data
    public static class User {
        private String name;
        private Integer age;
        private UserType userType;
    }

    public interface FierceEnum {
        /**
         * 获取枚举值名称
         *
         * @return 名称
         */
        String getName();

        Integer getValue();

        /**
         * 获取枚举值描述
         *
         * @return 描述
         */
        default String getDescription() {
            return null;
        }

        static <T> T getEnum(final Class<T> clazz, final Integer value) {
            if (clazz.isEnum() && FierceEnum.class.isAssignableFrom(clazz)) {
                final T[] constants = clazz.getEnumConstants();
                for (T constant : constants) {
                    final FierceEnum item = (FierceEnum) constant;
                    if (item.getValue().equals(value)) {
                        return constant;
                    }
                }
            }
            // 找不到或者不是枚举类，则返回null
            return null;
        }
    }
}
