package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.core.ObjectCodec;
import com.alibaba.fastjson2.adapter.jackson.core.TreeNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.TreeNodeUtils;

import java.io.IOException;

public class ObjectCodecWrapper
        extends ObjectCodec {
    public ObjectCodecWrapper() {
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        JSONReader jsonReader = p.getJSONReader();
        Object any = jsonReader.readAny();
        return (T) TreeNodeUtils.as(any);
    }
}
