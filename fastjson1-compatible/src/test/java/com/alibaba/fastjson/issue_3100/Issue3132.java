package com.alibaba.fastjson.issue_3100;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.json.bvtVO.一个中文名字的包.User;
import org.junit.jupiter.api.Test;

public class Issue3132 {
    @Test
    public void test_for_issue() throws Exception {
        User user = new User();
        user.setId(9);
        user.setName("asdffsf");
        System.out.println(JSONObject.toJSONString(user));
    }
}
