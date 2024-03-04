package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 第一次测试性能（次数、耗时毫秒）
 * 1024	27
 * 2048	11
 * 4096	18
 * 8192	31
 * 16384	55
 * 32768	117
 * 65536	291
 * 131072	446
 * 262144	1077
 * 524288	1844
 * 1048576	3618
 * <p>
 * 第二次性能测试
 * 1024	28
 * 2048	10
 * 4096	17
 * 8192	29
 * 16384	55
 * 32768	110
 * 65536	225
 * 131072	547
 * 262144	936
 * 524288	1831
 * 1048576	3482
 */
public class CompareUtilsPerfTest {

    @Test
    public void testCompare() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number1':1," +
                "'number2':1," +
                "'number3':1," +
                "'number4':1," +
                "'number5':1," +
                "'number1':1," +
                "'string1':'abc'," +
                "'string2':'abc'," +
                "'string3':'abc'," +
                "'string4':'abc'," +
                "'string5':'abc'," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number1':123," +
                "'number2':12," +
                "'number3':10," +
                "'number4':1," +
                "'number5':1.0," +
                "'number1':1.23," +
                "'string1':'abc'," +
                "'string2':'abc1'," +
                "'string3':'abc2'," +
                "'string4':'abc3'," +
                "'string5':'abc'," +
                "}");
        long times = 1024;

        for (int j = 0; j < times * 10; j++) {
            CompareUtils.compareToArray(json1, json2);
        }
        for (int i = 10; i < 21; i++) {
            long begine = System.currentTimeMillis();

            for (int j = 0; j < times; j++) {
                CompareUtils.compareToArray(json1, json2);
            }
            long end = System.currentTimeMillis();
            System.out.println(times + "\t" + (end - begine));
            times *= 2;
        }
    }
}
