package com.alibaba;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import com.caucho.hessian.io.Hessian2Output;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.*;

public class Hessian2Test {
    @Test
    public void test_num() throws Exception {
        for (int i = 0; i < 256; i++) {
            System.out.println(Integer.toHexString(i) + "\t" + (byte) i);
        }
    }
    @Test
    public void test_h2() throws Exception {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            int val = i & 0xFF;
            byte[] bytes = serializeByHessian2((long) i);
            byte[] jsonbBytes = JSONB.toBytes((long) i);

            String line = i + "\t" + Integer.toHexString(val) + "\t" + Arrays.toString(bytes);
            if (bytes.length == 1) {
                line += "\t" + Integer.toHexString(bytes[0] & 0xFF);
            }
            if (jsonbBytes.length > bytes.length) {
                System.err.println(line);
            } else {
                System.out.println(line);
            }
        }
    }
    @Test
    public void test_h2_values() throws Exception {
        long[] values = new long[]{
                71L
        };
        for (int i = 0; i < values.length; i++) {
            long val = values[i];
            byte[] bytes = serializeByHessian2(val);
            byte[] jsonbBytes = JSONB.toBytes(val);
            String line = i + "\t" + Long.toHexString(val) + "\t" + Arrays.toString(bytes);
            if (bytes.length == 1) {
                line += "\t" + Integer.toHexString(bytes[0] & 0xFF);
            }
            if (jsonbBytes.length > bytes.length) {
                System.err.println(line);
            } else {
                System.out.println(line);
            }
        }
    }
    @Test
    public void test_obj() throws Exception {
        Item item = new Item();
        item.id = 1;

        byte[] bytes = serializeByHessian2(item);
        byte[] jsonbBytes = JSONB.toBytes(item, JSONWriter.Feature.WriteClassName);
        System.out.println("h2\t\t: " + bytes.length + "\t" + Arrays.toString(bytes));
        System.out.println("jsonb\t: " + jsonbBytes.length + "\t" + Arrays.toString(jsonbBytes));
    }
    @Test
    public void test_obj_list_1() throws Exception {
        List list = new ArrayList<>();

        Item item = new Item();
        item.id = 1;
        list.add(item);

        byte[] bytes = serializeByHessian2(list);
        byte[] jsonbBytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteHashMapArrayListClassName);
        System.out.println("h2\t\t: " + bytes.length + "\t" + Arrays.toString(bytes));
        System.out.println("jsonb\t: " + jsonbBytes.length + "\t" + Arrays.toString(jsonbBytes));
    }
    @Test
    public void test_obj_list_3() throws Exception {
        List list = new ArrayList<>();

        {
            Item item = new Item();
            item.id = 1;
            list.add(item);
        }
        {
            Item item = new Item();
            item.id = 2;
            list.add(item);
        }
        {
            Item item = new Item();
            item.id = 3;
            list.add(item);
        }
        byte[] bytes = serializeByHessian2(list);
        byte[] jsonbBytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteHashMapArrayListClassName);
        System.out.println("h2\t\t: " + bytes.length + "\t" + Arrays.toString(bytes));
        System.out.println("jsonb\t: " + jsonbBytes.length + "\t" + Arrays.toString(jsonbBytes));
    }
    @Test
    public void test_field_list_3() throws Exception {
        List list = new ArrayList<>();

        {
            Item item = new Item();
            item.id = 1;
            list.add(item);
        }
        {
            Item item = new Item();
            item.id = 2;
            list.add(item);
        }
        {
            Item item = new Item();
            item.id = 3;
            list.add(item);
        }
        Bean bean = new Bean();
        bean.items = list;

        byte[] bytes = serializeByHessian2(bean);
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteHashMapArrayListClassName);
        System.out.println("h2\t\t: " + bytes.length + "\t" + Arrays.toString(bytes));
        System.out.println("jsonb\t: " + jsonbBytes.length + "\t" + Arrays.toString(jsonbBytes));
    }
    @Test
    public void test_obj_map_3() throws Exception {
        Map map = new HashMap();

        {
            Item item = new Item();
            item.id = 1;
            map.put("1", item);
        }
        {
            Item item = new Item();
            item.id = 2;
            map.put("2", item);
        }
        {
            Item item = new Item();
            item.id = 3;
            map.put("3", item);
        }
        byte[] bytes = serializeByHessian2(map);
        byte[] jsonbBytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteHashMapArrayListClassName);
        System.out.println("h2\t\t: " + bytes.length + "\t" + Arrays.toString(bytes));
        System.out.println("jsonb\t: " + jsonbBytes.length + "\t" + Arrays.toString(jsonbBytes));
    }
    public static byte[] serializeByHessian2(Object obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(obj);

        hessian2Output.flush();

        return byteArrayOutputStream.toByteArray();
    }
    public static class Item
            implements Serializable {
        public int id;
        public String name;

        public Item() {
        }
        public Item(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    public static class Bean
            implements Serializable {
        List<Item> items;
    }
    @Test
    public void test_bean2() throws Exception {
        Bean2 bean = new Bean2();
        bean.first = new Item(101, "DataWorks");
        bean.second = new Item(102, "MaxCompute");
        bean.third = new Item(103, "EMR");
        byte[] bytes = serializeByHessian2(bean);
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteHashMapArrayListClassName);

        System.out.println("h2\t\t: " + bytes.length + "\t" + Arrays.toString(bytes));
        System.out.println("jsonb\t: " + jsonbBytes.length + "\t" + Arrays.toString(jsonbBytes));

        JSONBDump.dump(jsonbBytes);
    }
    public static class Bean2
            implements Serializable {
        public Object first;
        public Object second;
        public Object third;
    }
}
