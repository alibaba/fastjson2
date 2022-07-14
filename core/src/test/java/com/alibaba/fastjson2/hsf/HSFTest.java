package com.alibaba.fastjson2.hsf;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HSFTest {
    @Test
    public void test_0() {
        VeryComplexDO vo = new VeryComplexDO();
        vo.setPdouble(6.6D);
        vo.setPfloat(7.7F);
        vo.setPint(8);
        vo.setPlong(9);
        vo.setPbyte((byte) 5);
        vo.setFshort((short) 4);
        vo.setpBaseDO(new BaseDO());
        byte[] bytes = JSONB.toBytes(vo, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
        JSONBDump.dump(bytes);
        VeryComplexDO vo2 = (VeryComplexDO) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.hsf.VeryComplexDO\",\n" +
                "\t\"fshort\":4,\n" +
                "\t\"pBaseDO\":{},\n" +
                "\t\"pbyte\":5,\n" +
                "\t\"pdouble\":6.6,\n" +
                "\t\"pfloat\":7.7,\n" +
                "\t\"pint\":8,\n" +
                "\t\"plong\":9\n" +
                "}", JSONB.toJSONString(bytes));
    }

    @Test
    public void test_1() {
        VeryComplexDO vo = VeryComplexDO.getFixedComplexDO();
        vo.setpBaseDO(new BaseDO());
        byte[] bytes = JSONB.toBytes(vo, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
        JSONBDump.dump(bytes);
        VeryComplexDO vo2 = (VeryComplexDO) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
    }

    @Test
    public void test_2() {
        JSONObject data = new JSONObject().fluentPut("id", 123);
        ComponentProtocol protocol = new ComponentProtocol();
        protocol.setData(data);
        protocol.setEndpoint(data);

        byte[] jsonbBytes = JSONB.toBytes(protocol, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        JSONReader reader = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length);
        reader.getContext().config(JSONReader.Feature.SupportAutoType);
        ObjectReader objectReader = reader.getObjectReader(Object.class);

        ComponentProtocol object = (ComponentProtocol) objectReader.readJSONBObject(reader, null, null, 0);
        reader.handleResolveTasks(object);

        assertNotNull(protocol.getData());
        assertSame(protocol.getData(), protocol.getEndpoint());
    }

//    @Test
//    public void test_concurrent_10() throws Exception {
//        final int THREAD_CNT = 5;
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch endLatch = new CountDownLatch(THREAD_CNT);
//
//        VeryComplexDO vo = VeryComplexDO.getFixedComplexDO();
//        Runnable task = () -> {
//            try {
//                startLatch.await();
//                for (int i = 0; i < 1000 * 1000; ++i) {
//                    JSONB.toBytes(VeryComplexDO.getFixedComplexDO());
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                endLatch.countDown();
//            }
//        };
//        Thread[] threads = new Thread[THREAD_CNT];
//        for (int i = 0; i < threads.length; i++) {
//            threads[i] = new Thread(task);
//            threads[i].start();
//        }
//        startLatch.countDown();
//        endLatch.await();
//        System.out.println("end");
//    }
}
