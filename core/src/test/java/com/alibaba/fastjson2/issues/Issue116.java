package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue116 {
    String testJson = "{\"soap:Envelope\":{\"-xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\",\"-xmlns:soap\":\"http://schemas.xmlsoap.org/soap/envelope/\",\"-xmlns:xsd\":\"http://www.w3.org/2001/XMLSchema\",\"soap:Body\":{\"getCountryCityByIpResponse\":{\"-xmlns\":\"http://WebXml.com.cn/\",\"getCountryCityByIpResult\":{\"string\":[\"30.40.202.23\",\"美国 俄亥俄州哥伦布市国防部网络信息中心\"]}}}}}";

    @Test
    public void test() {
        Object result = JSONPath.extract(testJson, "$['soap:Envelope']['soap:Body'].getCountryCityByIpResponse.getCountryCityByIpResult.string[*]");
        assertNotNull(result);
        assertEquals("[\"30.40.202.23\",\"美国 俄亥俄州哥伦布市国防部网络信息中心\"]", result.toString());
    }

    @Test
    public void test1() {
        Object result = JSONPath.extract(testJson, "$.soap\\:Envelope.soap\\:Body.getCountryCityByIpResponse.getCountryCityByIpResult.string[*]");
        assertNotNull(result);
        assertEquals("[\"30.40.202.23\",\"美国 俄亥俄州哥伦布市国防部网络信息中心\"]", result.toString());
    }
}
