package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue508 {
    @Test
    public void test() {
        Device device = new Device(1, "xxx");
        Xx xx = new Xx(device);
        String str = JSON.toJSONString(xx, JSONWriter.Feature.WriteClassName);
        System.out.println(str);
        Xx x = JSON.parseObject(str, Xx.class, JSONReader.Feature.SupportAutoType);
        assertEquals(xx.base.getClass(), x.base.getClass());
    }

    public interface IBase {
        int getId();
        String getName();
    }

    public abstract static class Base
            implements IBase {
        private int id;
        private String name;
        protected Base(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class Device
            extends Base
            implements IBase {
        public Device(int id, String name) {
            super(id, name);
        }
    }

    public interface Ixx {
        IBase getBase();
    }

    public static class Xx
            implements Ixx {
        private IBase base;

        public Xx(IBase base) {
            this.base = base;
        }

        public IBase getBase() {
            return base;
        }
    }
}
