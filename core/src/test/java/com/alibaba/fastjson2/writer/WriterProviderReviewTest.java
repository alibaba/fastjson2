package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class WriterProviderReviewTest {
    @Test
    public void clearingMixInsInvalidatesOnlyTargetWriters() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriter custom = ObjectWriters.ofToString(Object::toString);
        provider.register(String.class, custom);
        provider.mixIn(Bean.class, MixIn.class);
        for (boolean fieldBased : new boolean[]{false, true}) {
            assertNotNull(provider.getObjectWriter(Bean.class, Bean.class, fieldBased).getFieldWriter("renamed"));
        }
        provider.cleanupMixIn();
        assertSame(custom, provider.getObjectWriter(String.class));
        for (boolean fieldBased : new boolean[]{false, true}) {
            ObjectWriter writer = provider.getObjectWriter(Bean.class, Bean.class, fieldBased);
            assertNotNull(writer.getFieldWriter("value"));
            assertNull(writer.getFieldWriter("renamed"));
        }
    }

    @Test
    public void mixInInvalidatesBothCaches() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.getObjectWriter(Bean.class, Bean.class, false);
        provider.getObjectWriter(Bean.class, Bean.class, true);
        provider.mixIn(Bean.class, MixIn.class);
        for (boolean fieldBased : new boolean[] {false, true}) {
            ObjectWriter writer = provider.getObjectWriter(Bean.class, Bean.class, fieldBased);
            assertNotNull(writer.getFieldWriter("renamed"));
            assertNull(writer.getFieldWriter("value"));
        }
        provider.mixIn(Bean.class, null);
        assertNotNull(provider.getObjectWriter(Bean.class, Bean.class, true).getFieldWriter("value"));
    }

    @Test
    public void formattedWriterHonorsLocale() {
        ObjectWriter writer = new ObjectWriterProvider().getObjectWriter(LocalDate.class, "dd MMMM yyyy", Locale.FRANCE);
        LocalDate date = LocalDate.of(2020, 1, 2);
        try (JSONWriter json = JSONWriter.of()) {
            writer.write(json, date);
            assertEquals("\"" + DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE).format(date) + "\"", json.toString());
        }
    }

    public static class Bean {
        public int value;
    }

    @Test
    public void reflectiveFieldWriterHasCorrectTypeMetadata() throws Exception {
        String json = JSON.toJSONString(Bean.class.getField("value"), JSONWriter.Feature.WriteClassName);
        assertEquals("java.lang.reflect.Field", JSON.parseObject(json).getString("@type"));
    }

    @Test
    public void interfaceMethodUsesInterfaceMixIn() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.mixIn(Named.class, NamedMixIn.class);
        assertEquals("{\"renamed\":123}", provider.getObjectWriter(NamedBean.class).toJSONString(new NamedBean()));
    }

    public interface Named {
        int getValue();
    }

    public interface NamedMixIn {
        @JSONField(name = "renamed")
        int getValue();
    }

    public static class NamedBean implements Named {
        @Override
        public int getValue() {
            return 123;
        }
    }

    public static class MixIn {
        @JSONField(name = "renamed")
        public int value;
    }
}
