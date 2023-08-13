package com.alibaba.fastjson2.issues_1700;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1711 {
    @Test
    public void test() {
        Map map = new HashMap();
        map.put("now", "00000000");
        // Hutool BeanUtil
        final MyBean bean = BeanUtil.mapToBean(map, MyBean.class, false, null);

        assertEquals(
                JSON.toJSONString(bean),
                new String(JSON.toJSONBytes(bean))
        );

        System.out.println(JSON.toJSONString(bean));
    }

    public static class MyBean {
        private Date now;

        public Date getNow() {
            return now;
        }

        public void setNow(Date now) {
            this.now = now;
        }

        @Override
        public String toString() {
            return "MyBean{" +
                    "now=" + now +
                    '}';
        }
    }
}
