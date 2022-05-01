package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.LocalTime1;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LocalTimeTest {
    static LocalTime[] times = {
            LocalTime.of(12, 34, 1, 481_234_567),
//            LocalTime.of(12, 34, 1, 481_234_560),
//            LocalTime.of(12, 34, 1, 481_234_500),
//            LocalTime.of(12, 34, 1, 481_234_000),
//            LocalTime.of(12, 34, 1, 481_230_000),
//            LocalTime.of(12, 34, 1, 481_200_000),
            LocalTime.of(12, 34, 1, 481_000_000),
            LocalTime.of(12, 34, 1, 480_000_000),
            LocalTime.of(12, 34, 1, 400_000_000),
            LocalTime.of(12, 34, 1, 000_000_000)
    };

    @Test
    public void test_jsonb() {
        for (LocalTime time : times) {
            LocalTime1 vo = new LocalTime1();
            vo.setDate(time);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            LocalTime1 v1 = JSONB.parseObject(jsonbBytes, LocalTime1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_jsonb_1() {
        for (LocalTime time : times) {
            String str = time.toString();
            byte[] jsonbBytes = JSONB.toBytes(str);
            LocalTime lt = JSONB.parseObject(jsonbBytes, LocalTime.class);
            assertEquals(lt, time);
        }
    }

    @Test
    public void test_utf8() {
        for (LocalTime time : times) {
            LocalTime1 vo = new LocalTime1();
            vo.setDate(time);
            byte[] jsonbBytes = JSON.toJSONBytes(vo);

            LocalTime1 v1 = JSON.parseObject(jsonbBytes, LocalTime1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_str() {
        for (LocalTime time : times) {
            LocalTime1 vo = new LocalTime1();
            vo.setDate(time);
            String str = JSON.toJSONString(vo);

            LocalTime1 v1 = JSON.parseObject(str, LocalTime1.class);
            if (v1 == null) {
                fail();
            }
            assertEquals(vo.getDate(), v1.getDate());
        }
    }
}
