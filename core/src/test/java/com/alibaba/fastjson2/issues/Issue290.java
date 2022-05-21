package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue290 {
    @Test
    public void test() {
        AdminLoginForm adminLoginForm = new AdminLoginForm();
        adminLoginForm.password = "111";
        Admin admin = JSON.parseObject(JSON.toJSONString(adminLoginForm), Admin.class);
        assertNull(admin.password);
    }

    public class AdminLoginForm {
        /**
         * 管理员账号
         */
//        @NotBlank(message = "用户名不能为空")
//        @Length(min = 2, max = 20, message = "用户名长度为{min}-{max}位")
//        @Pattern(regexp = "^\\w+$", message = "用户名只能由数字、字母或下划线组成")
        public String username;

        /**
         * 密码
         * 为保证安全，password不进行序列化
         */
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public String password;
    }

    public class Admin {
        /**
         * 管理员账号
         */
//        @NotBlank(message = "管理员账号不能为空")
//        @Length(min = 2, max = 20, message = "管理员账号长度为{min}-{max}位")
//        @Pattern(regexp = "^\\w+$", message = "管理员账号只能由数字、字母或下划线组成")
//        @Column(name = "username", nullable = false, unique = true)
        private String username;

        /**
         * 密码
         * 为保证安全，password不进行序列化
         */
//        @NotBlank(message = "密码不能为空")
//        @Column(name = "password", nullable = false)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String password;
    }
}
