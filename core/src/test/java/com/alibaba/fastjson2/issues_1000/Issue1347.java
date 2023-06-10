package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1347 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.accountType = LoginAccountType.PHONE;
        assertEquals("{\"accountType\":2}", JSON.toJSONString(bean));
    }

    public static class Bean {
        public LoginAccountType accountType;
    }

    @Getter
    @AllArgsConstructor
    public enum LoginAccountType
            implements DictEnum<Integer>, Serializable {
        /**
         * 用户名
         */
        USERNAME(1, "用户名", true),
        /**
         * 手机号
         */
        PHONE(2, "手机号", true),
        /**
         * 电子邮件
         */
        EMAIL(3, "电子邮件", true);
        private final Integer value;
        private final String title;
        // 是否是本地账号
        private final boolean local;

        @JSONCreator
        public static LoginAccountType create(Integer value) {
            return DictEnum.valueOf(values(), value);
        }

        /**
         * 处理登录标识，统一转换大小写问题
         *
         * @param identifier 登录标识
         * @return 结果
         */
        public String handleIdentifier(final String identifier) {
            if (!local) {
                // 不是本地账号，第三方账号需要区分大小写
                return identifier;
            }
            return identifier == null ? null : identifier.toLowerCase();
        }
    }

    /**
     * 数据字典枚举接口。系统字典枚举接口
     *
     * author HouKunLin
     */
    public interface DictEnum<T extends Serializable> {
        /**
         * 通过枚举值从枚举列表中获取枚举对象
         *
         * @param values 枚举对象列表
         * @param value  枚举值
         * @param <T>    枚举值类型
         * @return 枚举对象
         */
        static <T extends Serializable, E extends Enum<E> & DictEnum<T>> E valueOf(E[] values, T value) {
            for (final E enums : values) {
                if (enums.getValue().equals(value)) {
                    return enums;
                }
            }
            return null;
        }

        /**
         * 字典值
         *
         * @return 字典值
         */
        @JSONField(value = true)
        T getValue();

        /**
         * 字典文本
         *
         * @return 字典文本
         */
        String getTitle();
    }
}
