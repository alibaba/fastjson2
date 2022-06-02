package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue400 {
    @Test
    public void test() {
        CacheObject cacheObject = new CacheObject();
        cacheObject.setId("11");
        CacheObject cacheObject2 = new CacheObject();
        cacheObject2.setId("22");
        List<CacheObject> cacheObjects = Arrays.asList(cacheObject, cacheObject2);

        List<String> transform = com.google.common.collect.Lists.transform(cacheObjects, CacheObject::getId);

        CacheObject cacheObject3 = new CacheObject();
        cacheObject3.setListEmCache(Arrays.asList(EnumTest.EM1, EnumTest.EM2));
        cacheObject3.setStrlistCache(transform);

        byte[] bytes = JSONB.toBytes(cacheObject3, JSONWriter.Feature.WriteClassName);

        CacheObject jsonObject = (CacheObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.SupportClassForName);

        List<String> strlistCache = jsonObject.getStrlistCache();
        assertEquals(2, strlistCache.size());
        assertEquals("11", strlistCache.get(0));
        assertEquals("22", strlistCache.get(1));
    }

    public static class CacheObject {
        private String id;

        private long version;
        private List<Object> listCache;
        private List<String> strlistCache;

        private List<EnumTest> listEmCache;

        public List<Object> getListCache() {
            return listCache;
        }

        public void setListCache(List<Object> listCache) {
            this.listCache = listCache;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the version
         */
        public long getVersion() {
            return version;
        }

        /**
         * @param version the version to set
         */
        public void setVersion(long version) {
            this.version = version;
        }

        public List<EnumTest> getListEmCache() {
            return listEmCache;
        }

        public void setListEmCache(List<EnumTest> listEmCache) {
            this.listEmCache = listEmCache;
        }

        public List<String> getStrlistCache() {
            return strlistCache;
        }

        public void setStrlistCache(List<String> strlistCache) {
            this.strlistCache = strlistCache;
        }
    }

    public static enum EnumTest{
        EM1, EM2
    }
}
