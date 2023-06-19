package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1410 {
    @Test
    public void testList() {
        String jsonArray = "[{\"dictId\":\"CN\",\"dictName\":\"China\",\"dictTypeId\":\"country\",\"level\":1,\"rank\":1,\"seqno\":\".CN.\",\"sortno\":1,\"status\":1,\"children\":[{\"dictId\":\"1\",\"dictName\":\"Jiangsu\",\"dictTypeId\":\"province\",\"level\":1,\"parentId\":\"CN\",\"rank\":2,\"seqno\":\".CN.1.\",\"sortno\":1,\"status\":1,\"children\":[{\"dictId\":\"101\",\"dictName\":\"Nanjing\",\"dictTypeId\":\"city\",\"level\":1,\"parentId\":\"1\",\"rank\":3,\"seqno\":\".CN.1.101.\",\"sortno\":1,\"status\":1},{\"dictId\":\"102\",\"dictName\":\"Changzhou\",\"dictTypeId\":\"city\",\"level\":1,\"parentId\":\"1\",\"rank\":3,\"seqno\":\".CN.1.102.\",\"sortno\":2,\"status\":1}]},{\"dictId\":\"2\",\"dictName\":\"Guangdong\",\"dictTypeId\":\"province\",\"level\":1,\"parentId\":\"CN\",\"rank\":2,\"seqno\":\".CN.2.\",\"sortno\":2,\"status\":1,\"children\":[{\"dictId\":\"201\",\"dictName\":\"Guangzhou\",\"dictTypeId\":\"city\",\"level\":1,\"parentId\":\"2\",\"rank\":3,\"seqno\":\".CN.2.201.\",\"sortno\":1,\"status\":1}]}]}]";
        String expected = "[{\"dictId\":\"101\",\"dictName\":\"Nanjing\",\"dictTypeId\":\"city\",\"level\":1,\"parentId\":\"1\",\"rank\":3,\"seqno\":\".CN.1.101.\",\"sortno\":1,\"status\":1}]";
        String path = "$..[?( @.dictTypeId =='city' && @.dictId =='101' )]";
        assertEquals(expected, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$.[?( @.dictTypeId =='city' && @.dictId =='101' )]";
        assertEquals("null", JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
    }
}
