package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2_vo.Int10;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.writer.ObjectWriters.*;

import static junit.framework.TestCase.assertEquals;

public class WriterInt20Test_0 {
    Int10 vo = new Int10();

    public WriterInt20Test_0() {
        vo.setV0000(0);
        vo.setV0001(1);
        vo.setV0002(2);
        vo.setV0003(3);
        vo.setV0004(4);
        vo.setV0005(5);
        vo.setV0006(6);
        vo.setV0007(7);
        vo.setV0008(8);
        vo.setV0009(9);
    }
    @Test
    public void test_reflect_utf8() throws Exception {
        ObjectWriter ow = ObjectWriters.ofReflect(Int10.class);
        {
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
//            assertEquals("{\"v0000\":0,\"v0001\":1,\"v0002\":2,\"v0003\":3,\"v0004\":4,\"v0005\":5,\"v0006\":6,\"v0007\":7,\"v0008\":8,\"v0009\":9}"
//                    , json);
        }
        {
            for (int i = 0; i < 10; ++i) {
                long start = System.currentTimeMillis();

                for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                    JSONWriter w = JSONWriter.of();
                    ow.write(w, vo);
                    w.close();
                }

                long millis = System.currentTimeMillis() - start;
                System.out.println("WriteInt20 reflect-utf8 millis : " + millis); // 2097 2076 2005 1695 1591
            }
        }
    }

    @Test
    public void test_lambda_utf8() throws Exception {
        ObjectWriter ow = ObjectWriters.objectWriter(Int10.class);
        {
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            assertEquals("{\"v0000\":0,\"v0001\":1,\"v0002\":2,\"v0003\":3,\"v0004\":4,\"v0005\":5,\"v0006\":6,\"v0007\":7,\"v0008\":8,\"v0009\":9}"
                    , json);
        }

        {
            for (int i = 0; i < 10; ++i) {
                long start = System.currentTimeMillis();

                for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                    JSONWriter w = JSONWriter.of();
                    ow.write(w, vo);
                }

                long millis = System.currentTimeMillis() - start;
                System.out.println("WriteInt20 lambda-utf8 millis : " + millis); // 1690
            }
        }
    }

    @Test
    public void test_manual_utf8() throws Exception {

        ObjectWriter ow = ObjectWriters.objectWriter(
                fieldWriter("v0000", Int10::getV0000),
                fieldWriter("v0001", Int10::getV0001),
                fieldWriter("v0002", Int10::getV0002),
                fieldWriter("v0003", Int10::getV0003),
                fieldWriter("v0004", Int10::getV0004),
                fieldWriter("v0005", Int10::getV0005),
                fieldWriter("v0006", Int10::getV0006),
                fieldWriter("v0007", Int10::getV0007),
                fieldWriter("v0008", Int10::getV0008),
                fieldWriter("v0009", Int10::getV0009)
        );
        {
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            assertEquals("{\"v0000\":0,\"v0001\":1,\"v0002\":2,\"v0003\":3,\"v0004\":4,\"v0005\":5,\"v0006\":6,\"v0007\":7,\"v0008\":8,\"v0009\":9}"
                    , json);
        }

        {
            for (int i = 0; i < 10; ++i) {
                long start = System.currentTimeMillis();

                for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                    JSONWriter w = JSONWriter.of();
                    ow.write(w, vo);
                }

                long millis = System.currentTimeMillis() - start;
                System.out.println("WriteInt20 manual-utf8 millis : " + millis); // 1607
            }
        }
    }
}
