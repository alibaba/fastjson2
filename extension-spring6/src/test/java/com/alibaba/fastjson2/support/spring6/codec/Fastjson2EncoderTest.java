package com.alibaba.fastjson2.support.spring6.codec;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fastjson2Encoder 单元测试
 *
 * @author 张治保
 * @since 2025/8/6
 */
public class Fastjson2EncoderTest {
    private Fastjson2Encoder encoderAll;
    private Fastjson2Encoder encoderJson;
    private DefaultDataBufferFactory bufferFactory;

    @BeforeEach
    void setUp() {
        encoderAll = new Fastjson2Encoder();
        encoderJson = new Fastjson2Encoder(new FastJsonConfig(), MediaType.APPLICATION_JSON);
        bufferFactory = new DefaultDataBufferFactory();
    }

    @Test
    void testDefaultConstructor() {
        Fastjson2Encoder defaultEncoder = new Fastjson2Encoder();
        assertNotNull(defaultEncoder);
        assertTrue(defaultEncoder.canEncode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void testConstructorWithConfig() {
        FastJsonConfig config = new FastJsonConfig();
        Fastjson2Encoder customEncoder = new Fastjson2Encoder(config, MediaType.APPLICATION_JSON);
        assertNotNull(customEncoder);
        assertTrue(customEncoder.canEncode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void testEncodeSimpleObject() {
        // 准备测试数据
        TestVO vo = new TestVO();
        vo.setId(123);
        vo.setName("test");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 执行编码
        DataBuffer result = encoderAll.encodeValue(vo, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        assertNotNull(result);
        String json = result.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"id\":123"));
        assertTrue(json.contains("\"name\":\"test\""));
    }

    @Test
    void testEncodeFlux() {
        // 准备测试数据
        TestVO vo1 = new TestVO();
        vo1.setId(1);
        vo1.setName("first");

        TestVO vo2 = new TestVO();
        vo2.setId(2);
        vo2.setName("second");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 执行编码
        Flux<DataBuffer> flux = encoderAll.encode(Flux.just(vo1, vo2), bufferFactory, type, MediaType.APPLICATION_JSON, null);

        // 验证结果
        StepVerifier.create(flux)
                .expectNextMatches(buffer -> {
                    String json = buffer.toString(StandardCharsets.UTF_8);
                    return json.contains("\"id\":1") && json.contains("\"name\":\"first\"");
                })
                .expectNextMatches(buffer -> {
                    String json = buffer.toString(StandardCharsets.UTF_8);
                    return json.contains("\"id\":2") && json.contains("\"name\":\"second\"");
                })
                .verifyComplete();
    }

    @Test
    void testEncodePrimitiveTypes() {
        // 测试整数
        ResolvableType intType = ResolvableType.forClass(Integer.class);
        DataBuffer intBuffer = encoderAll.encodeValue(123, bufferFactory, intType, MediaType.APPLICATION_JSON, null);
        assertEquals("123", intBuffer.toString(StandardCharsets.UTF_8));

        // 测试字符串
        ResolvableType stringType = ResolvableType.forClass(String.class);
        DataBuffer stringBuffer = encoderAll.encodeValue("hello world", bufferFactory, stringType, MediaType.APPLICATION_JSON, null);
        assertEquals("\"hello world\"", stringBuffer.toString(StandardCharsets.UTF_8));

        // 测试布尔值
        ResolvableType boolType = ResolvableType.forClass(Boolean.class);
        DataBuffer boolBuffer = encoderAll.encodeValue(true, bufferFactory, boolType, MediaType.APPLICATION_JSON, null);
        assertEquals("true", boolBuffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    void testEncodeWithCustomConfig() {
        // 创建自定义配置
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        Fastjson2Encoder customEncoder = new Fastjson2Encoder(config, MediaType.APPLICATION_JSON);

        // 测试带日期的对象
        TestVOWithDate vo = new TestVOWithDate();
        vo.setId(1);
        vo.setName("test");
        vo.setDate("2023-01-01 12:00:00");

        ResolvableType type = ResolvableType.forClass(TestVOWithDate.class);
        DataBuffer result = customEncoder.encodeValue(vo, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        String json = result.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"date\":\"2023-01-01 12:00:00\""));
    }

    @Test
    void testEncodeValidJsonString() {
        // 测试有效的 JSON 字符串
        String validJson = "{\"id\":123,\"name\":\"test\"}";
        ResolvableType type = ResolvableType.forClass(String.class);

        DataBuffer result = encoderAll.encodeValue(validJson, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        String encoded = result.toString(StandardCharsets.UTF_8);
        assertEquals(validJson, encoded);
    }

    @Test
    void testEncodeValidJsonBytes() {
        // 测试有效的 JSON 字节数组
        String jsonString = "{\"id\":123,\"name\":\"test\"}";
        byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
        ResolvableType type = ResolvableType.forClass(byte[].class);

        DataBuffer result = encoderAll.encodeValue(jsonBytes, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        String encoded = result.toString(StandardCharsets.UTF_8);
        assertEquals(jsonString, encoded);
    }

    @Test
    void testEncodeInvalidJsonString() {
        // 测试无效的 JSON 字符串（不是有效的 JSON 对象）
        String invalidJson = "not a json object";
        ResolvableType type = ResolvableType.forClass(String.class);

        // 应该正常编码，因为不是有效的 JSON 对象
        DataBuffer result = encoderAll.encodeValue(invalidJson, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        String encoded = result.toString(StandardCharsets.UTF_8);
        assertEquals("\"not a json object\"", encoded);
    }

    @Test
    void testEncodeNull() {
        // 测试 null 值
        ResolvableType type = ResolvableType.forClass(Object.class);

        DataBuffer result = encoderAll.encodeValue(null, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        String encoded = result.toString(StandardCharsets.UTF_8);
        assertEquals("null", encoded);
    }

    @Test
    void testEncodeComplexObject() {
        // 测试复杂对象
        ComplexVO complex = new ComplexVO();
        complex.setId(1);
        complex.setName("complex");
        complex.setTags(Arrays.asList("tag1", "tag2"));
        complex.setMetadata(
                new HashMap<String, String>() {
                    {
                        put("key1", "value1");
                        put("key2", "value2");
                    }
                }
        );

        ResolvableType type = ResolvableType.forClass(ComplexVO.class);

        DataBuffer result = encoderAll.encodeValue(complex, bufferFactory, type, MediaType.APPLICATION_JSON, null);

        assertNotNull(result);
        String json = result.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"complex\""));
        assertTrue(json.contains("\"tag1\""));
        assertTrue(json.contains("\"tag2\""));
        assertTrue(json.contains("\"key1\":\"value1\""));
        assertTrue(json.contains("\"key2\":\"value2\""));
    }

    @Test
    void testCanEncode() {
        // 测试不支持的媒体类型
        assertFalse(encoderAll.canEncode(ResolvableType.forClass(String.class), MediaType.TEXT_PLAIN));
        assertFalse(encoderAll.canEncode(ResolvableType.forClass(String.class), MediaType.APPLICATION_XML));
        assertTrue(encoderAll.canEncode(ResolvableType.forClass(TestVO.class), MediaType.APPLICATION_JSON));
        assertTrue(encoderAll.canEncode(ResolvableType.forClass(Integer.class), MediaType.APPLICATION_JSON));

        // 测试不支持的媒体类型
        assertFalse(encoderJson.canEncode(ResolvableType.forClass(String.class), MediaType.TEXT_PLAIN));
        assertFalse(encoderJson.canEncode(ResolvableType.forClass(String.class), MediaType.TEXT_HTML));
        assertFalse(encoderJson.canEncode(ResolvableType.forClass(String.class), MediaType.APPLICATION_XML));
    }

    @Test
    void testEncodeWithHints() {
        // 准备测试数据
        TestVO vo = new TestVO();
        vo.setId(789);
        vo.setName("hints test");

        ResolvableType type = ResolvableType.forClass(TestVO.class);
        //Map.of("test", "value");
        Map<String, Object> hints = new HashMap<String, Object>() {
            {
                put("test", "value");
            }
        };

        // 执行编码
        DataBuffer result = encoderAll.encodeValue(vo, bufferFactory, type, MediaType.APPLICATION_JSON, hints);

        // 验证结果
        assertNotNull(result);
        String json = result.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"id\":789"));
        assertTrue(json.contains("\"name\":\"hints test\""));
    }

    @Test
    void testEncodeWithDifferentMimeTypes() {
        // 测试不同的 MIME 类型
        TestVO vo = new TestVO();
        vo.setId(1);
        vo.setName("test");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 测试 APPLICATION_JSON
        DataBuffer jsonBuffer = encoderAll.encodeValue(vo, bufferFactory, type, MediaType.APPLICATION_JSON, null);
        assertNotNull(jsonBuffer);

        // 测试 ALL
        DataBuffer allBuffer = encoderAll.encodeValue(vo, bufferFactory, type, MediaType.ALL, null);
        assertNotNull(allBuffer);
    }

    @Test
    void testEncodeExceptionHandling() {
        // 测试编码异常处理
        // 创建一个可能导致编码异常的对象（如果有的话）
        // 这里我们测试一个正常情况，因为 Fastjson2 通常能处理大多数对象

        TestVO vo = new TestVO();
        vo.setId(1);
        vo.setName("test");

        ResolvableType type = ResolvableType.forClass(TestVO.class);

        // 正常情况下不应该抛出异常
        assertDoesNotThrow(() -> {
            encoderAll.encodeValue(vo, bufferFactory, type, MediaType.APPLICATION_JSON, null);
        });
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
}
