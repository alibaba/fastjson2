package com.alibaba.fastjson.compatible;

import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 20/03/2017.
 */
public class TypeUtilsComputeGettersTest {
    @Test
    public void test_for_computeGetters() {
        List<FieldInfo> fieldInfoList = TypeUtils.computeGetters(Model.class, null);
        assertEquals(1, fieldInfoList.size());
        assertEquals("id", fieldInfoList.get(0).name);
    }

    public static class Model {
        private int id;

        public int getId() {
            return id;
        }
    }
}
