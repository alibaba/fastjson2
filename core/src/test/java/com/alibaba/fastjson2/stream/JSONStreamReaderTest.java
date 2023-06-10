package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Date;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONStreamReaderTest {
    File tempFile;

    @Test
    public void test() throws Exception {
        init();

        try (
                InputStream fis = new FileInputStream(tempFile)
        ) {
            JSONStreamReader streamReader = JSONStreamReader.of(fis);

            int rowCount = 0;
            Object object;
            while ((object = streamReader.readLineObject()) != null) {
                JSONObject jsonObject = (JSONObject) object;
                jsonObject.size();
                rowCount++;
            }

            assertEquals(7702, rowCount);
        }
    }

    @Test
    public void testStream() throws Exception {
        init();

        try (
                InputStream fis = new FileInputStream(tempFile)
        ) {
            JSONStreamReader streamReader = JSONStreamReader.of(fis);

            assertEquals(7702, streamReader.stream().count());
        }
    }

    @Test
    public void testStreamParallel() throws Exception {
        init();

        try (
                InputStream fis = new FileInputStream(tempFile)
        ) {
            JSONStreamReader<Object> streamReader = JSONStreamReader.of(fis);

            assertEquals(7702, streamReader.stream().parallel().count());
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
            assertEquals("parallel stream not supported", e.getMessage());
        }
    }

    @Test
    public void testObj() throws Exception {
        init();

        try (
                InputStream fis = new FileInputStream(tempFile)
        ) {
            JSONStreamReader streamReader = JSONStreamReader.of(fis, Event.class);

            int rowCount = 0;
            Object object;
            while ((object = streamReader.readLineObject()) != null) {
                Event event = (Event) object;
                rowCount++;
            }

            assertEquals(7702, rowCount);
        }
    }

    @Test
    public void testObjGeneric() throws Exception {
        init();

        try (
                InputStream fis = new FileInputStream(tempFile)
        ) {
            JSONStreamReader<Event> streamReader = JSONStreamReader.of(fis, Event.class);

            int rowCount = 0;
            Event event;
            while ((event = streamReader.readLineObject()) != null) {
                rowCount++;
            }

            assertEquals(7702, rowCount);
        }
    }

    @Test
    public void testObjStream() throws Exception {
        init();

        try (
                InputStream fis = new FileInputStream(tempFile)
        ) {
            JSONStreamReader<Event> streamReader = JSONStreamReader.of(fis, Event.class);

            assertEquals(7702, streamReader.stream().filter(Objects::nonNull).count());
        }
    }

    @Test
    public void statAll() throws Exception {
        init();

        long lines = IOUtils.lines(tempFile);
        assertEquals(7702L, lines);

        JSONStreamReader reader = JSONStreamReader.of(tempFile);
        reader.statAll();

        System.out.println(
                JSON.toJSONString(reader.columnStats, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.NotWriteDefaultValue)
        );
    }

    private void init() throws IOException {
        if (tempFile == null) {
            tempFile = File.createTempFile("tmp", "json");

            InputStream fis = JSONStreamReaderTest.class.getClassLoader().getResourceAsStream("data/gharchive-2015-01-01-0.json.zip");
            ZipInputStream zipIn = new ZipInputStream(fis);
            zipIn.getNextEntry();

            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024 * 8];
            while (true) {
                int size = zipIn.read(buf);
                if (size == -1) {
                    break;
                }
                if (size > 0) {
                    out.write(buf, 0, size);
                }
            }
            out.close();
            zipIn.close();
        }
    }

    public static class Event {
        public String id;
        public String type;
        public Actor actor;
        public Repo repo;
        public Payload payload;

        @JSONField(name = "public")
        public boolean isPublic;
        @JSONField(name = "created_at")
        public Date createdAt;
    }

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class Actor {
        public long id;
        public String login;
        public String displayLogin;
        public String gravatarId;
        public String url;
        public String avatarUrl;
    }

    public static class Repo {
        public long id;
        public String name;
        public String url;
    }

    public static class Payload {
        public Forkee forkee;
    }

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class Forkee {
        public long id;
        public String nodeId;
        public String name;
        public String fullName;
        @JSONField(name = "private")
        public boolean isPrivate;
        public Owner owner;
    }

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class Owner {
        public long id;
        public String login;
        public String nodeId;
        public String avatarUrl;
        public String gravatarId;
    }
}
