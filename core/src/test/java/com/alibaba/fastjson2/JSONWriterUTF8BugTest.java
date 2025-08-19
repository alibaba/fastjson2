package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterUTF8BugTest {
    @Test
    public void testWriteStringWithOffset() {
        // 创建一个包含偏移量的字符数组
        // 数组内容: "___hello world" (前3个字符是偏移量)
        char[] chars = new char[100];
        // 填充偏移量字符
        for (int i = 0; i < 3; i++) {
            chars[i] = '_';
        }
        // 填充实际要写入的字符串 "hello world"
        String actualString = "hello world";
        for (int i = 0; i < actualString.length(); i++) {
            chars[i + 3] = actualString.charAt(i);
        }

        // 使用JSONWriterUTF8写入字符串，offset=3, len=11
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString(chars, 3, actualString.length(), true);

        // 验证结果应该是 "\"hello world\""
        String result = jsonWriter.toString();
        assertEquals("\"hello world\"", result);
    }

    @Test
    public void testWriteStringWithOffsetAndLongerString() {
        // 创建一个包含偏移量的较长字符数组
        // 数组内容: "___this is a longer test string for the bug" (前3个字符是偏移量)
        char[] chars = new char[200];
        // 填充偏移量字符
        for (int i = 0; i < 3; i++) {
            chars[i] = '_';
        }
        // 填充实际要写入的字符串
        String actualString = "this is a longer test string for the bug";
        for (int i = 0; i < actualString.length(); i++) {
            chars[i + 3] = actualString.charAt(i);
        }

        // 使用JSONWriterUTF8写入字符串，offset=3, len=实际字符串长度
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString(chars, 3, actualString.length(), true);

        // 验证结果应该是完整的字符串
        String result = jsonWriter.toString();
        assertEquals("\"" + actualString + "\"", result);
    }
}
