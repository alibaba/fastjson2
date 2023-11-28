package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue586 {
    @Test
    public void test() throws JSONException {
        Bean bean = JSON.parseObject("{\"resourceIds\":Set[1L,100L,1000L,1001L,1002L,1003L,1004L,1005L,1006L]}", Bean.class);
        assertEquals(9, bean.resourceIds.size());
        JSONAssert.assertEquals("[1,100,1000,1001,1002,1003,1004,1005,1006]", JSON.toJSONString(bean.resourceIds), false);
    }

    public static class Bean {
        private Set<Long> resourceIds;

        public Set<Long> getResourceIds() {
            return resourceIds;
        }

        public void setResourceIds(Set<Long> resourceIds) {
            this.resourceIds = resourceIds;
        }
    }
}
