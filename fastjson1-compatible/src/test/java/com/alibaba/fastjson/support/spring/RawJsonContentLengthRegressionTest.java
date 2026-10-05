package com.alibaba.fastjson.support.spring;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RawJsonContentLengthRegressionTest {
    @Test
    public void setsHeadersBeforeOpeningBody() throws Exception {
        String json = "{\"id\":123}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        for (Object payload : new Object[]{json, bytes}) {
            FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            HttpHeaders headers = new HttpHeaders();
            HttpOutputMessage message = new HttpOutputMessage() {
                @Override
                public OutputStream getBody() {
                    assertEquals(bytes.length, headers.getContentLength());
                    return body;
                }

                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            };
            converter.write(payload, MediaType.APPLICATION_JSON, message);
            assertEquals(json, new String(body.toByteArray(), StandardCharsets.UTF_8));
        }
    }
}
