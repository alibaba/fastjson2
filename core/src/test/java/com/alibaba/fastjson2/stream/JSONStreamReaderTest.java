package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
