package com.alibaba.fastjson.issue_1700;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1725 {
    @Test
    public void test_for_issue() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("enumField", 0);

        AbstractBean bean = JSON.parseObject(JSON.toJSONString(map), ConcreteBean.class);
        assertEquals(FieldEnum.A, bean.enumField);
    }

    public static class AbstractBean {
        public FieldEnum enumField;
    }

    public static class ConcreteBean
            extends AbstractBean {
    }

    public static enum FieldEnum {A, B}
}
