package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

public interface PropertyPreFilter
        extends Filter {
    boolean process(JSONWriter writer, Object source, String name);
}
