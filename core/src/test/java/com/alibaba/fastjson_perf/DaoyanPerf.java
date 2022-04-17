package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.*;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoyanPerf {
    public static byte[] serializeByHessian2(Object obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(obj);

        hessian2Output.flush();

        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] serializeByJdk(Object obj) {
        ByteArrayOutputStream byteOutputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {

            byteOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteOutputStream);

            objectOutputStream.writeObject(obj);

            objectOutputStream.flush();

            return byteOutputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != objectOutputStream) {
                try {
                    objectOutputStream.close();
                    byteOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    public static class CartItemDO implements Serializable {
        private static final long serialVersionUID = -3291877592429392571L;
        private String id = "myId";
        private long cartId;

        public CartItemDO() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getCartId() {
            return cartId;
        }

        public void setCartId(long cartId) {
            this.cartId = cartId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getTrackId() {
            return trackId;
        }

        public void setTrackId(String trackId) {
            this.trackId = trackId;
        }

        public long getItemId() {
            return itemId;
        }

        public void setItemId(long itemId) {
            this.itemId = itemId;
        }

        public long getSkuId() {
            return skuId;
        }

        public void setSkuId(long skuId) {
            this.skuId = skuId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getMainType() {
            return mainType;
        }

        public void setMainType(int mainType) {
            this.mainType = mainType;
        }

        public long getTpId() {
            return tpId;
        }

        public void setTpId(long tpId) {
            this.tpId = tpId;
        }

        public long getSubType() {
            return subType;
        }

        public void setSubType(long subType) {
            this.subType = subType;
        }

        public long getCityCode() {
            return cityCode;
        }


        public void setCityCode(long cityCode) {
            this.cityCode = cityCode;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        private long userId;
        private String trackId = "myTrackId";
        private long itemId;
        private long skuId;
        private int quantity;
        private int mainType = 0;
        private long tpId;
        private long subType = 0L;
        private long cityCode;
        private Map<String, String> attributes = new HashMap();
    }


    static final int LOOP_COUNT = 1000 * 1000 * 1;
    static final int ITER_COUNT = 5;

    private static List<CartItemDO> newCartsItem() {

        List<CartItemDO> list = new ArrayList();

        for (long i = 90000000000l; i < 90000000000l + 10; i++) {
            CartItemDO cartItemDO2 = new CartItemDO();

            cartItemDO2.setUserId(i);
            cartItemDO2.setAttributes(new HashMap());
            cartItemDO2.setCartId(i * 100);
            cartItemDO2.setCityCode(i * 12);
            cartItemDO2.setItemId(i * 3);
            cartItemDO2.setMainType(11);
            cartItemDO2.setQuantity(900);
            cartItemDO2.setSkuId(i * 5);
            cartItemDO2.setSubType(i * 6);
            cartItemDO2.setTpId(i * 7);
            cartItemDO2.setTrackId(String.valueOf(i * 8));
            list.add(cartItemDO2);
        }

        return list;
    }

    private byte[] jsonBytes;
    private byte[] jsonbBytes;
    private byte[] hessian2Bytes;
    private byte[] jdkBytes;
    private List<CartItemDO> lists = newCartsItem();

    public DaoyanPerf() throws Exception {
        {
            hessian2Bytes = serializeByHessian2(lists);
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(hessian2Bytes);

            Hessian2Input hessian2Output = new Hessian2Input(bytesIn);
            hessian2Output.readObject();
        }

        {
            jsonbBytes = JSONB.toBytes(lists, JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.IgnoreErrorGetter);
            Type type = new TypeReference<List<CartItemDO>>(){}.getType();
            JSONB.parseObject(jsonbBytes, type);
        }
        {
            jsonBytes = JSON.toJSONBytes(lists);
            Type type = new TypeReference<List<CartItemDO>>(){}.getType();
            JSON.parseObject(jsonBytes, type);
        }
        {
            jdkBytes = serializeByJdk(lists);
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(jdkBytes));
            inputStream.readObject();
        }
    }

    @Test
    public void test_size_cmp() throws Exception {
//        Object object = JSONArray.of(
//                JSONObject.of("id", 101).fluentPut("name", "DataWorks"),
//                JSONObject.of("id", 102).fluentPut("name", "MaxCompute")
//        );
        Object object = JSONObject.of("id", 101).fluentPut("name", "DataWorks");

        byte[] jsonbBytes = JSONB.toBytes(object);
        byte[] h2Bytes = serializeByHessian2(object);

        System.out.println("jsonbBytes size " + jsonbBytes.length);
        System.out.println("hessian2Bytes size " + h2Bytes.length);
    }

    @Test
    public void test_hessoin2() throws Exception {
        byte[] copyOfWrittenBuffer = null;

        for (int i = 0; i < ITER_COUNT; ++i)  {
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                copyOfWrittenBuffer = serializeByHessian2(lists);
            }

            System.out.println("hession2 : millis " + (System.currentTimeMillis() - start) + ", len " + copyOfWrittenBuffer.length);
            // jdk-11.0.13 2798
        }
    }

    @Test
    public void test_hessoin2_parse() throws Exception {
        Object o = null;
        for (int i = 0; i < ITER_COUNT; ++i)  {
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                ByteArrayInputStream bytesIn = new ByteArrayInputStream(hessian2Bytes);

                Hessian2Input hessian2Input = new Hessian2Input(bytesIn);
                o = hessian2Input.readObject();

            }

            System.out.println("hession2-parse : millis " + (System.currentTimeMillis() - start) + ", len " + hessian2Bytes.length);
            // jdk-11.0.13 3869
        }
    }

    @Test
    public void test_fastjson2_jsonb() {
        byte[] copyOfWrittenBuffer = null;

        for (int i = 0; i < ITER_COUNT; ++i){
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                copyOfWrittenBuffer = JSONB.toBytes(lists);
            }

            System.out.println("jsonb : millis " + (System.currentTimeMillis() - start) + ", len " + copyOfWrittenBuffer.length);
            // jdk-11.0.13  : 727
            // zulu11.52.13 : 472
        }
    }

    @Test
    public void test_fastjson2_jsonb_parse() {
        Type type = new TypeReference<List<CartItemDO>>(){}.getType();
        for (int i = 0; i < ITER_COUNT; ++i){
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                JSONB.parseObject(jsonbBytes, type);
            }

            System.out.println("jsonb-parse : millis " + (System.currentTimeMillis() - start) + ", len " + jsonbBytes.length);
            // jdk-11.0.13 691
        }
    }

    @Test
    public void test_fastjson2() {
        byte[] copyOfWrittenBuffer = null;

        for (int i = 0; i < ITER_COUNT; ++i){
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                copyOfWrittenBuffer = JSON.toJSONBytes(lists);
            }

            System.out.println("json : millis " + (System.currentTimeMillis() - start) + ", len " + copyOfWrittenBuffer.length);
        }
    }

    @Test
    public void test_fastjson2_parse() {

        Type type = new TypeReference<List<CartItemDO>>(){}.getType();

        for (int i = 0; i < ITER_COUNT; ++i){
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                JSON.parseObject(jsonBytes, type);
            }

            System.out.println("json-parse : millis " + (System.currentTimeMillis() - start) + ", len " + jsonBytes.length);
        }
    }

    @Test
    public void test_jdk() {
        byte[] copyOfWrittenBuffer = null;

        for (int i = 0; i < ITER_COUNT; ++i){
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                copyOfWrittenBuffer = serializeByJdk(lists);
            }

            System.out.println("jdk : millis " + (System.currentTimeMillis() - start) + ", len " + copyOfWrittenBuffer.length);
        }
    }

    @Test
    public void test_jdk_parse() throws Exception {
        byte[] copyOfWrittenBuffer = serializeByJdk(lists);

        for (int i = 0; i < ITER_COUNT; ++i){
            long start = System.currentTimeMillis();

            for (int j = 0; j < LOOP_COUNT; j++) {
                ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(copyOfWrittenBuffer));
                inputStream.readObject();
            }

            System.out.println("jdk-parse : millis " + (System.currentTimeMillis() - start) + ", len " + copyOfWrittenBuffer.length);
        }
    }

}
