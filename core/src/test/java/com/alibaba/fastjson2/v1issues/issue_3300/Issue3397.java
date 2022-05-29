package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author ：Nanqi
 * @Date ：Created in 16:32 2020/8/16
 */
public class Issue3397 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"date\":\"2020-08-16 16:35:18.188\"}";
        VO vo = JSON.parseObject(text, VO.class);

        JSONObject json = (JSONObject) JSON.toJSON(vo);

        Date date = json.getDate("date");
//        assertEquals("Sun Aug 16 16:35:18 CST 2020", date.toString());
    }

    public static class VO {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
        private LocalDateTime date;

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }
    }
}
