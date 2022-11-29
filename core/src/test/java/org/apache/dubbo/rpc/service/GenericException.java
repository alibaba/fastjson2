package org.apache.dubbo.rpc.service;

import com.alibaba.fastjson2.JSON;

import java.beans.Transient;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

public class GenericException
        extends RuntimeException {
    private static final long serialVersionUID = -1182299763306599962L;

    private final boolean useCause;
    private final String exceptionClass;
    private final String exceptionMessage;
    private final GenericExceptionInfo genericExceptionInfo;

    public GenericException() {
        this(null, null);
    }

    public GenericException(String exceptionClass, String exceptionMessage) {
        super(exceptionMessage);
        this.useCause = false;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
        this.genericExceptionInfo = new GenericExceptionInfo(exceptionClass, exceptionMessage, exceptionMessage, getStackTrace());
    }

    public GenericException(Throwable cause) {
        super(toString(cause));
        this.useCause = false;
        this.exceptionClass = cause.getClass().getName();
        this.exceptionMessage = cause.getMessage();
        this.genericExceptionInfo = new GenericExceptionInfo(this.exceptionClass, this.exceptionMessage, super.getMessage(), getStackTrace());
    }

    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    protected GenericException(GenericExceptionInfo info) {
        super(info.getMsg(), null, true, false);
        setStackTrace(info.getStackTrace());
        this.useCause = false;
        this.exceptionClass = info.getExClass();
        this.exceptionMessage = info.getExMsg();
        this.genericExceptionInfo = info;
    }

    @Transient
    public String getExceptionClass() {
        if (this.useCause) {
            return ((GenericException) getCause()).getExceptionClass();
        }
        return exceptionClass;
    }

    @Transient
    public String getExceptionMessage() {
        if (this.useCause) {
            return ((GenericException) getCause()).getExceptionMessage();
        }
        return exceptionMessage;
    }

    @Override
    @Transient
    public StackTraceElement[] getStackTrace() {
        if (this.useCause) {
            return ((GenericException) getCause()).getStackTrace();
        }
        return super.getStackTrace();
    }

    @Override
    @Transient
    public String getMessage() {
        if (this.useCause) {
            return getCause().getMessage();
        }
        return JSON.toJSONString(GenericExceptionInfo.createNoStackTrace(genericExceptionInfo));
    }

    public String getGenericException() {
        if (this.useCause) {
            return ((GenericException) getCause()).getGenericException();
        }
        return JSON.toJSONString(genericExceptionInfo);
    }

    @Override
    @Transient
    public String getLocalizedMessage() {
        return getMessage();
    }

    /**
     * create generic exception info
     */
    public static class GenericExceptionInfo
            implements Serializable {
        private String exClass;
        private String exMsg;
        private String msg;
        private StackTraceElement[] stackTrace;

        public GenericExceptionInfo() {
        }

        public GenericExceptionInfo(
                String exceptionClass,
                String exceptionMessage,
                String message,
                StackTraceElement[] stackTrace) {
            this.exClass = exceptionClass;
            this.exMsg = exceptionMessage;
            this.msg = message;
            this.stackTrace = stackTrace;
        }

        public static GenericExceptionInfo createNoStackTrace(GenericExceptionInfo info) {
            return new GenericExceptionInfo(info.getExClass(), info.getExMsg(), info.getMsg(), null);
        }

        public String getMsg() {
            return msg;
        }

        public String getExClass() {
            return exClass;
        }

        public String getExMsg() {
            return exMsg;
        }

        public void setExClass(String exClass) {
            this.exClass = exClass;
        }

        public void setExMsg(String exMsg) {
            this.exMsg = exMsg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public StackTraceElement[] getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
        }
    }
}
