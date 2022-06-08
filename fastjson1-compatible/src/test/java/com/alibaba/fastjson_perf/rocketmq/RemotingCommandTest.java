package com.alibaba.fastjson_perf.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemotingCommandTest {
    RemotingCommand cmd;
    String str;
    byte[] bytes;

    @BeforeEach
    public void init() {
        cmd = new RemotingCommand();
        cmd.setCode(310);
        cmd.setLanguage(LanguageCode.JAVA);
        cmd.setVersion(399);
        cmd.setFlag(1);
        cmd.addExtField("a", "please_rename_unique_group_name");
        cmd.addExtField("b", "TopicTest");
        cmd.addExtField("c", "TBW102");
        cmd.addExtField("d", "4");
        cmd.addExtField("e", "1");

        str = JSON.toJSONString(cmd, SerializerFeature.DisableCircularReferenceDetect);
        bytes = JSON.toJSONBytes(cmd, SerializerFeature.DisableCircularReferenceDetect);
    }

    public void serialize_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            JSON.toJSONBytes(cmd, SerializerFeature.DisableCircularReferenceDetect);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("serialize millis : " + millis);
    }

    @Test
    public void serialize_perf_test() {
        JSON.toJSONBytes(cmd, SerializerFeature.DisableCircularReferenceDetect);

        for (int i = 0; i < 10; i++) {
            serialize_perf(); // 185
        }
    }

    public void deserialize_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            JSON.parseObject(bytes, RemotingCommand.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("serialize millis : " + millis);
    }

    @Test
    public void deserialize_perf_test() {
        JSON.parseObject(bytes, RemotingCommand.class);

        for (int i = 0; i < 10; i++) {
            deserialize_perf(); // 296
        }
    }

    public void parseTree_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            JSON.parseObject(bytes);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("parseTreeBytes millis : " + millis);
    }

    @Test
    public void parseTree_perf_test() {
        JSON.parseObject(bytes);

        for (int i = 0; i < 10; i++) {
            parseTree_perf(); // 520
        }
    }

    public void parseStringTree_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("serialize millis : " + millis);
    }

    @Test
    public void parseStringTree_perf_test() {
        JSON.parseObject(str);

        for (int i = 0; i < 10; i++) {
            parseStringTree_perf(); // 394
        }
    }
}
