package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;

public class Issue2025 {
    @Test
    public void test() {
        QueryWrapper qw = new QueryWrapper<>();
        qw.select("*");
        qw.eq("sys_id", 1712740060383170562L);
        qw.ne("ceshi", 1);
        qw.in("wode", 3, 4);
        qw.orderByAsc("time");
        qw.groupBy("group");
        qw.having("SUM({0}) AS nums", "group");

        String json = JSON.toJSONString(qw, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);
        //qw.set("full_name", "test111");
        System.out.println(json);
        //new TypeReference<QueryWrapper>() {}.getType()
        QueryWrapper query = JSON.parseObject(json, QueryWrapper.class, JSONReader.Feature.FieldBased);
    }
}
