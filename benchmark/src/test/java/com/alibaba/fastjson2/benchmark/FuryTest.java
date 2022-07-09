package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinary;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import io.fury.Fury;
import io.fury.Language;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class FuryTest {
    static MediaContent mc;

    static Fury fury = Fury.builder().withLanguage(Language.JAVA).build();

    static {
        try {
            InputStream is = EishayParseBinary.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void test() {
        byte[] furyBytes = fury.serialize(mc);
        byte[] jsonbBytes = JSONB.toBytes(mc, JSONWriter.Feature.WriteClassName);

        System.out.println("fury size : " + furyBytes.length);
        System.out.println("jsonb size : " + jsonbBytes.length);
    }
}
