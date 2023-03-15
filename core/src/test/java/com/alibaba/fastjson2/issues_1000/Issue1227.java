package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderBean;
import com.alibaba.fastjson2.reader.ObjectReaderImplDate;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1227 {
    @Test
    public void test() {
        String str = "{\"sqlDate\":\"2021-11-11 12:12:12 CST\",\"timestamp\":\"2023-03-13 09:41:16.404 CST\",\"utilDate\":\"2020-12-31 00:00:00 CST\"}";

        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean.sqlDate);
        assertNotNull(bean.timestamp);
        assertNotNull(bean.utilDate);

        Bean bean1 = JSON.parseObject(str.getBytes(), Bean.class);
        assertNotNull(bean1.sqlDate);
        assertNotNull(bean1.timestamp);
        assertNotNull(bean1.utilDate);

        assertEquals(bean.sqlDate, bean1.sqlDate);
        assertEquals(bean.timestamp, bean1.timestamp);
        assertEquals(bean.utilDate, bean1.utilDate);

        Bean bean2 = JSON.parseObject(str).toJavaObject(Bean.class);
        assertNotNull(bean2.sqlDate);
        assertNotNull(bean2.timestamp);
        assertNotNull(bean2.utilDate);

        assertEquals(bean.sqlDate, bean2.sqlDate);
        assertEquals(bean.timestamp, bean2.timestamp);
        assertEquals(bean.utilDate, bean2.utilDate);
    }

    @Test
    public void test1() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReaderImplDate dateReader = ObjectReaderImplDate.of("yyyy-MM-dd HH:mm:ss zzz", Locale.CHINA);
        provider.register(java.util.Date.class, dateReader);
        ObjectReaderBean objectReader = (ObjectReaderBean) provider.getObjectReader(Bean.class);
        JSONReader.Context context = JSONFactory.createReadContext(provider);
        ObjectReader dateReader1 = objectReader
                .getFieldReader("utilDate")
                .getObjectReader(context);
        assertSame(dateReader, dateReader1);

        String str = "{\"sqlDate\":\"2021-11-11 12:12:12 CST\",\"timestamp\":\"2023-03-13 09:41:16.404 CST\",\"utilDate\":\"2020-12-31 00:00:00 CST\"}";
        Bean bean = JSON.parseObject(str, Bean.class, context);
        assertNotNull(bean.sqlDate);
        assertNotNull(bean.timestamp);
        assertNotNull(bean.utilDate);
    }

    public static class Bean {
        public java.sql.Date sqlDate;
        public java.sql.Timestamp timestamp;
        public java.util.Date utilDate;
    }
}
