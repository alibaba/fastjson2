package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumValueMixinTest2 {
    @Test
    public void test() {
        ObjectWriterProvider writerProvider = new ObjectWriterProvider();
        writerProvider.mixIn(XEnum.class, XEnumMixin.class);
        ObjectWriter<Bean> objectWriter = writerProvider.getObjectWriter(Bean.class);

        Bean bean = new Bean();
        bean.size = Size.Large;

        JSONWriter jsonWriter = JSONWriter.of(writerProvider);
        objectWriter.write(jsonWriter, bean);
        String str = jsonWriter.toString();
        assertEquals("{\"size\":101}", str);

        ObjectReaderProvider readerProvider = new ObjectReaderProvider();
        ObjectReader<Bean> objectReader = readerProvider.getObjectReader(Bean.class);
        readerProvider.mixIn(XEnum.class, XEnumMixin.class);

        JSONReader jsonReader = JSONReader.of(str, 0, str.length(), JSONFactory.createReadContext(readerProvider));
        Bean bean1 = objectReader.readObject(jsonReader);
        assertEquals(bean.size, bean1.size);
    }

    public interface XEnumMixin {
        @JSONField(value = true)
        int getValue();
    }

    public static class Bean {
        private Size size;

        public Size getSize() {
            return size;
        }

        public void setSize(Size size) {
            this.size = size;
        }
    }

    public interface XEnum {
        int getValue();
    }

    public enum Size implements XEnum {
        Small(99),
        Medium(100),
        Large(101),
        XLarge(102);

        private final int value;

        Size(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }
}
