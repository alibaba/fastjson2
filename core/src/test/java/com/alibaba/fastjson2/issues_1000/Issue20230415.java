package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

public class Issue20230415 {
    @Test
    public void test() {
        String content = "{\"status\":\"200\",\"result\":null,\"data\":null,\"extra\":null,\"msg\":\"success\",\"res\":0}";
        for (int i = 0; i < 1000; i++) {
            JSONObject.parseObject(content, RequestMessage.class);
        }
    }

    public enum RequestStatusEnums {
        /**
         * "200  正确
         */
        Right("200", "正确"),
        /**
         * 300  错误
         */
        Error("300", "错误"),
        /**
         * 400 其他 错误
         */
        Other("400", "其他 错误");

        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        RequestStatusEnums(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static RequestStatusEnums getByKey(String key) {
            if (null == key) {
                return null;
            }
            for (RequestStatusEnums cenum : values()) {
                if (cenum.getKey().equals(key)) {
                    return cenum;
                }
            }
            return null;
        }
    }

    public static class RequestMessage {
        public RequestMessage() {}

        public RequestMessage(String status) {
            this.setStatus(status);
        }

        public RequestMessage(RequestStatusEnums statusEnums) {
            this.setStatus(statusEnums.getKey());
        }

        public RequestMessage message(MessageEnums messageEnums) {
            this.setMsg(messageEnums.getMsgtext());
            return this;
        }
        public RequestMessage message(String message) {
            this.setMsg(message);
            return this;
        }
        public RequestMessage result(Object result) {
            this.setResult(result);
            return this;
        }
        public RequestMessage data(Object data) {
            this.setData(data);
            return this;
        }

        /**
         * 请求状态
         */
        private String status = "300";

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        /*
         * 返回结果
         */
        private Object result;

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        /**
         * 放置必须返回为data的字段
         */
        private Object data;

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        /**
         * 附加信息
         */
        private Object extra;

        public Object getExtra() {
            return extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        /**
         * 消息
         */
        private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void setMsg(MessageEnums enums) {
            this.msg = enums.getMsgtext();
        }

        private int res;

        public int getRes() {
            return res;
        }

        public void setRes(int res) {
            this.res = res;
        }
    }

    public enum MessageEnums {
        SAVE_SUCCESS("S_0001", "保存成功！"),
        SAVE_SUCCESS_2("S_0002", "{0}保存成功！"),
        DELETE_SUCCESS("S_1001", "删除成功！"),
        LOGIN_SUCCESS("S_2001", "登录成功！"),
        QUERY_SUCCESS("S_3001", "查询成功！"),
        SAVE_FAILURE("E_0000", "保存失败！"),
        MODEL_IS_NULL("E_0001", "参数异常！"),
        DATA_NOT_EXIST("E_0002", "该{0}不存在！"),
        DATA_ERROR("E_0003", "参数错误：{0}"),
        DATA_IS_NULL("E_0004", "该{0}不能为空！"),
        DATA_OTHER_ERROR("E_0005", "{0}"),
        DELETE_FAILURE("E_1000", "删除失败！"),
        DELETE_USED_DATA("E_0101", "该{0}已被{1}使用，不可删除！"),
        DELETE_DATA_HAS_RELATION("E_0102", "该{0}已有{1}，不可删除！"),

        LOGIN_FAILURE("E_2000", "登录失败！");

        private String code;
        private String msgtext;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsgtext() {
            return msgtext;
        }

        public void setMsgtext(String msgtext) {
            this.msgtext = msgtext;
        }

        MessageEnums(String code, String msgtext) {
            this.code = code;
            this.msgtext = msgtext;
        }

        public static MessageEnums getByCode(String code) {
            if (null == code) {
                return null;
            }
            for (MessageEnums cenum : values()) {
                if (cenum.getCode().equals(code)) {
                    return cenum;
                }
            }
            return null;
        }
    }
}
