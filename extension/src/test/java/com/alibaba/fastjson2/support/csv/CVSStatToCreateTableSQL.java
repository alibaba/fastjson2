package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.File;

public class CVSStatToCreateTableSQL {
    @Test
    public void test() throws Exception {
        File file = new File("/Users/wenshao/Downloads/AH_Provisional_COVID-19_Deaths_by_Race_and_Educational_Attainment.csv");
        if (!file.exists()) {
            return;
        }

        String ddl = CSVMaxComputeUtls.genCreateTable(file, "x5");
        System.out.println(ddl);
    }
}
