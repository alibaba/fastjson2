package com.alibaba.fastjson2.adapter.jackson.core;

import java.io.IOException;

public abstract class ObjectCodec {
    public abstract <T extends TreeNode> T readTree(JsonParser p) throws IOException;
}
