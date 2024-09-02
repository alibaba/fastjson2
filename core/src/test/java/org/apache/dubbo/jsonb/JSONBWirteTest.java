package org.apache.dubbo.jsonb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.dubbo.jsonb.model.UserDTO;
import org.apache.dubbo.jsonb.rw.BigDecimalReader;
import org.apache.dubbo.jsonb.rw.BigDecimalWriter;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

public class JSONBWirteTest {
    @Test
    public void jsonbWirteTest() {
        // 序列化BigDecimal类型的时候添加千分位逗号
        JSON.register(BigDecimal.class, BigDecimalWriter.INSTANCE);
        // 反序列化BigDecimal类型的时候去除千分位逗号
        JSON.register(BigDecimal.class, BigDecimalReader.INSTANCE);

        UserDTO user = new UserDTO();
        user.setName("张三");
        user.setDeposit(new BigDecimal("1000.01"));
        user.setBirthday(LocalDate.now());
        LocalDateTime actDate = LocalDateTime.of(2024, 7, 1, 10, 11, 56);
        user.setActDate(actDate);
        user.setCreateDate(LocalDateTime.of(2024, 7, 1, 10, 11, 56));
        user.setModifyDate(LocalDateTime.of(2024, 7, 2, 10, 11, 56));

        // 摘至dobbo源码org.apache.dubbo.common.serialize.fastjson2.FastJson2ObjectOutput.writeObject(Object)
        JSONWriter.Feature[] writeFeatures = { JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable, JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName, JSONWriter.Feature.WriteNameAsSymbol };

        // 摘至dobbo源码org.apache.dubbo.common.serialize.fastjson2.FastJson2ObjectInput.readObject(Class<T>)
        JSONReader.Feature[] readerFeatures = { JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable, JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject, JSONReader.Feature.FieldBased };

        byte[] bytes = JSONB.toBytes(user, writeFeatures);
        UserDTO result = JSONB.parseObject(bytes, UserDTO.class, readerFeatures);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getDeposit(), result.getDeposit());
        assertEquals("2024-07-01", result.getActDate().format(dtf));
    }

    @Test
    public void wirteTest() {
        // 序列化BigDecimal类型的时候添加千分位逗号
        JSON.register(BigDecimal.class, BigDecimalWriter.INSTANCE);
        // 反序列化BigDecimal类型的时候去除千分位逗号
        JSON.register(BigDecimal.class, BigDecimalReader.INSTANCE);

        UserDTO user = new UserDTO();
        user.setName("张三");
        user.setDeposit(new BigDecimal("1000.01"));
        user.setBirthday(LocalDate.now());
        LocalDateTime actDate = LocalDateTime.of(2024, 7, 1, 10, 11, 56);
        user.setActDate(actDate);
        user.setCreateDate(LocalDateTime.of(2024, 7, 1, 10, 11, 56));
        user.setModifyDate(LocalDateTime.of(2024, 7, 2, 10, 11, 56));

        String text = JSON.toJSONString(user);
        System.out.println(text);

        UserDTO result = JSON.parseObject(text, UserDTO.class);
        System.out.println(result);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getDeposit(), result.getDeposit());
        assertEquals("2024-07-01", result.getActDate().format(dtf));
    }
}
