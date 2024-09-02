package com.alibaba.fastjson2.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 继承字段忽略
 */
public class InheritMethodWriterTest {

    @Test
    public void writer() {
        UrlEntity entity = new UrlEntity("https://www.baidu.com/web/user");
        entity.setName("rose");
        String text = JSON.toJSONString(entity);
        assertEquals(text, "{\"name\":\"rose\"}");
    }

    @Test
    public void read() {
        String text = "{\"name\":\"rose\", \"url\":\"https://www.baidu.com/web/user\"}";
        UrlEntity entity = JSON.parseObject(text,UrlEntity.class);
        System.out.println(entity);
        assertEquals(entity.getName(), "rose");
        assertEquals(entity.getUrl(), "https://www.baidu.com/web/user?name=rose");
    }
    
    public static abstract class AbstractJsonEntity {
        @JSONField(serialize = false)
        private String url;

        public AbstractJsonEntity(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public class UrlEntity extends AbstractJsonEntity {

        private String name;

        public UrlEntity(String url) {
            super(url);
        }

        // @JSONField(serialize = false)
        @Override
        public String getUrl() {
            String text = JSON.toJSONString(this);
            JSONObject queryObj = JSONObject.parseObject(text);

            StringBuffer query = new StringBuffer();
            Set<String> keys = queryObj.keySet();
            for (String key : keys) {
                String value = queryObj.getString(key);
                if (StringUtils.isBlank(value)) {
                    continue;
                }

                String encodeKey = "";
                String encodeValue = "";
                try {
                    encodeKey = URLEncoder.encode(key, "UTF-8");
                    encodeValue = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (StringUtils.isBlank(encodeValue)) {
                    continue;
                }

                if (StringUtils.isBlank(query)) {
                    query.append("?");
                } else {
                    query.append("&");
                }
                query.append(encodeKey).append("=").append(encodeValue);
            }

            if (StringUtils.isNotBlank(query)) {
                return super.getUrl() + query;
            }

            return super.getUrl();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "UrlEntity [name=" + name + ", url=" + getUrl() + "]";
        }
    }
}
