package com.alibaba.fastjson2.support.spring.websocket.sockjs;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

import java.io.InputStream;

public class FastjsonSockJsMessageCodec
        extends AbstractSockJsMessageCodec {
    private FastJsonConfig config = new FastJsonConfig();

    public FastJsonConfig getFastJsonConfig() {
        return config;
    }

    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.config = fastJsonConfig;
    }

    @Override
    public String[] decode(String content) {
        return JSON.parseObject(content, String[].class, config.getReaderFeatures());
    }

    @Override
    public String[] decodeInputStream(InputStream content) {
        return JSON.parseObject(content, String[].class, config.getReaderFeatures());
    }

    @Override
    protected char[] applyJsonQuoting(String content) {
        return content.toCharArray();
    }

    @Override
    public String encode(String... messages) {
        JSONWriter jsonWriter = JSONWriter.of(config.getWriterFeatures());
        if (jsonWriter.utf8) {
            jsonWriter.writeRaw(new byte[]{'a'});
        } else {
            jsonWriter.writeRaw(new char[]{'a'});
        }
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
