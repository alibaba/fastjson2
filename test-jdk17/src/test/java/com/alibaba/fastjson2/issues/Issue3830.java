package com.alibaba.fastjson2.issues;

import java.util.Date;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import lombok.Data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * #3830 测试用例<br>
 * V2.0.59 当对象的字段是复杂对象且值为空时，WriteNulls会导致NullPointerException
 *
 * @author zhaohuihua
 * @version 20251018
 */
public class Issue3830 {

    @Test
    public void testRecordFieldOrder() {

        User user = new User();
        user.setId("U00001");
        user.setRealName("Foo");
        user.setCreateTime(new Date());
        JSONObject json = JSONObject.from(user, JSONWriter.Feature.WriteNulls);

        Object realName = json.get("realName");
        assertEquals("Foo", realName);

        Object remark = json.get("remark");
        assertNull(remark);

        Object address = json.get("address");
        assertNull(address);
    }

    @Data
    protected static class User {
        private String id;
        private String realName;
        private String remark;
        private Address address;
        private Date createTime;
    }

    @Data
    protected static class Address {
        private String id;
        private String city;
    }
}
