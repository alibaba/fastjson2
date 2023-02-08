package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1072 {
    @Test
    public void test() {
        String result = "{\n" +
                "\"status\":0,\n" +
                "\"message\":\"查询成功\",\n" +
                "\"timestamp\":1673586491000,\n" +
                "\"data\":[\n" +
                "{\n" +
                "\"namespace\":\"unit08\",\n" +
                "\"tag\":\"AM\",\n" +
                "\"history\":0,\n" +
                "\"items\":[\n" +
                "{\n" +
                "\"circle\":0,\n" +
                "\"item\":\"AV\",\n" +
                "\"type\":\"double\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "}";
        PageResult<TagView> resultTag = JSON.parseObject(result, new TypeReference<PageResult<TagView>>() {
        });
        TagView tagView = resultTag.getData().get(0);
        assertEquals(0, tagView.history);
        assertEquals("AM", tagView.tag);
    }

    @Getter
    @Setter
    public static class TagItemModel
            implements Serializable {
        private static final long serialVersionUID = -6280378922670952440L;
        protected String item;
        protected String type;
        protected Integer history;
        protected Integer circle;
        protected String description;
    }

    @Getter
    @Setter
    public static class TagView
            implements Comparable<TagView>, Serializable {
        private String namespace;
        private String tag;
        private String type;
        //     private int source;
        private List<TagItemModel> items;
        private Integer history;
        private String label;
        private String description;

        @Override
        public int compareTo(TagView o) {
            if (this.namespace.compareTo(o.getNamespace()) == 0) {
                return this.tag.compareTo(o.tag);
            } else {
                return this.namespace.compareTo(o.getNamespace());
            }
        }
    }

    @Getter
    @Setter
    public static class AjaxResult {
        private String message;
        private int status;
        private long timestamp = Instant.now().toEpochMilli();

        public AjaxResult() {
        }

        public AjaxResult(int status, String message) {
            this.message = message;
            this.status = status;
        }
    }

    @Getter
    @Setter
    public static class MutiResult<T>
            extends AjaxResult {
        private List<T> data;

        public MutiResult() {
            super();
        }

        public MutiResult(List<T> data) {
            super();
            this.data = data;
        }

        public MutiResult(int status) {
            this(status, null);
        }

        public MutiResult(int status, String message) {
            super(status, message);
        }

        public MutiResult(int status, String message, List<T> data) {
            super(status, message);
            this.data = data;
        }
    }

    @Getter
    @Setter
    public static class PageResult<T>
            extends MutiResult<T> {
        private int pageNum = 1;
        private int pageSize;
        private long total;

        public PageResult() {
            super();
        }

        public PageResult(List<T> data) {
            super(data);
            this.pageSize = data == null ? 0 : data.size();
            this.total = this.pageSize;
        }

        public PageResult(long total, List<T> data) {
            super(data);
            this.pageSize = data == null ? 0 : data.size();
            this.total = total;
        }

        public PageResult(String message, long total, List<T> data) {
            super(0, message, data);
            this.pageNum = 1;
            this.pageSize = data == null ? 0 : data.size();
        }

        public PageResult(int status) {
            this(status, "");
        }

        public PageResult(int status, String message) {
            super(status, message);
        }

        public PageResult(int status, String message, List<T> data) {
            super(status, message, data);
        }

        public PageResult(int pageNum, int pageSize, long total, List<T> data) {
            super(data);
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.total = total;
        }

        public PageResult(int status, String message, int pageNum, int pageSize, long total, List<T> data) {
            super(status, message, data);
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.total = total;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}
