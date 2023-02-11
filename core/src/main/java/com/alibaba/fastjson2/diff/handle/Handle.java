package com.alibaba.fastjson2.diff.handle;

import com.alibaba.fastjson2.diff.path.JsonCompareResult;

public interface Handle {
    JsonCompareResult handle();

    JsonCompareResult getResult();
}
