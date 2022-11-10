package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OracleClobTest {
    @Test
    public void test() {
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        assertEquals(JdbcSupport.ClobWriter.class, provider.getObjectWriter(oracle.sql.CLOB.class).getClass());
        assertEquals(JdbcSupport.ClobWriter.class, provider.getObjectWriter(oracle.sql.NCLOB.class).getClass());

        assertEquals(JdbcSupport.ClobWriter.class, provider.getObjectWriter(oracle.jdbc.driver.OracleClob.class).getClass());
        assertEquals(JdbcSupport.ClobWriter.class, provider.getObjectWriter(oracle.jdbc.driver.OracleNClob.class).getClass());
    }
}
