package com.alibaba.fastjson2.spring;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class FastJsonHttpMessageConverterUnitTest {
    @Test
    public void test() throws Exception {
        FastJsonHttpMessageConverter messageConverter = new FastJsonHttpMessageConverter();
        messageConverter.setFastJsonConfig(new FastJsonConfig());
        assertNotNull(messageConverter.getFastJsonConfig());
        assertTrue(messageConverter.canRead(VO.class, VO.class, MediaType.APPLICATION_JSON));
        assertTrue(messageConverter.canWrite(VO.class, VO.class, MediaType.APPLICATION_JSON));

        messageConverter.setSupportedMediaTypes(Arrays
                .asList(new MediaType[]{MediaType.APPLICATION_JSON}));
        assertEquals(1, messageConverter.getSupportedMediaTypes().size());

        Method method = FastJsonHttpMessageConverter.class.getDeclaredMethod(
                "supports", Class.class);
        method.setAccessible(true);
        method.invoke(messageConverter, int.class);

        VO vo = (VO) messageConverter.read(VO.class, VO.class, new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream("{\"id\":123}".getBytes(Charset.forName("UTF-8")));
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        });
        assertEquals(vo.getId(), 123);

        messageConverter.write(vo, VO.class, MediaType.APPLICATION_JSON, new HttpOutputMessage() {
            @Override
            public OutputStream getBody() {
                return new ByteArrayOutputStream();
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        });
    }

    public static class VO {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
