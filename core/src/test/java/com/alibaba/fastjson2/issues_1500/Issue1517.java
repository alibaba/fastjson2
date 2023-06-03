package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSONObject;
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

    private static class Bean {
        private long id;
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
