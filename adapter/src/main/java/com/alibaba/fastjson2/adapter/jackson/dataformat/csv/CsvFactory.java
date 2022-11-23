package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;

public class CsvFactory
        extends JsonFactory {
    public JSONWriter createJSONWriter() {
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.BeanToArray);
        return JSONWriter.ofCSV(context);
    }

    public boolean isCSV() {
        return true;
    }
}
