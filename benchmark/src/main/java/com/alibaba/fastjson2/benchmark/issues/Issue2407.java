package com.alibaba.fastjson2.benchmark.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Issue2407 {
    static String str;
    static byte[] utf8;

    static {
        try {
            InputStream is = Issue2407.class.getClassLoader().getResourceAsStream("issue/issue2407.json");
            str = IOUtils.toString(is, "UTF-8");
            utf8 = str.getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void parseArray(Blackhole bh) {
        bh.consume(
                JSON.parseArray(str)
        );
    }

    @Benchmark
    public void parseArray1(Blackhole bh) {
        bh.consume(
                JSON.parseArray(str, Bean.class)
        );
    }

//    @Benchmark
    public void parseArray1_utf8(Blackhole bh) {
        bh.consume(
                JSON.parseArray(utf8, Bean.class)
        );
    }

    public class Bean {
        public String content;
        public String hostName;
        public String uniqueKey;
        public String originalAppName;
        public String odinLeaf;
        public long logTime;
        public long logId;
        public String appName;
        public String queryFrom;
        public String logName;
        public int isService;
        public long pathId;
        public String timestamp;
        public long collectTime;
        public String fileKey;
        public String parentPath;
        public long offset;
        public long preOffset;
        @JSONField(name = "ENV_ODIN_SU")
        public String envOdinSu;
    }
}
