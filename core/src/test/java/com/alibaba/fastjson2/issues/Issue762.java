package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue762 {
    @Test
    public void test() {
        TalkRecord taskRecord = new TalkRecord();
        taskRecord.setUpdateTime(new Date(1663269881438L));
        assertEquals("{\"updateTime\":\"2022-09-16 03:24:41.438\"}", JSON.toJSONString(taskRecord));
        assertEquals("\"2022-09-16 03:24:41.438\"", JSON.toJSONString(taskRecord.getUpdateTime()));
        assertEquals("\"2022-09-16 03:24:41.438\"", JSON.toJSONString(taskRecord.getUpdateTime(), JSONWriter.Feature.OptimizedForAscii));
    }

    public class TalkRecord {
        private Date talkTime;
        private Date createTime;
        private Date updateTime;

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        public Date getTalkTime() {
            return talkTime;
        }

        public void setTalkTime(Date talkTime) {
            this.talkTime = talkTime;
        }
    }
}
