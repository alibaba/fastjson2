package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1683 {
    @Test
    public void test_for_issue() throws Exception {
        String line = "[2, \"浪漫奇侠\", \"雨天不打伞\", 4536]";
        BookDO book = JSON.parseObject(line, BookDO.class, Feature.SupportArrayToBean);
        assertEquals(2L, book.bookId.longValue());
        assertEquals("浪漫奇侠", book.bookName);
        assertEquals("雨天不打伞", book.authorName);
        assertEquals(4536, book.wordCount.intValue());
    }

    @JSONType(orders = {"bookId", "bookName", "authorName", "wordCount"})
    public static class BookDO {
        private Long bookId;

        private String bookName;

        private String authorName;

        private Integer wordCount;

        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public Integer getWordCount() {
            return wordCount;
        }

        public void setWordCount(Integer wordCount) {
            this.wordCount = wordCount;
        }
    }
}
