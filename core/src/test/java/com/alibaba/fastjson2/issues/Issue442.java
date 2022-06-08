package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue442 {
    @Test
    public void test() {
        String json =
                "[\n"
                        + "   {\n"
                        + "      \"DepartureState\" : \"1\",\n"
                        + "      \"Realtime\" : [\n"
                        + "         {\n"
                        + "            \"ArriveStaName\" : \"aaaa\",\n"
                        + "         }\n"
                        + "      ]"
                        + "   }\n"
                        + "]";

        List<P> result = JSON.parseArray(json, P.class);
        assertNotNull(result.get(0).getList().get(0));
        assertNotNull(result.get(0).getList().get(0).getArriveStaName());
    }

    @Data
    public class P {
        @JSONField(name = "DepartureState")
        private String departureState;
        @JSONField(name = "Realtime")
        private List<Realtime> list;
    }

    @Data
    public class Realtime {
        @JSONField(name = "ArriveStaName")
        private String arriveStaName;
    }
}
