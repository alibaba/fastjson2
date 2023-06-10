package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue409 {
    @Test
    public void test() {
        String str = "{\n" +
                "\"total\": 1,\n" +
                "\"records\": {\n" +
                "\"user_serial\": 2201266,\n" +
                "\"user_name\": \"朱忠恕\",\n" +
                "\"codes\": [\n" +
                "\"Teacher\"\n" +
                "],\n" +
                "\"dept_list\": [\n" +
                "{\n" +
                "\"dept_id\": 200882,\n" +
                "\"pt_node_path_name\": \"/测试学校\",\n" +
                "\"dept_name\": \"办公室\",\n" +
                "\"is_grade\": 0\n" +
                "}\n" +
                "],\n" +
                "\"manage_class_ids\": [],\n" +
                "\"user_deps\": [\n" +
                "200882\n" +
                "]\n" +
                "}\n" +
                "}";

        JSONObject teacherObject = JSON.parseObject(str);
        List<TeacherJson> teacherList = teacherObject.getList("records", TeacherJson.class);
        assertNotNull(teacherList);
        assertEquals(1, teacherList.size());
        TeacherJson teacherJson = teacherList.get(0);
        assertEquals("朱忠恕", teacherJson.userName);
    }
    public static class TeacherJson {
        private static final long serialVersionUID = 1L;

        @JSONField(name = "user_serial")
        private Long userSerial;

        @JSONField(name = "user_name")
        private String userName;

        private List<String> codes;

        @JSONField(name = "dept_list")
        private List<DeptList> deptList;

        @JSONField(name = "manage_class_ids")
        private List<DeptList> manageClassIds;

        @JSONField(name = "user_deps")
        private List<String> userDeps;

        public Long getUserSerial() {
            return userSerial;
        }

        public void setUserSerial(Long userSerial) {
            this.userSerial = userSerial;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public List<String> getCodes() {
            return codes;
        }

        public void setCodes(List<String> codes) {
            this.codes = codes;
        }

        public List<DeptList> getDeptList() {
            return deptList;
        }

        public void setDeptList(List<DeptList> deptList) {
            this.deptList = deptList;
        }

        public List<DeptList> getManageClassIds() {
            return manageClassIds;
        }

        public void setManageClassIds(List<DeptList> manageClassIds) {
            this.manageClassIds = manageClassIds;
        }

        public List<String> getUserDeps() {
            return userDeps;
        }

        public void setUserDeps(List<String> userDeps) {
            this.userDeps = userDeps;
        }

        public String toJson() {
            return JSON.toJSONString(this);
        }

        @Override
        public String toString() {
            return this.toJson();
        }
    }

    public static class DeptList {
        private static final long serialVersionUID = 1L;

        @JSONField(name = "dept_id")
        private Long deptId;

        @JSONField(name = "pt_node_path_name")
        private String ptNodePathName;

        @JSONField(name = "dept_name")
        private String deptName;

        @JSONField(name = "is_grade")
        private String isGrade;

        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        public String getPtNodePathName() {
            return ptNodePathName;
        }

        public void setPtNodePathName(String ptNodePathName) {
            this.ptNodePathName = ptNodePathName;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public String getIsGrade() {
            return isGrade;
        }

        public void setIsGrade(String isGrade) {
            this.isGrade = isGrade;
        }
    }
}
