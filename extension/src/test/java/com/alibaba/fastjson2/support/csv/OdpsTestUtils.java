package com.alibaba.fastjson2.support.csv;

import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;

public class OdpsTestUtils {
    private static String accessID = "";
    private static String accessKey = "";
    private static String project = "sonar_test";
    private static String endpoint;

    public static Odps odps() {
        Account account = new AliyunAccount(accessID, accessKey);
        Odps odps = new Odps(account);
        odps.setDefaultProject(project);
        if (endpoint != null && !endpoint.isEmpty()) {
            odps.setEndpoint(endpoint);
        }
        return odps;
    }
}
