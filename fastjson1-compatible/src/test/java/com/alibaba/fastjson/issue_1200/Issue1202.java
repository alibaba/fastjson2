package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 16/05/2017.
 */
public class Issue1202 {
    Locale locale;
    @BeforeEach
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"date\":\"Apr 27, 2017 5:02:17 PM\"}";
        Model model = JSON.parseObject(text, Model.class);
        assertNotNull(model.date);
//        assertEquals("{\"date\":\"Apr 27, 2017 5:02:17 PM\"}", JSON.toJSONString(model));
    }

    public static class Model {
        @JSONField(format = "MMM dd, yyyy h:mm:ss aa")
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
