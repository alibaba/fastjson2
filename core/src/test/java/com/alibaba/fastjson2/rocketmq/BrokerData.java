package com.alibaba.fastjson2.rocketmq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BrokerData {
    public static final long MASTER_ID = 0L;

    private String cluster;
    private String brokerName;
    private HashMap<Long/* brokerId */, String/* broker address */> brokerAddrs;

    private final Random random = new Random();

    public String selectBrokerAddr() {
        String addr = this.brokerAddrs.get(MASTER_ID);

        if (addr == null) {
            List<String> addrs = new ArrayList<String>(brokerAddrs.values());
            return addrs.get(random.nextInt(addrs.size()));
        }

        return addr;
    }

    public HashMap<Long, String> getBrokerAddrs() {
        return brokerAddrs;
    }

    public void setBrokerAddrs(HashMap<Long, String> brokerAddrs) {
        this.brokerAddrs = brokerAddrs;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
