package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

public class Issue862 {
    @Test
    public void test() {
        String str = "{\"code\":105,\"extFields\":{\"Signature\":\"XL1bamvn+ttxXnlEmfoWuYl977w=\",\"topic\":\"TBW102\",\"AccessKey\":\"dev\"},\"flag\":0,\"language\":0,\"opaque\":0,\"serializeTypeCurrentRPC\":0,\"version\":395}\n" +
                "\n" +
                "{\"code\":0,\"flag\":1,\"language\":\"JAVA\",\"opaque\":0,\"serializeTypeCurrentRPC\":\"JSON\",\"version\":399}{\"brokerDatas\":[{\"brokerAddrs\":{0:\"192.168.1.236:20911\"},\"brokerName\":\"broker-02\",\"cluster\":\"DefaultCluster\"},{\"brokerAddrs\":{0:\"192.168.1.236:10911\"},\"brokerName\":\"broker-01\",\"cluster\":\"DefaultCluster\"}],\"filterServerTable\":{},\"queueDatas\":[{\"brokerName\":\"broker-01\",\"perm\":7,\"readQueueNums\":8,\"topicSysFlag\":0,\"writeQueueNums\":8},{\"brokerName\":\"broker-02\",\"perm\":7,\"readQueueNums\":8,\"topicSysFlag\":0,\"writeQueueNums\":8}]}";
        JSON.parseObject(str, JSONReader.Feature.IgnoreCheckClose);
    }
}
