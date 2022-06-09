package com.alibaba.fastjson2.v1issues.issue_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NoArgsConstructor
@Data
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class Issue2428 {
    private String myName;
    private NestedBean nestedBean;

    @AllArgsConstructor
    @Data
    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class NestedBean {
        private String myId;
    }

    @Test
    public void test_for_issue() {
        Issue2428 demoBean = new Issue2428();
        demoBean.setMyName("test name");
        demoBean.setNestedBean(new NestedBean("test id"));
        String text = JSON.toJSONString(JSON.toJSON(demoBean));
        assertEquals("{\"my_name\":\"test name\",\"nested_bean\":{\"my_id\":\"test id\"}}", text);
    }
}
