package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeFilterTest1 {
    @Test
    public void testSet() {
        RpcException e = new RpcException();
        e.setStackTrace(new StackTraceElement[0]);
        JSONWriter.Feature[] writerFeatures = {JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol};
        byte[] bytes = JSONB.toBytes(e, writerFeatures);

        JSONBDump.dump(bytes);

        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(true, "com.alibaba.fastjson2.autoType.AutoTypeFilterTest1");
        RpcException e1 = (RpcException) JSONB.parseObject(bytes, Object.class, filter, JSONReader.Feature.FieldBased);
        assertEquals(e.getStackTrace().length, e1.getStackTrace().length);
    }

    public static class RpcException
            extends RuntimeException {
        public static final int UNKNOWN_EXCEPTION = 0;
        public static final int NETWORK_EXCEPTION = 1;
        public static final int TIMEOUT_EXCEPTION = 2;
        public static final int BIZ_EXCEPTION = 3;
        public static final int FORBIDDEN_EXCEPTION = 4;
        public static final int SERIALIZATION_EXCEPTION = 5;
        public static final int NO_INVOKER_AVAILABLE_AFTER_FILTER = 6;
        public static final int LIMIT_EXCEEDED_EXCEPTION = 7;
        public static final int TIMEOUT_TERMINATE = 8;
        public static final int REGISTRY_EXCEPTION = 9;
        public static final int ROUTER_CACHE_NOT_BUILD = 10;
        public static final int METHOD_NOT_FOUND = 11;
        public static final int VALIDATION_EXCEPTION = 12;
        private static final long serialVersionUID = 7815426752583648734L;
        /**
         * RpcException cannot be extended, use error code for exception type to keep compatibility
         */
        private final int code;

        public RpcException() {
            super();
            code = 0;
        }

        public RpcException(String message, Throwable cause) {
            super(message, cause);
            code = 0;
        }

        public RpcException(String message) {
            super(message);
            code = 0;
        }

        public RpcException(Throwable cause) {
            super(cause);
            code = 0;
        }

        public RpcException(int code) {
            super();
            this.code = code;
        }

        public RpcException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public RpcException(int code, String message) {
            super(message);
            this.code = code;
        }

        public RpcException(int code, Throwable cause) {
            super(cause);
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public boolean isBiz() {
            return code == BIZ_EXCEPTION;
        }

        public boolean isForbidden() {
            return code == FORBIDDEN_EXCEPTION;
        }

        public boolean isTimeout() {
            return code == TIMEOUT_EXCEPTION;
        }

        public boolean isNetwork() {
            return code == NETWORK_EXCEPTION;
        }

        public boolean isSerialization() {
            return code == SERIALIZATION_EXCEPTION;
        }

        public boolean isNoInvokerAvailableAfterFilter() {
            return code == NO_INVOKER_AVAILABLE_AFTER_FILTER;
        }

        public boolean isLimitExceed() {
            return code == LIMIT_EXCEEDED_EXCEPTION || getCause() instanceof LimitExceededException;
        }

        public boolean isValidation() {
            return code == VALIDATION_EXCEPTION;
        }
    }

    public static class LimitExceededException
            extends Exception {
    }
}
