package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

/**
 * Created by wenshao on 11/04/2017.
 */
public class Issue1140 {
    @Test
    public void test_for_issue1() throws Exception {
        String s = "\uD83C\uDDEB\uD83C\uDDF7";

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JSON.writeJSONString(out, s, new SerializeFilter[0]);
    }

    @Test
    public void test_for_issue2() throws Exception {
        String s = "\uD83C\uDDEB\uD83C\uDDF7";

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JSON.writeJSONString(out, s, new SerializerFeature[0]);
    }
}
