package com.alibaba.fastjson.support.spring;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FastjsonSockJsMessageCodecTest {
    @Test
    public void test_encode() {
        FastjsonSockJsMessageCodec fastjsonCodec = new FastjsonSockJsMessageCodec();
        Jackson2SockJsMessageCodec jacksonCodec = new Jackson2SockJsMessageCodec();

        String v0 = "a0\"", v1 = "a1";
        String fastjsonResult = fastjsonCodec.encode(v0, v1);
        String jacksonResult = jacksonCodec.encode(v0, v1);
        assertEquals("a[\"a0\\\"\",\"a1\"]", fastjsonResult);
        assertEquals(jacksonResult, fastjsonResult);
    }
}
