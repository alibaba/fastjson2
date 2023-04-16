package com.alibaba.fastjson2.v1issues.issue_4300;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4368 {
    @Test
    public void test() {
        Dto1 dto1 = new Dto1();
        List<Dto2> dto2List = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Dto2 dto2 = new Dto2();
            dto2.setIntegerList(Arrays.asList(1, 2, 3));
            dto2List.add(dto2);
        }
        dto1.setDto2List(dto2List);
        assertEquals("{\"dto2List\":[{\"integerList\":[1,2,3]},{\"integerList\":[1,2,3]}]}", JSON.toJSONString(dto1));
    }

    @Getter
    @Setter
    public class Dto1 {
        private List<Dto2> dto2List;
    }

    @Getter
    @Setter
    public class Dto2 {
        private List<Integer> integerList;
    }
}
