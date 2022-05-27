package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue361 {
    @Test
    public void test0() {
        assertFalse(JSON.isEnabled(JSONReader.Feature.SupportSmartMatch));
        JSON.config(JSONReader.Feature.SupportSmartMatch);
        assertTrue(JSONFactory.createReadContext().isEnabled(JSONReader.Feature.SupportSmartMatch));
        assertTrue(JSON.isEnabled(JSONReader.Feature.SupportSmartMatch));
        JSON.config(JSONReader.Feature.SupportSmartMatch, false);
        assertFalse(JSONFactory.createReadContext().isEnabled(JSONReader.Feature.SupportSmartMatch));
        assertFalse(JSON.isEnabled(JSONReader.Feature.SupportSmartMatch));
        JSON.config(JSONReader.Feature.SupportSmartMatch, true);
        assertTrue(JSON.isEnabled(JSONReader.Feature.SupportSmartMatch));
        JSON.config(JSONReader.Feature.SupportSmartMatch, false);
        assertFalse(JSON.isEnabled(JSONReader.Feature.SupportSmartMatch));
    }

    @Test
    public void test1() {
        assertFalse(JSON.isEnabled(JSONWriter.Feature.MapSortField));
        JSON.config(JSONWriter.Feature.MapSortField);
        assertTrue(JSONFactory.createWriteContext().isEnabled(JSONWriter.Feature.MapSortField));
        assertTrue(JSON.isEnabled(JSONWriter.Feature.MapSortField));
        JSON.config(JSONWriter.Feature.MapSortField, false);
        assertFalse(JSONFactory.createWriteContext().isEnabled(JSONWriter.Feature.MapSortField));
        assertFalse(JSON.isEnabled(JSONWriter.Feature.MapSortField));
        JSON.config(JSONWriter.Feature.MapSortField, true);
        assertTrue(JSON.isEnabled(JSONWriter.Feature.MapSortField));
        JSON.config(JSONWriter.Feature.MapSortField, false);
        assertFalse(JSON.isEnabled(JSONWriter.Feature.MapSortField));
    }
}
