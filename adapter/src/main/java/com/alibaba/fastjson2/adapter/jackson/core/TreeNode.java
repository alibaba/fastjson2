package com.alibaba.fastjson2.adapter.jackson.core;

public interface TreeNode {
    TreeNode get(String fieldName);

    TreeNode get(int index);

    JsonParser traverse(ObjectCodec codec);

    boolean isMissingNode();
}
