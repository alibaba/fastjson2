package com.alibaba.fastjson2.support.spring;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

import java.io.InputStream;

public class FastjsonSockJsMessageCodec extends AbstractSockJsMessageCodec {

    public String[] decode(String content) {
        return JSON.parseObject(content, String[].class);
    }

    public String[] decodeInputStream(InputStream content) {
        return JSON.parseObject(content, String[].class);
    }

    @Override
    protected char[] applyJsonQuoting(String content) {
        return content.toCharArray();
    }

    @Override
    public String encode(String... messages) {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeRaw(new char[] {'a'});
        jsonWriter.startArray();
        for (int i = 0; i < messages.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            String message = messages[i];
            jsonWriter.writeString(message);
        }
        jsonWriter.endArray();
        return jsonWriter.toString();
    }

}
