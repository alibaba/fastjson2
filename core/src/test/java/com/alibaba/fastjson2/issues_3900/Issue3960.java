package com.alibaba.fastjson2.issues_3900;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3960 {
    @Test
    public void test() throws Exception {
        String message = "<Container>" +
                "<WeightMajor measurementSystem=\"English\" unit=\"lbs\">0</WeightMajor>" +
                "</Container>";

        // 使用 JDK 原生 API 解析，不依赖 JAXB
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(message)));

        Element root = document.getDocumentElement();
        Element weightMajor = (Element) root.getFirstChild();

        String json1 = com.alibaba.fastjson.JSON.toJSONString(weightMajor);
        String json2 = com.alibaba.fastjson2.JSON.toJSONString(weightMajor);
        assertEquals(json1, json2);
    }
}
