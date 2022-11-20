package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;

public abstract class Module {
    public ObjectWriterModule getWriterModule() {
        return null;
    }

    public ObjectReaderModule getReaderModule() {
        return null;
    }
}
