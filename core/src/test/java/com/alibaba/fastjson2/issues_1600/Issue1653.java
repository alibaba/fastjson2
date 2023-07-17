package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

public class Issue1653 {
    @Test
    public void test() {
        JSON.configReaderZoneId(null);
        JSON.configWriterZoneId(null);
    }
}
