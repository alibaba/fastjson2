package com.alibaba.fastjson2_demo;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2_vo.Int2;

public class Int2Decoder {
    private static final long V0 = Fnv.hashCode64("v0000");
    private static final long V1 = Fnv.hashCode64("v0001");

    public void acceptField(JSONReader jsonReader, Int2 object, long hashCode) {
        if (hashCode == V0) {
            int fieldInt = jsonReader.readInt32Value();
            object.setV0000(fieldInt);
        } else if (hashCode == V0) {
            int fieldInt = jsonReader.readInt32Value();
            object.setV0000(fieldInt);
        }
    }
}
