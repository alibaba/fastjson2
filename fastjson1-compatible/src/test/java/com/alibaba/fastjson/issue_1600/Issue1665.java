package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1665 {
    @Test
    public void test_for_issue() throws Exception {
        TypeReference<Collection<Model>> typeReference = new TypeReference<Collection<Model>>() {
        };

        Collection<Model> collection = TypeUtils.cast(JSON.parse("[{\"id\":101}]"), typeReference.getType(), ParserConfig.getGlobalInstance());
        assertEquals(1, collection.size());
        Model model = collection.iterator().next();
        assertEquals(101, model.id);
    }

    public static class Model {
        public int id;
    }
}
