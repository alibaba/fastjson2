package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue860 {
    @Test
    public void test() {
        float[] abc = new float[]{
                0.0044160923f,
                1.0449644E-4f,
                5.4759195E-5f,
                2.1139298E-5f,
                1.4655446E-5f,
                0.020943796f,
                0.012482191f,
                0.020476084f,
                0.011609814f,
                1.2344321f,
                0.3234f,
                234.1122f,
                0.23342f,
                1.2f,
                1.2344322f,
                0.3234f,
                234.1122f,
                0.23342f,
                1.2f,
                1.2344323f,
                1.88888f
        };
        // 这里是为了让b": 这里的的冒号正好到达8092长度
        // 需要在下一步进行扩容
        // 下面 12 改成 11 能正常扩容
        char[] hello = new char[(1 << 13) - 12];
        Arrays.fill(hello, '1');
        JSONObject object = new JSONObject();
        object.put("a", new String(hello));
        object.put("b", abc);
        String str = JSON.toJSONString(object);
        assertNotNull(str);
        JSONObject object1 = JSON.parseObject(str);
        assertEquals(2, object1.size());
    }
}
