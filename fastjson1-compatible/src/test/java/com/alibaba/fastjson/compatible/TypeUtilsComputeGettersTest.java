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
        assertEquals(4, fieldInfoList.size());
    }

    public static class Model {
        private int id;
        private String name;
        private List<String> values;
        private boolean set;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getValues() {
            return values;
        }

        public boolean isSet() {
            return set;
        }
    }

    @Test
    public void test_for_computeGetters1() {
        List<FieldInfo> fieldInfoList = TypeUtils.computeGetters(B.class, null);
        assertEquals(1, fieldInfoList.size());
    }

    public static class A<T> {
        public T value;
    }

    public static class B
            extends A<String> {
    }
}
