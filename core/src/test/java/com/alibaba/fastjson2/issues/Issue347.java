package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Issue347 {
    @Test
    public void test() {
        DemoDto d0 = new DemoDto();
        d0.setVar1("d0");

        List<DemoDto> list = new ArrayList<>();
        list.add(d0);

        ValueFilter demoFilter = (object, name, value) -> value;
        System.out.println(JSON.toJSONString(list, demoFilter));
    }

    @Getter
    @Setter
    public static class DemoDto {
        private String var1;
        private Map<String, Object> var2;
    }
}
