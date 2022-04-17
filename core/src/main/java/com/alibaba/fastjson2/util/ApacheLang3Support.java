package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;

import java.util.function.Function;
import java.util.function.Supplier;

public class ApacheLang3Support {
    @JSONType(typeName = "org.apache.commons.lang3.tuple.Pair")
    public interface PairMixIn<L, R> {
        @JSONCreator
        static <L, R> Object of(L left, R right) {
            return null;
        }

        @JSONField(read = false)
        Object setValue(Object value);
    }

    public interface MutablePairMixIn<L, R> {
        @JSONCreator
        static <L, R> Object of(L left, R right) {
            return null;
        }

        Object getLeft();
        Object getRight();

        @JSONField(read = false)
        Object setValue(Object value);
    }

    public interface TripleMixIn<L, M, R> {
        @JSONCreator
        static <L, M, R> Object of(L left, M middle, R right) {
            return null;
        }
    }
}
