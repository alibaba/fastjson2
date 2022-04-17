package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import junit.framework.TestCase;

import java.util.List;

/**
 * Created by wenshao on 09/05/2017.
 */
public class Issue1188 extends TestCase {
    public void test_for_issue_1188() throws Exception {
        String json = "{\"ids\":\"a1,a2\",\"name\":\"abc\"}";
        Info info = JSON.parseObject(json, Info.class);
        assertNull(info.ids);
    }

    public static class Info{

        @JSONField(read = false)
        private List<Integer> ids;
        private String name;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
