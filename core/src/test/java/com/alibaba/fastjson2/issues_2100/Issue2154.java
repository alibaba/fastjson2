package com.alibaba.fastjson2.issues_2100;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

enum Type {
    X(101),
    M(102),
    S(103);

    private final int code;

    Type(int code) {
        this.code = code;
    }

    @JSONField(value = true)
    public int getCode() {
        return code;
    }

}

/**
 * @author 张治保
 * @since 2024/1/6
 */
public class Issue2154 {
    // failure
    @Test
    public void intEnumDeserialize1() {
        Bean1 bean1 = JSON.parseObject("{\"type\":102}", Bean1.class);
        assertEquals(102, bean1.type.getCode());
    }

    // success
    @Test
    public void intEnumDeserialize2() {
        // add this
        {
            Bean1 bean = new Bean1();
            bean.type = Type.S;
            JSON.toJSONString(bean);
        }

        intEnumDeserialize1();
    }
}

class Bean1 {
    public Type type;
}
