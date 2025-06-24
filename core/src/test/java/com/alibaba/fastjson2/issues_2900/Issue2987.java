package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2987 {
    @Test
    public void test() {
        MyFile myFile = new MyFile();
        myFile.type = "file";
        myFile.name = "my_file.txt";
        myFile.data = "Some content ...";

        MyDirectory myDirectory = new MyDirectory();
        myDirectory.type = "directory";
        myDirectory.name = "My directory";
        myDirectory.items = Arrays.asList(myFile);

        String json = JSON.toJSONString(myDirectory);
        System.out.println(json);
        {
            MyDirectory object = JSON.parseObject(json, MyDirectory.class);
            assertNotNull(object);
        }
        {
            MyNode object = JSON.parseObject(json, MyNode.class);
            assertTrue(object instanceof MyDirectory);
        }
    }

    @JSONType(
            typeKey = "type",
            seeAlso = {MyDirectory.class, MyFile.class}
    )
    abstract class MyNode {
        public String type;
        public String name;
    }

    @JSONType(typeName = "file")
    class MyFile
            extends MyNode {
        public String data;
    }

    @JSONType(typeName = "directory")
    class MyDirectory
            extends MyNode {
        public List<MyNode> items;
    }
}
