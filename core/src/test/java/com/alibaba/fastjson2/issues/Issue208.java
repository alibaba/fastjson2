package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class Issue208 {
    @Test
    public void testMemberMetadata() {
        String json = "{\n" +
                "                \t\"code\": \"项目成员1\",\n" +
                "                \t\"formatType\": 0,\n" +
                "                \t\"id\": \"a368f1ff-8702-4d80-9cdf-5260b5fe0fdd\",\n" +
                "                \t\"level\": 0,\n" +
                "                }";

        MemberMetadata source = new MemberMetadata();
        source.setCode("项目成员1");
        source.setFormatType((byte) 0);
        source.setId(UUID.fromString("a368f1ff-8702-4d80-9cdf-5260b5fe0fdd"));
        source.setLevel(0);
        String jsonString = JSON.toJSONString(source);
        System.out.println(jsonString);

        MemberMetadata memberMetadata = JSON.parseObject(json, MemberMetadata.class);
    }

    public class MemberMetadata {
        private String code;
        private byte formatType;
        //------这里是重点，不能使用id作为名称------
        private UUID id;
        private int level;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public byte getFormatType() {
            return formatType;
        }

        public void setFormatType(byte formatType) {
            this.formatType = formatType;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }
}
