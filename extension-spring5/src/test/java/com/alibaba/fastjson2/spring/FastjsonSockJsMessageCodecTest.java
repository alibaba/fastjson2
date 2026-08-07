package com.alibaba.fastjson2.spring;

import com.alibaba.fastjson2.support.spring.websocket.sockjs.FastjsonSockJsMessageCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FastjsonSockJsMessageCodecTest {
    @Test
    public void test_encode() {
        FastjsonSockJsMessageCodec fastjsonCodec = new FastjsonSockJsMessageCodec();

        String v0 = "a0\"", v1 = "a1";
        String fastjsonResult = fastjsonCodec.encode(v0, v1);
        assertEquals("a[\"a0\\\"\",\"a1\"]", fastjsonResult);
    }
}
