package com.alibaba.fastjson2.spring.codec;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.codec.Fastjson2Decoder;
import com.alibaba.fastjson2.support.spring.codec.Fastjson2Encoder;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fastjson2Encoder 和 Fastjson2Decoder 集成测试
 *
 * @author 张治保
 * @since 2025/8/6
 */
public class Fastjson2CodecTest {
    private Fastjson2Encoder encoder;
    private Fastjson2Decoder decoder;
    private DefaultDataBufferFactory bufferFactory;

    @BeforeEach
    void setUp() {
        encoder = new Fastjson2Encoder();
        decoder = new Fastjson2Decoder();
        bufferFactory = new DefaultDataBufferFactory();
    }

    @Test
    void testEncodeThenDecodeSimpleObject() {
        // 准备测试数据
        TestVO original = new TestVO();
        original.setId(123);
        original.setName("test object");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(decoded);
        assertInstanceOf(TestVO.class, decoded);
        TestVO result = (TestVO) decoded;
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
    }

    @Test
    void testEncodeThenDecodeComplexObject() {
        // 准备复杂测试数据
        ComplexVO original = new ComplexVO();
        original.setId(1);
        original.setName("complex test");
        original.setTags(Arrays.asList("tag1", "tag2", "tag3"));
        //Map.of("key1", "value1", "key2", "value2")
        original.setMetadata(
                new HashMap<String, String>() {
                    {
                        put("key1", "value1");
                        put("key2", "value2");
                    }
                }
        );

        ResolvableType type = ResolvableType.forClass(ComplexVO.class);

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(decoded);
        assertInstanceOf(ComplexVO.class, decoded);
        ComplexVO result = (ComplexVO) decoded;
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getTags(), result.getTags());
        assertEquals(original.getMetadata(), result.getMetadata());
    }

    @Test
    void testEncodeThenDecodeFlux() {
        // 准备测试数据
        TestVO vo1 = new TestVO();
        vo1.setId(1);
        vo1.setName("first");

        TestVO vo2 = new TestVO();
        vo2.setId(2);
        vo2.setName("second");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 编码 Flux
        Flux<DataBuffer> encodedFlux = encoder.encode(Flux.just(vo1, vo2), bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码 Flux
        Flux<Object> decodedFlux = decoder.decode(encodedFlux, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        StepVerifier.create(decodedFlux)
                .expectNextMatches(result -> {
                    assertInstanceOf(TestVO.class, result);
                    TestVO resultVO = (TestVO) result;
                    return resultVO.getId() == 1 && "first".equals(resultVO.getName());
                })
                .expectNextMatches(result -> {
                    assertInstanceOf(TestVO.class, result);
                    TestVO resultVO = (TestVO) result;
                    return resultVO.getId() == 2 && "second".equals(resultVO.getName());
                })
                .verifyComplete();
    }

    @Test
    void testEncodeThenDecodeMono() {
        // 准备测试数据
        TestVO original = new TestVO();
        original.setId(456);
        original.setName("mono test");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码为 Mono
        Mono<Object> decodedMono = decoder.decodeToMono(Flux.just(encodedBuffer), type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        StepVerifier.create(decodedMono)
                .expectNextMatches(result -> {
                    assertInstanceOf(TestVO.class, result);
                    TestVO resultVO = (TestVO) result;
                    return resultVO.getId() == 456 && "mono test".equals(resultVO.getName());
                })
                .verifyComplete();
    }

    @Test
    void testEncodeThenDecodeWithCustomConfig() {
        // 创建自定义配置
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");

        Fastjson2Encoder customEncoder = new Fastjson2Encoder(config, MediaType.APPLICATION_JSON);
        Fastjson2Decoder customDecoder = new Fastjson2Decoder(config, MediaType.APPLICATION_JSON);

        // 准备测试数据
        TestVOWithDate original = new TestVOWithDate();
        original.setId(1);
        original.setName("custom config test");
        original.setDate("2023-01-01 12:00:00");

        ResolvableType type = ResolvableType.forClass(TestVOWithDate.class);

        // 编码
        DataBuffer encodedBuffer = customEncoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = customDecoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(decoded);
        assertInstanceOf(TestVOWithDate.class, decoded);
        TestVOWithDate result = (TestVOWithDate) decoded;
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDate(), result.getDate());
    }

    @Test
    void testEncodeThenDecodePrimitiveTypes() {
        // 测试整数
        testEncodeThenDecodePrimitive(123, Integer.class);

        // 测试字符串
        testEncodeThenDecodePrimitive("hello world", String.class);

        // 测试布尔值
        testEncodeThenDecodePrimitive(true, Boolean.class);

        // 测试浮点数
        testEncodeThenDecodePrimitive(123.45, Double.class);
    }

    private <T> void testEncodeThenDecodePrimitive(T original, Class<T> clazz) {
        ResolvableType type = ResolvableType.forClass(clazz);

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(decoded);
        assertEquals(original, decoded);
    }

    @Test
    void testEncodeThenDecodeNull() {
        ResolvableType type = ResolvableType.forClass(Object.class);

        // 编码 null
        DataBuffer encodedBuffer = encoder.encodeValue(null, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNull(decoded);
    }

    @Test
    void testEncodeThenDecodeWithHints() {
        // 准备测试数据
        TestVO original = new TestVO();
        original.setId(789);
        original.setName("hints test");

        ResolvableType type = ResolvableType.forClass(TestVO.class);
        //Map.of("test", "value")
        Map<String, Object> hints = new HashMap<String, Object>() {
            {
                put("test", "value");
            }
        };

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, hints);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, hints);

        // 验证结果
        assertNotNull(decoded);
        assertInstanceOf(TestVO.class, decoded);
        TestVO result = (TestVO) decoded;
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
    }

    @Test
    void testEncodeThenDecodeLargeObject() {
        // 准备大型测试数据
        LargeVO original = new LargeVO();
        original.setId(1);
        original.setName("large test");
        original.setDescription("This is a large object for testing purposes");
        //"x".repeat(1000)
        char[] chars = new char[1000];
        Arrays.fill(chars, 'x');
        original.setData(new String(chars)); // 1KB 的数据

        ResolvableType type = ResolvableType.forClass(LargeVO.class);

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(decoded);
        assertInstanceOf(LargeVO.class, decoded);
        LargeVO result = (LargeVO) decoded;
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getData(), result.getData());
    }

    @Test
    void testEncodeThenDecodeWithSpecialCharacters() {
        // 准备包含特殊字符的测试数据
        TestVO original = new TestVO();
        original.setId(1);
        original.setName("特殊字符测试: 中文, English, 123, !@#$%^&*()");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 编码
        DataBuffer encodedBuffer = encoder.encodeValue(original, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 解码
        Object decoded = decoder.decode(encodedBuffer, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(decoded);
        assertInstanceOf(TestVO.class, decoded);
        TestVO result = (TestVO) decoded;
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
    }

    // 测试用的 VO 类
    @Setter
    @Getter
    private static class TestVO {
        private int id;
        private String name;
    }

    // 带日期的测试 VO 类
    @Setter
    @Getter
    private static class TestVOWithDate {
        private int id;
        private String name;
        private String date;
    }

    // 复杂对象测试类
    @Setter
    @Getter
    private static class ComplexVO {
        private int id;
        private String name;
        private List<String> tags;
        private Map<String, String> metadata;
    }

    // 大型对象测试类
    @Setter
    @Getter
    private static class LargeVO {
        private int id;
        private String name;
        private String description;
        private String data;
    }
}
