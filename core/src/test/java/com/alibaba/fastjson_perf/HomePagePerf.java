package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.homepage.GetHomePageResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class HomePagePerf {
    private String str;

    public HomePagePerf() throws Exception {
        InputStream is = HomePagePerf.class.getClassLoader().getResourceAsStream("data/homepage.json");
        str = IOUtils.toString(is, "UTF-8");
        is.close();
    }

    @Test
    public void test_homepage() {
        GetHomePageResponse resp = JSON.parseObject(str, GetHomePageResponse.class);
        String str2 = JSON.toJSONString(resp);
        String str2_pretty = JSON.toJSONString(resp);
        String str3 = JSON.toJSONString(resp, JSONWriter.Feature.BeanToArray);
        byte[] bytes = JSON.toJSONBytes(resp);
        byte[] bytes2 = JSON.toJSONBytes(resp, JSONWriter.Feature.BeanToArray);
        System.out.println(str2);

    }
}
