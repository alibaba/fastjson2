package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;

public class CsvFactory
        extends JsonFactory {
    public JSONWriter createJSONWriter() {
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.BeanToArray);
        return JSONWriter.ofCSV(context);
    }

    public JSONReader createJSONReader(String str) {
        JSONReader.Context context = JSONFactory.createReadContext(
                JSONReader.Feature.SupportArrayToBean
        );
        return JSONReader.ofCSV(str, context);
    }
}
