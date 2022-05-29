package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

public class FastJsonHttpMessageConverterTest {
    @Test
    public void test_read() throws Exception {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        converter.setSupportedMediaTypes(Arrays
                .asList(new MediaType[]{MediaType.APPLICATION_JSON_UTF8}));
        Assertions.assertEquals(1, converter.getSupportedMediaTypes().size());

        Method method = FastJsonHttpMessageConverter.class.getDeclaredMethod(
                "supports", Class.class);
        method.setAccessible(true);
        method.invoke(converter, int.class);

        HttpInputMessage input = new HttpInputMessage() {
            public HttpHeaders getHeaders() {
                // TODO Auto-generated method stub
                return null;
            }

            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream("{\"id\":123}".getBytes(Charset
                        .forName("UTF-8")));
            }
        };
        VO vo = (VO) converter.read(VO.class, input);
        Assertions.assertEquals(123, vo.getId());

        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        HttpOutputMessage out = new HttpOutputMessage() {
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            public OutputStream getBody() throws IOException {
                return byteOut;
            }
        };
        converter.write(vo, MediaType.TEXT_PLAIN, out);

        byte[] bytes = byteOut.toByteArray();
        Assertions.assertEquals("{\"id\":123}", new String(bytes, "UTF-8"));
    }

    @Test
    public void test_1() throws Exception {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        Assertions.assertNotNull(converter.getFastJsonConfig());
        converter.setFastJsonConfig(new FastJsonConfig());

        converter.canRead(VO.class, MediaType.APPLICATION_JSON_UTF8);
        converter.canWrite(VO.class, MediaType.APPLICATION_JSON_UTF8);
        converter.canRead(VO.class, VO.class, MediaType.APPLICATION_JSON_UTF8);
        converter.canWrite(VO.class, VO.class, MediaType.APPLICATION_JSON_UTF8);

        HttpInputMessage input = new HttpInputMessage() {
            public HttpHeaders getHeaders() {
                // TODO Auto-generated method stub
                return null;
            }

            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream("{\"id\":123}".getBytes(Charset
                        .forName("UTF-8")));
            }
        };
        VO vo = (VO) converter.read(VO.class, VO.class, input);
        Assertions.assertEquals(123, vo.getId());

        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        HttpOutputMessage out = new HttpOutputMessage() {
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            public OutputStream getBody() throws IOException {
                return byteOut;
            }
        };
        converter.write(vo, VO.class, MediaType.TEXT_PLAIN, out);

        byte[] bytes = byteOut.toByteArray();
        Assertions.assertEquals("{\"id\":123}", new String(bytes, "UTF-8"));

        converter.setSupportedMediaTypes(Collections
                .singletonList(MediaType.APPLICATION_JSON));

        converter.write(vo, VO.class, null, out);

        converter.write(vo, VO.class, MediaType.ALL, out);

        HttpOutputMessage out2 = new HttpOutputMessage() {
            public HttpHeaders getHeaders() {
                return new HttpHeaders() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public MediaType getContentType() {
                        return MediaType.APPLICATION_JSON;
                    }

                    @Override
                    public long getContentLength() {
                        return 1;
                    }
                };
            }

            public OutputStream getBody() throws IOException {
                return byteOut;
            }
        };

        converter.write(vo, VO.class, MediaType.ALL, out2);
    }

    private SerializeFilter serializeFilter = new ValueFilter() {
        @Override
        public Object process(Object object, String name, Object value) {
            if (value == null) {
                return "";
            }
            if (value instanceof Number) {
                return String.valueOf(value);
            }
            return value;
        }
    };

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
