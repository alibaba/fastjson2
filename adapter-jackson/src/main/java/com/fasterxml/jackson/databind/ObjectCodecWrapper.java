package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.TreeNodeUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class ObjectCodecWrapper extends ObjectCodec {

    public ObjectCodecWrapper() {
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        JSONReader jsonReader = p.getRaw();
        Object any = jsonReader.readAny();
        return (T) TreeNodeUtils.as(any);
    }
}
