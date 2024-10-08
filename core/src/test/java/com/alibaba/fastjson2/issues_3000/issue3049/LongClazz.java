package com.alibaba.fastjson2.issues_3000.issue3049;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LongClazz {
    @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
    private Long d;
}
