package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1861 {
    @Test
    public void test() {
        String json = "{\"myData1\":{\"myData2\":{\"key\":{\"startTime\":null}}}}";
        MyData data = JSON.parseObject(json, MyData.class);
        assertEquals(json, JSON.toJSONString(data, JSONWriter.Feature.WriteNulls));
    }

    public class MyData {
        private MyData1 myData1;

        public MyData1 getMyData1() {
            return myData1;
        }

        public void setMyData1(MyData1 myData1) {
            this.myData1 = myData1;
        }
    }

    public class MyData1 {
        @JsonProperty("myData2")
        private @Valid Map<String, MyData2> myData2 = null;

        public Map<String, MyData2> getMyData2() {
            return myData2;
        }

        public void setMyData2(Map<String, MyData2> myData2) {
            this.myData2 = myData2;
        }
    }

    public class MyData2 {
        @JsonProperty("startTime")
        private OffsetDateTime startTime;

        public OffsetDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(OffsetDateTime startTime) {
            this.startTime = startTime;
        }
    }
}
