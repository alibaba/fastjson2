package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue555 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"values\":Set[]}", Bean.class);
        assertTrue(bean.values.isEmpty());
    }

    public static class Bean {
        private Set<String> values;

        public Set<String> getValues() {
            return values;
        }

        public void setValues(Set<String> values) {
            this.values = values;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"values\":Set[]}", Bean1.class);
        assertTrue(bean.values.isEmpty());
    }

    public static class Bean1 {
        private Set<Long> values;

        public Set<Long> getValues() {
            return values;
        }

        public void setValues(Set<Long> values) {
            this.values = values;
        }
    }

    @Test
    public void test2() {
        // 可正常序列化 （无Set）
        String ss1 = "{\"@type\":\"com.xueyi.system.api.model.LoginUser\",\"enterpriseId\":1,\"enterpriseName\":\"administrator\",\"expireTime\":1657824093068,\"ipaddr\":\"127.0.0.1\",\"isLessor\":\"Y\",\"loginTime\":1657780893068,\"permissions\":[\"*:*:*\"],\"roleIds\": [],\"roles\":[\"administrator\",\"admin\"],\"scope\":{\"dataScope\":\"1\",\"deptScope\":[],\"postScope\":[],\"userScope\":[]},\"sourceName\":\"slave\",\"token\":\"d9d04f73-6933-47b5-8549-963733b96ddf\",\"userId\":1,\"userName\":\"admin\",\"userType\":\"00\"}";
        // 反序列化异常 （有Set）
        String ss2 = "{\"@type\":\"com.xueyi.system.api.model.LoginUser\",\"enterpriseId\":1,\"enterpriseName\":\"administrator\",\"expireTime\":1657824093068,\"ipaddr\":\"127.0.0.1\",\"isLessor\":\"Y\",\"loginTime\":1657780893068,\"permissions\":Set[\"*:*:*\"],\"roleIds\": Set[],\"roles\":Set[\"administrator\",\"admin\"],\"scope\":{\"dataScope\":\"1\",\"deptScope\":Set[],\"postScope\":Set[],\"userScope\":Set[]},\"sourceName\":\"slave\",\"token\":\"d9d04f73-6933-47b5-8549-963733b96ddf\",\"userId\":1,\"userName\":\"admin\",\"userType\":\"00\"}";

        LoginUser bean1 = JSON.parseObject(ss1, LoginUser.class);
        assertNotNull(bean1);
        LoginUser bean2 = JSON.parseObject(ss2, LoginUser.class);
        assertNotNull(bean2);
    }

    public static class LoginUser {
        /** 用户唯一标识 */
        private String token;

        /** 企业账号Id */
        private Long enterpriseId;

        /** 企业账号 */
        private String enterpriseName;

        /** 租户标识 */
        private String isLessor;

        /** 用户名Id */
        private Long userId;

        /** 用户名 */
        private String userName;

        /** 用户标识 */
        private String userType;

        /** 主数据源 */
        private String sourceName;

        /** 登录时间 */
        private Long loginTime;

        /** 过期时间 */
        private Long expireTime;

        /** 登录IP地址 */
        private String ipaddr;

        /** 权限列表 */
        private Set<String> permissions;

        /** 角色权限列表 */
        private Set<String> roles;

        /** 角色Id列表 */
        private Set<Long> roleIds;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Long getEnterpriseId() {
            return enterpriseId;
        }

        public void setEnterpriseId(Long enterpriseId) {
            this.enterpriseId = enterpriseId;
        }

        public String getEnterpriseName() {
            return enterpriseName;
        }

        public void setEnterpriseName(String enterpriseName) {
            this.enterpriseName = enterpriseName;
        }

        public String getIsLessor() {
            return isLessor;
        }

        public void setIsLessor(String isLessor) {
            this.isLessor = isLessor;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public Long getLoginTime() {
            return loginTime;
        }

        public void setLoginTime(Long loginTime) {
            this.loginTime = loginTime;
        }

        public Long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Long expireTime) {
            this.expireTime = expireTime;
        }

        public String getIpaddr() {
            return ipaddr;
        }

        public void setIpaddr(String ipaddr) {
            this.ipaddr = ipaddr;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }

        public Set<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(Set<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }
}
