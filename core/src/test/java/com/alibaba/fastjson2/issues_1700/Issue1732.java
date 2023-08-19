package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1732 {
    @Test
    public void test() {
        Path path = JSON.parseObject("\"/tmp\"", Path.class);
        assertEquals(File.separatorChar + "tmp", path.toString());
    }

    @Test
    public void testBean() {
        JSONObject object = JSONObject.of("path", "/tmp");
        Bean bean = JSON.parseObject(object.toJSONString(), Bean.class);
        assertEquals(bean.path.toString(), File.separatorChar + "tmp");
        Bean bean1 = object.toJavaObject(Bean.class);
        assertEquals(bean1.path.toString(), File.separatorChar + "tmp");
    }

    public static class Bean {
        public Path path;
    }
}
