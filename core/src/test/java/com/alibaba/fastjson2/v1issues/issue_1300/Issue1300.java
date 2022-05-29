package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 01/07/2017.
 */
public class Issue1300 {
    @Test
    public void testFullJSON() {
        JSONObject data = new JSONObject();
        data.put("name", "string");
        data.put("code", 1);
        data.put("pinyin", "pinyin");
        City object = TypeUtils.cast(data, City.class);
        assertEquals("string", object.name);
        assertEquals(1, object.code);
        assertEquals("pinyin", object.pinyin);
    }

    @Test
    public void testEmptyJSON() {
        City object = TypeUtils.cast(new JSONObject(), City.class);
        assertNull(object.name);
        assertEquals(0, object.code);
    }

    public static class City
            implements Parcelable {
        public final int code;
        public final String name;
        public final String pinyin;

        @JSONCreator
        public City(@JSONField(name = "code") int code,
                    @JSONField(name = "name") String name,
                    @JSONField(name = "pinyin") String pinyin) {
            this.code = code;
            this.name = name;
            this.pinyin = pinyin;
        }
    }

    public static interface Parcelable {
    }
}
