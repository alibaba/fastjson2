package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1505 {
    @Test
    public void test() {
        ResultDTO resultDTO = new ResultDTO(false, 300002, "非法参数");
        byte[] bytes = JSONB.toBytes(resultDTO, JSONWriter.Feature.NotWriteDefaultValue);
        assertEquals(
                "{\n" +
                        "\t\"code\":300002,\n" +
                        "\t\"msg\":\"非法参数\"\n" +
                        "}",
                JSONB.toJSONString(bytes)
        );
        ResultDTO<CardCouponDTO> deserialzedResultDTO = JSONB.parseObject(bytes, resultDTO.getClass());
        assertEquals(resultDTO.isSuccess(), deserialzedResultDTO.isSuccess());
    }

    public static class CardCouponDTO {
    }

    @Getter
    @Setter
    public static class ResultDTO<T>
            implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean success = true;
        private int code;
        private String msg;

        public ResultDTO(boolean success, int code, String msg) {
            this.success = success;
            this.code = code;
            this.msg = msg;
        }
    }
}
