package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.UUID1;
import com.alibaba.fastjson2_vo.UUIDFIeld2;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UUIDTest {
    static UUID[] values = new UUID[]{
            UUID.randomUUID(),
            null
    };

    @Test
    public void isUUID() {
        assertTrue(TypeUtils.isUUID("2626c7cc-8466-45a4-aa93-2638546f56e1"));
        assertTrue(TypeUtils.isUUID("2626c7cc846645a4aa932638546f56e1"));

        assertFalse(TypeUtils.isUUID("abc"));

        assertFalse(TypeUtils.isUUID("2626c7cc+8466-45a4-aa93-2638546f56e1"));
        assertFalse(TypeUtils.isUUID("2626c7cc-8466+45a4-aa93-2638546f56e1"));
        assertFalse(TypeUtils.isUUID("2626c7cc-8466-45a4+aa93-2638546f56e1"));
        assertFalse(TypeUtils.isUUID("2626c7cc-8466-45a4-aa93+2638546f56e1"));

        assertFalse(TypeUtils.isUUID("M626c7cc-8466-45a4-aa93-2638546f56e1"));
        assertFalse(TypeUtils.isUUID("#M626c7cc-8466-45a4-aa93-2638546f56e1"));
        assertFalse(TypeUtils.isUUID("2626c7cc-8466-45a4-aa93-2638546f56em"));
        assertFalse(TypeUtils.isUUID("2626c7cc-8466-45a4-aa93-2638546f56e#"));

        assertFalse(TypeUtils.isUUID("M626c7cc846645a4aa932638546f56e1"));
        assertFalse(TypeUtils.isUUID("2626c7cc846645a4aa932638546f56eM"));
        assertFalse(TypeUtils.isUUID("#626c7cc846645a4aa932638546f56e1"));
        assertFalse(TypeUtils.isUUID("2626c7cc846645a4aa932638546f56e#"));
    }

    @Test
    public void test_jsonb() {
        for (UUID dateTime : values) {
            UUID1 vo = new UUID1();
            vo.setId(dateTime);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            JSONB.dump(jsonbBytes);

            UUID1 v1 = JSONB.parseObject(jsonbBytes, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_utf8() {
        for (UUID uuid : values) {
            UUID1 vo = new UUID1();
            vo.setId(uuid);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            UUID1 v1 = JSON.parseObject(utf8Bytes, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void readUUID() {
        UUID uuid = UUID.fromString("3eef3eee-eeee-f0ef-ef0e-f12eeeeef53f");
        String str = uuid.toString();
        assertEquals(
                uuid,
                JSONReader.ofJSONB(
                        JSONB.toBytes(str)
                ).readUUID()
        );
        assertEquals(
                uuid,
                JSONReader.ofJSONB(
                        JSONB.toBytes(
                                str.replace("-", "")
                        )
                ).readUUID()
        );
    }

    @Test
    public void test_utf8_1() {
        String str = "d9ac58be-c854-496b-b550-56f0b773d241";
        UUID uuid = UUID.fromString(str);

        String jsonStr = "\"" + str + "\"";
        {
            UUID parsed = JSONB.parseObject(
                    JSONB.toBytes(str), UUID.class);
            assertEquals(uuid, parsed);
        }
        {
            UUID parsed = JSON.parseObject(jsonStr, UUID.class);
            assertEquals(uuid, parsed);
        }
        {
            UUID parsed = JSON.parseObject(jsonStr.toCharArray(), UUID.class);
            assertEquals(uuid, parsed);
        }
        {
            UUID parsed = JSON.parseObject(jsonStr.getBytes(StandardCharsets.UTF_8), UUID.class);
            assertEquals(uuid, parsed);
        }
        byte[] utf8Bytes = JSON.toJSONBytes(uuid);
        assertEquals("\"d9ac58be-c854-496b-b550-56f0b773d241\"", new String(utf8Bytes));
    }

    @Test
    public void test_str() {
        for (UUID uuid : values) {
            UUID1 vo = new UUID1();
            vo.setId(uuid);
            String str = JSON.toJSONString(vo);

            UUID1 v1 = JSON.parseObject(str, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_str_1() {
        String str = "d9ac58be-c854-496b-b550-56f0b773d241";
        UUID uuid = UUID.fromString(str);
        String json = JSON.toJSONString(uuid);
        assertEquals("\"d9ac58be-c854-496b-b550-56f0b773d241\"", json);
    }

    @Test
    public void test_ascii() {
        for (UUID dateTime : values) {
            UUID1 vo = new UUID1();
            vo.setId(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            UUID1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_2() {
        {
            UUIDFIeld2 vo = new UUIDFIeld2();
            vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

            String str = JSON.toJSONString(vo);
            assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
        }
        {
            UUIDFIeld2 vo = new UUIDFIeld2();
            vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

            String str = JSON.toJSONString(vo);
            assertEquals("{\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
        }
        {
            UUIDFIeld2 vo = new UUIDFIeld2();
            vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");
            vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

            String str = JSON.toJSONString(vo);
            assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\",\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
        }
    }

    @Test
    public void test_creators() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter objectWriter = creator.createObjectWriter(UUIDFIeld2.class);

            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
                assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
                assertEquals("{\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");
                vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
                assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\",\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }

            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
                assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
                assertEquals("{\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");
                vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
                assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\",\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }

            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
//                assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
//                assertEquals("{\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
            {
                UUIDFIeld2 vo = new UUIDFIeld2();
                vo.v0000 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");
                vo.v0001 = UUID.fromString("d9ac58be-c854-496b-b550-56f0b773d241");

                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                String str = jsonWriter.toString();
//                assertEquals("{\"v0000\":\"d9ac58be-c854-496b-b550-56f0b773d241\",\"v0001\":\"d9ac58be-c854-496b-b550-56f0b773d241\"}", str);
            }
        }
    }
}
