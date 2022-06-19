package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;

public interface ApacheLang3Support {
    @JSONType(typeName = "org.apache.commons.lang3.tuple.Pair")
    interface PairMixIn<L, R> {
        @JSONCreator
        static <L, R> Object of(L left, R right) {
            return null;
        }

        @JSONField(deserialize = false)
        Object setValue(Object value);
    }

    interface MutablePairMixIn<L, R> {
        @JSONCreator
        static <L, R> Object of(L left, R right) {
            return null;
        }

        Object getLeft();

        Object getRight();

        @JSONField(deserialize = false)
        Object setValue(Object value);
    }

    interface TripleMixIn<L, M, R> {
        @JSONCreator
        static <L, M, R> Object of(L left, M middle, R right) {
            return null;
        }
    }
}
