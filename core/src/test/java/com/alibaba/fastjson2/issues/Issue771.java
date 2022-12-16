package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue771 {
    @Test
    public void test() {
        User startInfo = new User();
        startInfo.setStartTime(new Date());
        startInfo.setPageNo(1);
        startInfo.setPageSize(20);
        HashMap<String, Integer> map = new HashMap<>();
        map.put("111", 1);
        startInfo.setFinishMap(map);
        String s = JSON.toJSONString(startInfo, JSONWriter.Feature.WriteClassName);
        Object parse = JSON.parseObject(s, User.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(parse);
    }

    @Data
    public static class User {
        // 开始时间
        private Date startTime;
        // 当前页
        private Integer pageNo;
        // 每页大小
        private Integer pageSize;
        // 已完成结果
        private Map<String, Integer> finishMap;
    }

    @Test
    public void test1() {
        User1 startInfo = new User1();
        startInfo.setStartTime(new Date());
        startInfo.setPageNo(1);
        startInfo.setPageSize(20);
        HashMap<String, String> map = new HashMap<>();
        map.put("111", "1");
        startInfo.setFinishMap(map);
        String s = JSON.toJSONString(startInfo, JSONWriter.Feature.WriteClassName);
        Object parse = JSON.parseObject(s, User1.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(parse);
    }

    @Data
    public static class User1 {
        // 开始时间
        private Date startTime;
        // 当前页
        private Integer pageNo;
        // 每页大小
        private Integer pageSize;
        // 已完成结果
        private Map<String, String> finishMap;
    }
}
