package com.alibaba.fastjson2.spring.codec;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.codec.Fastjson2Decoder;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fastjson2Decoder 单元测试
 *
 * @author 张治保
 * @since 2025/8/6
 */
public class Fastjson2DecoderTest {
    private Fastjson2Decoder decoderAll;
    private Fastjson2Decoder decoderJson;
    private DefaultDataBufferFactory bufferFactory;

    @BeforeEach
    void setUp() {
        decoderAll = new Fastjson2Decoder();
        decoderJson = new Fastjson2Decoder(new FastJsonConfig(), MediaType.APPLICATION_JSON);
        bufferFactory = new DefaultDataBufferFactory();
    }

    @Test
    void testDefaultConstructor() {
        Fastjson2Decoder defaultDecoder = new Fastjson2Decoder();
        assertNotNull(defaultDecoder);
        assertTrue(defaultDecoder.canDecode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void testConstructorWithConfig() {
        FastJsonConfig config = new FastJsonConfig();
        Fastjson2Decoder customDecoder = new Fastjson2Decoder(config, MediaType.APPLICATION_JSON);
        assertNotNull(customDecoder);
        assertTrue(customDecoder.canDecode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void testDecodeSimpleObject() {
        // 准备测试数据
        String json = "{\"id\":123,\"name\":\"test\"}";
        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 执行解码
        Object result = decoderAll.decode(buffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(result);
        assertInstanceOf(TestVO.class, result);
        TestVO vo = (TestVO) result;
        assertEquals(123, vo.getId());
        assertEquals("test", vo.getName());
    }

    @Test
    void testDecodeToMono() {
        // 准备测试数据
        String json = "{\"id\":456,\"name\":\"mono test\"}";
        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 执行解码
        Mono<Object> mono = decoderAll.decodeToMono(Flux.just(buffer), type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        StepVerifier.create(mono)
                .expectNextMatches(result -> {
                    assertInstanceOf(TestVO.class, result);
                    TestVO vo = (TestVO) result;
                    return vo.getId() == 456 && "mono test".equals(vo.getName());
                })
                .verifyComplete();
    }

    @Test
    void testDecodeFlux() {
        // 准备测试数据
        String json1 = "{\"id\":1,\"name\":\"first\"}";
        String json2 = "{\"id\":2,\"name\":\"second\"}";

        DataBuffer buffer1 = bufferFactory.wrap(json1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = bufferFactory.wrap(json2.getBytes(StandardCharsets.UTF_8));

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 执行解码
        Flux<Object> flux = decoderAll.decode(Flux.just(buffer1, buffer2), type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        StepVerifier.create(flux)
                .expectNextMatches(result -> {
                    assertInstanceOf(TestVO.class, result);
                    TestVO vo = (TestVO) result;
                    return vo.getId() == 1 && "first".equals(vo.getName());
                })
                .expectNextMatches(result -> {
                    assertInstanceOf(TestVO.class, result);
                    TestVO vo = (TestVO) result;
                    return vo.getId() == 2 && "second".equals(vo.getName());
                })
                .verifyComplete();
    }

    @Test
    void testDecodePrimitiveTypes() {
        // 测试整数
        DataBuffer intBuffer = bufferFactory.wrap("123".getBytes(StandardCharsets.UTF_8));
        Object intResult = decoderAll.decode(intBuffer, ResolvableType.forClass(Integer.class), MediaType.APPLICATION_JSON, null);
        assertEquals(123, intResult);

        // 测试字符串
        DataBuffer stringBuffer = bufferFactory.wrap("\"hello world\"".getBytes(StandardCharsets.UTF_8));
        Object stringResult = decoderAll.decode(stringBuffer, ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON, null);
        assertEquals("hello world", stringResult);

        // 测试布尔值
        DataBuffer boolBuffer = bufferFactory.wrap("true".getBytes(StandardCharsets.UTF_8));
        Object boolResult = decoderAll.decode(boolBuffer, ResolvableType.forClass(Boolean.class), MediaType.APPLICATION_JSON, null);
        assertEquals(true, boolResult);
    }

    @Test
    void testDecodeWithCustomConfig() {
        // 创建自定义配置
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        Fastjson2Decoder customDecoder = new Fastjson2Decoder(config, MediaType.APPLICATION_JSON);

        // 测试带日期的对象
        String json = "{\"id\":1,\"name\":\"test\",\"date\":\"2023-01-01 12:00:00\"}";
        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));

        ResolvableType type = ResolvableType.forClass(TestVOWithDate.class);
        Object result = customDecoder.decode(buffer, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        assertInstanceOf(TestVOWithDate.class, result);
    }

    @Test
    void testDecodeInvalidJson() {
        // 准备无效的 JSON 数据
        String invalidJson = "{\"id\":123,\"name\":}";
        DataBuffer buffer = bufferFactory.wrap(invalidJson.getBytes(StandardCharsets.UTF_8));

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 验证抛出异常
        assertThrows(DecodingException.class, () -> {
            decoderAll.decode(buffer, type, MediaType.APPLICATION_JSON, null);
        });
    }

    @Test
    void testDecodeEmptyBuffer() {
        // 准备空数据
        DataBuffer emptyBuffer = bufferFactory.wrap(new byte[0]);

        ResolvableType type = ResolvableType.forClass(String.class);

        // 验证空数据解码
        Object result = decoderAll.decode(emptyBuffer, type, MediaType.APPLICATION_JSON, null);
        assertNull(result);
    }

    @Test
    void testCanDecode() {
        // 测试支持的媒体类型
        assertTrue(decoderAll.canDecode(ResolvableType.forClass(String.class), MediaType.TEXT_PLAIN));
        assertTrue(decoderAll.canDecode(ResolvableType.forClass(String.class), MediaType.APPLICATION_XML));
        assertTrue(decoderAll.canDecode(ResolvableType.forClass(TestVO.class), MediaType.APPLICATION_JSON));
        assertTrue(decoderAll.canDecode(ResolvableType.forClass(Integer.class), MediaType.APPLICATION_JSON));

        // 测试不支持的媒体类型
        assertFalse(decoderJson.canDecode(ResolvableType.forClass(String.class), MediaType.TEXT_PLAIN));
        assertFalse(decoderJson.canDecode(ResolvableType.forClass(String.class), MediaType.TEXT_HTML));
        assertFalse(decoderJson.canDecode(ResolvableType.forClass(String.class), MediaType.APPLICATION_XML));
    }

    @Test
    void testDecodeWithHints() {
        // 准备测试数据
        String json = "{\"id\":789,\"name\":\"hints test\"}";
        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));

        ResolvableType type = ResolvableType.forClass(TestVO.class);
        Map<String, Object> hints = new HashMap<String, Object>() {{
                put("test", "value");
            }};

        // 执行解码
        Object result = decoderAll.decode(buffer, type, MediaType.APPLICATION_JSON, hints);

        // 验证结果
        assertNotNull(result);
        assertInstanceOf(TestVO.class, result);
        TestVO vo = (TestVO) result;
        assertEquals(789, vo.getId());
        assertEquals("hints test", vo.getName());
    }

    // 测试用的 VO 类
    @Setter
    @Getter
    private static class TestVO {
        private int id;
        private String name;
    }

    // 带日期的测试 VO 类
    @Getter
    @Setter
    private static class TestVOWithDate {
        private int id;
        private String name;
        private String date;
    }
}
