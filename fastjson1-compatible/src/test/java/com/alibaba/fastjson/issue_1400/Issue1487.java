package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1487 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model._id = 1001L;
        model.id = 1002L;

        String json = JSON.toJSONString(model);
        assertEquals("{\"_id\":1001,\"id\":1002}", json);
        Model model1 = JSON.parseObject(json, Model.class);
        assertEquals(json, JSON.toJSONString(model1));
    }

    public static class Model {
        private Long _id;
        private Long id;

        @JSONField(name = "_id")
        public long get_id() {
            if (null != _id) {
                return _id.longValue();
            } else {
                return 0L;
            }
        }

        @JSONField(name = "_id")
        public void set_id(Long _id) {
            this._id = _id;
        }

        public long getId() {
            if (null != id) {
                return id.longValue();
            } else {
                return 0L;
            }
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
