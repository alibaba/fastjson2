package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1451 {
    @Test
    public void test() {
        String value = "plJzdGFja2FibGWmT2NvdXBvbrFTcGF5VGltZW91dKZNemVyb7BQc2Vjb25kc+BRbmVnYXRpdmWwTW5hbm9IAcnDgE51bml0c5ZQU0VDT05EU05OQU5PU05jbGFzc1tqYXZhLnRpbWUuRHVyYXRpb26lTHZpcLFOY2xhc3N4dG9wLnJvd3MuZHViYm8udGVzdC5hcGkubW9kZWwuU3RhY2thYmxlRGlzY291bnRNZnVsbLGlTmV4dHJhklNKU09OT2JqZWN0AKZPdGVhbU5vWVRNMjAyMTA1MDgxMDAwMDGlTmNsYXNzc3RvcC5yb3dzLmR1YmJvLnRlc3QuYXBpLm1vZGVsLkFjdGl2aXR5UmVzcFdza3VLZXlQcmljZU1hcKamUnByb2R1Y3RJZOJPc2hvcElk4k5jbGFzc3h0b3Aucm93cy5kdWJiby50ZXN0LmFwaS5tb2RlbC5TaG9wUHJvZHVjdFNrdUtleU5za3VJZOKl0GSlpQ==";
        byte[] bytes = Base64.getDecoder().decode(value);
        HashMap objects = (HashMap) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );
        assertEquals(4, objects.size());
        assertEquals("top.rows.dubbo.test.api.model.ActivityResp", objects.get("class"));
    }

    @Test
    public void testJSONB2() {
        String value = "plJzdGFja2FibGWmT2NvdXBvbrFTcGF5VGltZW91dKZNemVyb7BQc2Vjb25kc+BRbmVnYXRpdmWwTW5hbm9IAcnDgE51bml0c5ZQU0VDT05EU05OQU5PU05jbGFzc1tqYXZhLnRpbWUuRHVyYXRpb26lTHZpcLFOY2xhc3N4dG9wLnJvd3MuZHViYm8udGVzdC5hcGkubW9kZWwuU3RhY2thYmxlRGlzY291bnRNZnVsbLGlTmV4dHJhklNKU09OT2JqZWN0AKZPdGVhbU5vWVRNMjAyMTA1MDgxMDAwMDGlTmNsYXNzc3RvcC5yb3dzLmR1YmJvLnRlc3QuYXBpLm1vZGVsLkFjdGl2aXR5UmVzcFdza3VLZXlQcmljZU1hcKamUnByb2R1Y3RJZOJPc2hvcElk4k5jbGFzc3h0b3Aucm93cy5kdWJiby50ZXN0LmFwaS5tb2RlbC5TaG9wUHJvZHVjdFNrdUtleU5za3VJZOKl0GSlpQ==";
        byte[] bytes = Base64.getDecoder().decode(value);
        HashMap objects = (HashMap) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );
        assertEquals(4, objects.size());
    }
}
