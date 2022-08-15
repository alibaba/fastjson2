package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue638 {
    @Test
    public void test() {
        Clothing clothing = new Clothing();
        if (clothing.getTShirtType() == null) {
            clothing.setTShirtType(new ArrayList<>());
        }
        clothing.getTShirtType().add("Sweater Vest");
        assertEquals("{\"tShirtType\":[\"Sweater Vest\"]}", JSON.toJSONString(clothing));
    }

    @Getter
    @Setter
    public class Clothing {
        @JSONField(name = "tShirtType")
        private List<String> tShirtType;
    }
}
