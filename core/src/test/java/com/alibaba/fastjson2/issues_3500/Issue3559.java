package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3559 {
    @Test
    public void test() throws IOException {
        FileSystemResource resource = new FileSystemResource(this.getClass().getClassLoader().getResource("issues/issue3559.json").getPath());
        long length = resource.contentLength();
        long modified = resource.lastModified();
        JSON.toJSONString(resource);
        assertEquals(length, resource.contentLength());
        assertEquals(modified, resource.lastModified());
    }
}
