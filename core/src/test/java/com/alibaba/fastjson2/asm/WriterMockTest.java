package com.alibaba.fastjson2.asm;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

public class WriterMockTest {
    @Test
    public void testUTF8() {
        byte[] NAME_ID = "\"name\":".getBytes(StandardCharsets.ISO_8859_1);
        byte[] NAME_NAME = "\"id\":".getBytes(StandardCharsets.ISO_8859_1);
        byte[] NAME_SINCE = "\"since\":".getBytes(StandardCharsets.ISO_8859_1);

        Bean bean = new Bean();
        bean.id = 123456789L;
        bean.name = "abc";
        bean.since = OffsetDateTime.now();

        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            int offset = writer.getOffset();
            int minCapacity = offset + 23;

            String name = bean.name;

            byte[] bytes = (byte[]) writer.ensureCapacity(minCapacity);
            bytes[offset++] = '{';

            System.arraycopy(NAME_ID, 0, bytes, offset, NAME_ID.length);
            offset += NAME_ID.length;
            offset = IOUtils.writeInt64(bytes, offset, bean.id);

            bytes[offset++] = ',';

            System.arraycopy(NAME_NAME, 0, bytes, offset, NAME_NAME.length);
            offset += NAME_NAME.length;
            offset = StringUtils.writeLatin1(bytes, offset, name.getBytes(StandardCharsets.ISO_8859_1), (byte) '"');
            bytes[offset++] = '}';
            writer.setOffset(offset);
            System.out.println(writer.toString());
        }
    }

    public static class Bean {
        public long id;
        public String name;
        public OffsetDateTime since;
    }
}
