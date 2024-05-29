package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2641 {
    @Test
    public void test() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        {
            Bean bean = new Bean(new LinkedHashSet<>());
            String jackson = objectMapper.writeValueAsString(bean);
            assertEquals(jackson, JSON.toJSONString(bean));
        }

        {
            LinkedHashSet<String> groups = new LinkedHashSet<>();
            groups.add("abc");
            Bean bean = new Bean(groups);
            String jackson = objectMapper.writeValueAsString(bean);
            assertEquals(jackson, JSON.toJSONString(bean));
        }
    }

    public static class Bean {
        private final Set<String> groups;

        public Bean(Set<String> groups) {
            this.groups = groups;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Set<String> getGroups() {
            return groups;
        }
    }
}
