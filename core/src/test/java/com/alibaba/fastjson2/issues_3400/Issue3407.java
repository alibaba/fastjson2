package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3407 {
    @Test
    public void test() {
        String format = "yyyy-MM-dd HH:mm:ss";
        long millis = System.currentTimeMillis();

        ZonedDateTime zdt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault());
        String expected = "{\"create_time\":\"" + DateTimeFormatter.ofPattern(format).format(zdt) + "\"}";

        Bean bean = new Bean();
        bean.createTime = millis;
        assertEquals(expected, JSON.toJSONString(bean));
        assertEquals(expected, new String(JSON.toJSONBytes(bean), StandardCharsets.UTF_8));
        assertEquals(expected, ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class).toJSONString(bean));

        Bean1 bean1 = new Bean1();
        bean1.createTime = millis;
        assertEquals(expected, JSON.toJSONString(bean1));
        assertEquals(expected, new String(JSON.toJSONBytes(bean1), StandardCharsets.UTF_8));
        assertEquals(expected, ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class).toJSONString(bean1));
    }

    public static class Bean {
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        @JSONField(name = "create_time", ordinal = 14, format = "yyyy-MM-dd HH:mm:ss")
        public long createTime;
    }

    public static class Bean1 {
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        @JSONField(name = "create_time", ordinal = 14, format = "yyyy-MM-dd HH:mm:ss")
        private long createTime;

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }
    }
}
