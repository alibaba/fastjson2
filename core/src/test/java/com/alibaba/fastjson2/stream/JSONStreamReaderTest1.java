package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONStreamReaderTest1 {
    @Test
    public void testUTF8() throws Exception {
        String str = "[101,102,103]\n[201,202,203]";
        byte[] bytes = str.getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        JSONStreamReader reader = JSONStreamReader.of(in, Long.class, String.class, BigDecimal.class);

        List obj1 = (List) reader.readLineObject();
        assertEquals(101L, obj1.get(0));
        assertEquals("102", obj1.get(1));
        assertEquals(BigDecimal.valueOf(103), obj1.get(2));

        List obj2 = (List) reader.readLineObject();
        assertEquals(201L, obj2.get(0));
        assertEquals("202", obj2.get(1));
        assertEquals(BigDecimal.valueOf(203), obj2.get(2));
    }

    @Test
    public void testUTF8_1() throws Exception {
        String str = "[101,102,103]\n[201,202,203]";
        Charset charset = StandardCharsets.UTF_8;
        byte[] bytes = str.getBytes(charset);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        JSONStreamReader reader = JSONStreamReader.of(in, charset);

        List obj1 = (List) reader.readLineObject();
        assertEquals("[101,102,103]", JSON.toJSONString(obj1));

        List obj2 = (List) reader.readLineObject();
        assertEquals("[201,202,203]", JSON.toJSONString(obj2));
    }

    @Test
    public void testUTF8_1Generic() throws Exception {
        String str = "[101,102,103]\n[201,202,203]";
        Charset charset = StandardCharsets.UTF_8;
        byte[] bytes = str.getBytes(charset);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        JSONStreamReader<List> reader = JSONStreamReader.of(in, charset);

        List list1 = reader.readLineObject();
        assertEquals("[101,102,103]", JSON.toJSONString(list1));

        List list2 = reader.readLineObject();
        assertEquals("[201,202,203]", JSON.toJSONString(list2));
    }

    @Test
    public void testUTF16() throws Exception {
        String str = "[101,102,103]\n[201,202,203]";
        Charset charset = StandardCharsets.UTF_16;
        byte[] bytes = str.getBytes(charset);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        JSONStreamReader reader = JSONStreamReader.of(in, charset, Long.class, String.class, BigDecimal.class);

        List obj1 = (List) reader.readLineObject();
        assertEquals(101L, obj1.get(0));
        assertEquals("102", obj1.get(1));
        assertEquals(BigDecimal.valueOf(103), obj1.get(2));

        List obj2 = (List) reader.readLineObject();
        assertEquals(201L, obj2.get(0));
        assertEquals("202", obj2.get(1));
        assertEquals(BigDecimal.valueOf(203), obj2.get(2));
    }

    @Test
    public void testUTF16_1() throws Exception {
        String str = "[101,102,103]\n[201,202,203]";
        Charset charset = StandardCharsets.UTF_16;
        byte[] bytes = str.getBytes(charset);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        JSONStreamReader reader = JSONStreamReader.of(in, charset);

        List obj1 = (List) reader.readLineObject();
        assertEquals("[101,102,103]", JSON.toJSONString(obj1));

        List obj2 = (List) reader.readLineObject();
        assertEquals("[201,202,203]", JSON.toJSONString(obj2));
    }

    @Test
    public void testUTF16_1Generic() throws Exception {
        String str = "[101,102,103]\n[201,202,203]";
        Charset charset = StandardCharsets.UTF_16;
        byte[] bytes = str.getBytes(charset);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        JSONStreamReader<List> reader = JSONStreamReader.of(in, charset);

        List list1 = reader.readLineObject();
        assertEquals("[101,102,103]", JSON.toJSONString(list1));

        List list2 = reader.readLineObject();
        assertEquals("[201,202,203]", JSON.toJSONString(list2));
    }
}
