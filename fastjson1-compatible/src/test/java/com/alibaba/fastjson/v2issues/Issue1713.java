package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ContextNameFilter;
import com.alibaba.fastjson2.filter.ContextValueFilter;
import com.alibaba.fastjson2.filter.Filter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1713 {
    @Test
    public void test() {
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile("13012345678");
        userInfo.setUserId(123L);
        List<Filter> filters = new ArrayList<>();
        filters.add((ContextValueFilter) (context, object, name, value) -> {
            assertNotNull(context.getField());
            return value;
        });
        filters.add((ContextNameFilter) (context, object, name, value) -> {
            assertNotNull(context.getField());
            return name;
        });
        assertEquals(
                "{\"mobile\":\"13012345678\",\"userId\":123}",
                JSON.toJSONString(userInfo, filters.toArray(new Filter[0]))
        );
    }

    @Data
    public static class UserInfo{
        private String mobile;
        private long userId;
    }
}
