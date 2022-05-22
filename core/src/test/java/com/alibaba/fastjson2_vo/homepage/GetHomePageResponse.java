package com.alibaba.fastjson2_vo.homepage;

import com.alibaba.fastjson2.annotation.JSONField;

public class GetHomePageResponse {
    public String api;
    @JSONField(name = "v")
    public String version;
    public String ret;

    public GetHomePageData data;
}
