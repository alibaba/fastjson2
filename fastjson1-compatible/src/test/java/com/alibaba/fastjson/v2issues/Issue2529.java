package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class Issue2529 {
    @Test
    public void test() {
        final String format = JSON.DEFAULT_DATE_FORMAT;
        try {
            JSONObject.DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
            ResultVo rv = ResultVo.sucessReturn("测试Date转换");
            String str = JSONObject.toJSONString(rv);
            ResultVo rvnew = JSON.parseObject(str, ResultVo.class);
            System.out.println(JSONObject.toJSONString(rvnew));
        } finally {
            JSON.DEFAULT_DATE_FORMAT = format;
        }
    }

    public static class ResultVo {
        public String code;
        public String msg;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date requesttime;
        /**
         * 成功返回1,及消息
         *
         * @param retmsg
         * @return
         */
        public static ResultVo sucessReturn(String retmsg) {
            ResultVo rv = new ResultVo();
            rv.code = "200";
            rv.msg = retmsg;
            rv.requesttime = new Date();
            return rv;
        }
    }
}
