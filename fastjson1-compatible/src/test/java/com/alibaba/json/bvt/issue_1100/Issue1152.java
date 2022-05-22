package com.alibaba.json.bvt.issue_1100;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 08/05/2017.
 */
public class Issue1152 {
    @Test
    public void test_for_issue() throws Exception {
        TestBean tb = JSONObject.parseObject("{shijian:\"0000-00-00T00:00:00\"}",TestBean.class);
        assertNull(tb.getShijian());
    }

    public void test_for_issue_2() throws Exception {
        TestBean tb = JSONObject.parseObject("{shijian:\"0001-01-01T00:00:00+08:00\"}",TestBean.class);
        assertNotNull(tb.getShijian());
    }

    public static class TestBean {
        private Date shijian;

        public Date getShijian() {
            return shijian;
        }
        @JSONField(name="shijian" )
        public void setShijian(Date shijian) {
            this.shijian = shijian;
        }
    }
}
