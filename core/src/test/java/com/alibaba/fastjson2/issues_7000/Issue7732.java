package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue7732 {
    private static final String LOGIN_USER_JSON = "{\"expireTime\":1785411580135,\"ifRemindResetPassword\":false,\"ipaddr\":\"10.10.10.10\",\"loginTime\":1785409780135,\"passwordExpiredDay\":0,\"permissions\":[\"::*\"],\"sysUser\":{\"admin\":true,\"apiCall\":0,\"businessCatalogType\":2,\"isEnableSecurityDownload\":0,\"isSingleLogin\":0,\"limitSingleLogin\":false,\"ptzLockTime\":1,\"status\":1,\"telephone\":\"2asmPb/g4knm0thZtw==\",\"tenantId\":1,\"tenantMgr\":false,\"userCode\":\"aaadmin\",\"userId\":12,\"userLevel\":3,\"userName\":\"tt\",\"userPassword\":\"9C5gafUuHPSOqYUzP4TksEOS8OhJo=\",\"userType\":3,\"videoPlayConcurrent\":50},\"token\":\"0:aaadmin:0ecac3d2-3daeaa\",\"userCode\":\"aaadmin\",\"userid\":12,\"username\":\"tt\"}";
    private static final String DEVICE_JSON = "{\"auth\":true,\"broadcastPushAfterAck\":false,\"channelCount\":0,\"deviceId\":\"44444444\",\"deviceType\":3,\"enableAudio\":true,\"expires\":86400,\"firmware\":\"V4.62.325\",\"hostAddress\":\"10.20.30.11:606\",\"ip\":\"10.20.30.11\",\"isStoreGbDeviceAlarm\":0,\"keepaliveIntervalTime\":60,\"keepaliveTime\":\"2026-07-30 18:54:53\",\"localIp\":\"10.20.30.145\",\"manufacturer\":\"HIKVISION\",\"mediaRouteId\":1,\"mobilePositionSubmissionInterval\":5,\"model\":\"DS-7616NX-I3\",\"name\":\"DeepinMind\",\"networkType\":4,\"password\":\"O11DE0OTg3                      \",\"port\":25606,\"registerTime\":\"2026-07-30 08:37:46\",\"sipGatewayGroupId\":\"sipRoute_1\",\"sipGatewayRouteId\":2,\"sipServerId\":\"1\",\"ssrcCheck\":true,\"streamMode\":\"TCP-PASSIVE\",\"streamModeForParam\":1,\"subscribeCycleForAlarm\":0,\"subscribeCycleForCatalog\":3600,\"subscribeCycleForMobilePosition\":0,\"transport\":\"UDP\",\"updateTime\":\"2026-07-30 18:54:53\",\"useStatus\":1,\"vendorId\":2}";
    private static final String ROUTE_JSON = "{\"id\":2,\"routeCode\":\"sipRoute_1\",\"routeName\":\"信令路由1\"}";

    @Test
    public void testLoginUserPayload() {
        LoginUser loginUser = JSON.parseObject(LOGIN_USER_JSON, LoginUser.class);
        assertEquals("aaadmin", loginUser.userCode);
        assertEquals("tt", loginUser.username);
        assertTrue(loginUser.sysUser.admin);

        JSONObject object = JSON.parseObject(LOGIN_USER_JSON);
        assertEquals("aaadmin", object.getString("userCode"));
    }

    @Test
    public void testDevicePayload() {
        Device device = JSON.parseObject(DEVICE_JSON, Device.class);
        assertEquals("44444444", device.deviceId);
        assertEquals("sipRoute_1", device.sipGatewayGroupId);

        JSONObject object = JSON.parseObject(DEVICE_JSON);
        assertEquals("DeepinMind", object.getString("name"));
    }

    @Test
    public void testRoutePayload() {
        SipRouteCache route = JSON.parseObject(ROUTE_JSON, SipRouteCache.class);
        assertEquals(2, route.id);
        assertEquals("sipRoute_1", route.routeCode);
        assertEquals("\u4fe1\u4ee4\u8def\u75311", route.routeName);
    }

    public static class LoginUser {
        public long expireTime;
        public boolean ifRemindResetPassword;
        public String ipaddr;
        public long loginTime;
        public int passwordExpiredDay;
        public String[] permissions;
        public SysUser sysUser;
        public String token;
        public String userCode;
        public int userid;
        public String username;
    }

    public static class SysUser {
        public boolean admin;
        public int apiCall;
        public int businessCatalogType;
        public int isEnableSecurityDownload;
        public int isSingleLogin;
        public boolean limitSingleLogin;
        public int ptzLockTime;
        public int status;
        public String telephone;
        public int tenantId;
        public boolean tenantMgr;
        public String userCode;
        public int userId;
        public int userLevel;
        public String userName;
        public String userPassword;
        public int userType;
        public int videoPlayConcurrent;
    }

    public static class Device {
        public boolean auth;
        public boolean broadcastPushAfterAck;
        public int channelCount;
        public String deviceId;
        public int deviceType;
        public boolean enableAudio;
        public int expires;
        public String firmware;
        public String hostAddress;
        public String ip;
        public int isStoreGbDeviceAlarm;
        public int keepaliveIntervalTime;
        public String keepaliveTime;
        public String localIp;
        public String manufacturer;
        public int mediaRouteId;
        public int mobilePositionSubmissionInterval;
        public String model;
        public String name;
        public int networkType;
        public String password;
        public int port;
        public String registerTime;
        public String sipGatewayGroupId;
        public int sipGatewayRouteId;
        public String sipServerId;
        public boolean ssrcCheck;
        public String streamMode;
        public int streamModeForParam;
        public int subscribeCycleForAlarm;
        public int subscribeCycleForCatalog;
        public int subscribeCycleForMobilePosition;
        public String transport;
        public String updateTime;
        public int useStatus;
        public int vendorId;
    }

    public static class SipRouteCache {
        public int id;
        public String routeCode;
        public String routeName;
    }
}
