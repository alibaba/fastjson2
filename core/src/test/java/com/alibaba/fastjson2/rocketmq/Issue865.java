package com.alibaba.fastjson2.rocketmq;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue865 {
    @Test
    public void test() {
        RemotingCommand cmd = new RemotingCommand();
        String str = JSON.toJSONString(cmd);
        RemotingCommand cmd1 = JSON.parseObject(str, RemotingCommand.class);
        assertEquals(cmd.code, cmd1.code);
    }

    public static class RemotingCommand {
        protected RemotingCommand() {
        }

        private int code;
        private LanguageCode language = LanguageCode.JAVA;
        private int version;
        private int flag;
        private String remark;
        private HashMap<String, String> extFields;
        private transient CommandCustomHeader customHeader;
        private SerializeType serializeTypeCurrentRPC;
        private transient byte[] body;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public LanguageCode getLanguage() {
            return language;
        }

        public void setLanguage(LanguageCode language) {
            this.language = language;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public HashMap<String, String> getExtFields() {
            return extFields;
        }

        public void setExtFields(HashMap<String, String> extFields) {
            this.extFields = extFields;
        }

        public CommandCustomHeader getCustomHeader() {
            return customHeader;
        }

        public void setCustomHeader(CommandCustomHeader customHeader) {
            this.customHeader = customHeader;
        }

        public SerializeType getSerializeTypeCurrentRPC() {
            return serializeTypeCurrentRPC;
        }

        public void setSerializeTypeCurrentRPC(SerializeType serializeTypeCurrentRPC) {
            this.serializeTypeCurrentRPC = serializeTypeCurrentRPC;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }
    }

    public static class CommandCustomHeader {
    }

    public static class SerializeType {
    }

    public enum LanguageCode {
        JAVA
    }
}
