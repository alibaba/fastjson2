package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.support.arrow.CSVUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

public class CVSStatToCreateTableSQL {
    @Test
    public void test() throws Exception {
        File file = new File("/Users/wenshao/Downloads/COVID-19_Public_Therapeutic_Locator.csv");
        if (!file.exists()) {
            return;
        }

        String ddl = CSVUtils.genMaxComputeCreateTable(file, "x5");
        System.out.println(ddl);
    }
}
