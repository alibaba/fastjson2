package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3485 {
    @Test
    public void test() throws Exception {
        String json = "{\n" +
                "                    \"file_list\": [\n" +
                "                        11,  // CreateBlogRequest.java - 可能需要修改以支持删除操作\n" +
                "                        13,  // BlogRepository.java - 需要添加删除方法\n" +
                "                        22,  // CreateBlogResponse.java - 可能需要修改以支持删除操作的响应\n" +
                "                        26,  // BlogService.java - 需要添加删除博客的业务逻辑\n" +
                "                        27,  // BlogPost.java - 可能需要修改以支持删除操作\n" +
                "                        29,  // BlogController.java - 需要添加删除博客的API\n" +
                "                        33   // BlogServiceTest.java - 需要添加删除博客的测试用例\n" +
                "                    ]\n" +
                "                }";
        JSONObject jsonObject = JSON.parseObject(json);
        assertEquals("{\"file_list\":[11,13,22,26,27,29,33]}", jsonObject.toJSONString());
    }
}
