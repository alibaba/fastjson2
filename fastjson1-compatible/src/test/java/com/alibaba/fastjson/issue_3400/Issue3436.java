package com.alibaba.fastjson.issue_3400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3436 {
    @Test
    public void test_for_issue() throws Exception {
        com.alibaba.fastjson.JSON.addMixInAnnotations(FileSystemResource.class, FileSystemResourceMixedIn.class);

        FileSystemResource fileSystemResource = new FileSystemResource("E:\\my-code\\test\\test-fastjson.txt");

        String json = JSON.toJSONString(fileSystemResource);
        assertEquals("{\"path\":\"E:/my-code/test/test-fastjson.txt\"}", json);

        FileSystemResource fsr1 = JSON.parseObject(json, FileSystemResource.class);
        assertEquals(fileSystemResource.getPath(), fsr1.getPath());
        System.out.println("file size after Serialize：" + fileSystemResource.getFile().length());
    }

    @JSONType(asm = false, includes = "path")
    public static class FileSystemResourceMixedIn {
        @JSONCreator
        public FileSystemResourceMixedIn(String path) {
        }
    }
}
