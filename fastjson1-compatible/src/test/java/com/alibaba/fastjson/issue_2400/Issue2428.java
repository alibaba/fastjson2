package com.alibaba.fastjson.issue_2400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
        String text = JSON.toJSONString(JSON.toJSON(demoBean), SerializerFeature.SortField);
        assertEquals("{\"my_name\":\"test name\",\"nested_bean\":{\"my_id\":\"test id\"}}", text);
    }
}
