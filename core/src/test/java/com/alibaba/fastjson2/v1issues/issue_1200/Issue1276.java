package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 18/06/2017.
 */
public class Issue1276 {
    @Test
    public void test_for_issue() throws Exception {
        MyException e = new MyException(100, "error msg");
        String str = JSON.toJSONString(e);

        MyException e1 = JSON.parseObject(str, MyException.class);
        assertEquals(e.getCode(), e1.getCode());
        assertEquals(e.getMessage(), e1.getMessage());
    }

    public static class MyException
            extends RuntimeException {
        private static final long serialVersionUID = 7815426752583648734L;
        private long code;

        public MyException() {
            super();
            this.code = 0;
        }

        public MyException(String message, Throwable cause) {
            super(message, cause);
            this.code = 0;
        }

        public MyException(String message) {
            super(message);
            this.code = 0;
        }

        public MyException(Throwable cause) {
            super(cause);
            this.code = 0;
        }

        public MyException(long code) {
            super();
            this.code = code;
        }

        public MyException(long code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public MyException(long code, String message) {
            super(message);
            this.code = code;
        }

        public MyException(long code, Throwable cause) {
            super(cause);
            this.code = code;
        }

        public void setCode(long code) {
            this.code = code;
        }

        public long getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "MyException{" +
                    "code=" + code +
                    '}';
        }
    }
}
