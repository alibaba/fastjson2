package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;

public class CsvFactory
        extends JsonFactory {
    public boolean isCSV() {
        return true;
    }
}
