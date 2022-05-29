package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBuilderTest {
    @Test
    public void test_create() {
        String str = "{\"id\":12304,\"name\":\"ljw\"}";
        {
            VO vo = JSON.parseObject(str, VO.class);
            assertEquals(12304, vo.getId());
            assertEquals("ljw", vo.getName());
        }
        JSONObject jsonObject = JSON.parseObject(str);
        {
            VO vo = jsonObject.toJavaObject(VO.class);
            assertEquals(12304, vo.getId());
            assertEquals("ljw", vo.getName());
        }
        VO vo = jsonObject.toJavaObject((Type) VO.class);
        assertEquals(12304, vo.getId());
        assertEquals("ljw", vo.getName());
    }

    @Test
    public void test_create_1() {
        ObjectReader<VO> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(VO.class);
        String str = "{\"id\":12304,\"name\":\"ljw\"}";
        {
            VO vo = objectReader.readObject(JSONReader.of(str));
            assertEquals(12304, vo.getId());
            assertEquals("ljw", vo.getName());
        }
        JSONObject jsonObject = JSON.parseObject(str);
        {
            VO vo = objectReader.createInstance(jsonObject);
            assertEquals(12304, vo.getId());
            assertEquals("ljw", vo.getName());
        }
        VO vo = jsonObject.toJavaObject((Type) VO.class);
        assertEquals(12304, vo.getId());
        assertEquals("ljw", vo.getName());
    }

    @JSONType(builder = VOBuilder.class)
    public static class VO {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONBuilder(buildMethod = "xxx")
    public static class VOBuilder {
        private VO vo = new VO();

        public VO xxx() {
            return vo;
        }

        public VOBuilder withId(int id) {
            vo.id = id;
            return this;
        }

        public VOBuilder withName(String name) {
            vo.name = name;
            return this;
        }
    }
}
