package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1517 {
    @Test
    public void test() {
        Map map = new HashMap<>();
        map.put("id", 123123);

        List artist = new ArrayList();
        Map art = new HashMap();
        art.put("key", "2345");
        art.put("name", "luger");
        artist.add(art);
        map.put("artist", artist);

        Bean bean = JSONObject.from(map).toJavaObject(Bean.class);
        assertEquals(123123, bean.id);
    }

    @Test
    public void testWithStringList() {
        Map map = new HashMap<>();
        map.put("id", 123123);

        List artist = new ArrayList();
        Map art = new HashMap();
        art.put("key", "2345");
        art.put("name", "luger");
        artist.add(art);
        // artist 对应的是个数组格式的字符串
        map.put("artist", JSONObject.toJSONString(artist));

        Bean bean = JSONObject.from(map).toJavaObject(Bean.class);
        // 应该对比list是否转换成功
        assertEquals("luger", bean.list.get(0).name);
    }

    private static class Bean {
        private long id;
        @JSONField(alternateNames = "artist")
        private List<Artist> list;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public List<Artist> getList() {
            return list;
        }

        public void setList(List<Artist> list) {
            this.list = list;
        }
    }

    public static class Artist {
        public String key;
        public String name;
    }
}
