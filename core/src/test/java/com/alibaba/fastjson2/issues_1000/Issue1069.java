package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1069 {
    @Test
    public void test() {
        UserAttrActionInfo bean = new UserAttrActionInfo();
        Map<Integer, List<Long>> map = new LinkedHashMap<>();
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list.add(4L);
        map.put(1234, list);
        bean.tagInfos = map;

        //序列化然后再反序列化会出错，但类似的代码在fastjson1上运行正常
        String strJson = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        System.out.println("strJson=" + strJson);
        UserAttrActionInfo bean1 = JSON.parseObject(strJson, UserAttrActionInfo.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(bean1);
        assertNotNull(bean1.tagInfos);
        assertEquals(bean.tagInfos.getClass(), bean1.tagInfos.getClass());
    }

    private static class UserAttrActionInfo {
        public Map<Integer, List<Long>> tagInfos;
        public UserAttrActionInfo() {}
    }
}
