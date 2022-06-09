package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue402 {
    @Test
    public void test() {
        JSON.mixIn(DeptTreeJson.class, DeptTreeJsonMixIn.class);
        JSONArray jsonArray = JSON.parseArray(str);
        DeptTreeJson test = jsonArray.getObject(0, DeptTreeJson.class);
        assertEquals("测试公司", test.getDeptName());
        JSON.mixIn(DeptTreeJson.class, null);
    }

    @Test
    public void test1() {
        JSONArray jsonArray = JSON.parseArray(str);
        DeptTreeJson test = jsonArray.getObject(0, DeptTreeJson.class);
        assertEquals("测试公司", test.getDeptName());
    }

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public static class DeptTreeJsonMixIn {

    }

    static String str = "[{\n" +
            "\"id\": 199616,\n" +
            "\"pid\": 0,\n" +
            "\"children\": [\n" +
            "{\n" +
            "\"id\": 199617,\n" +
            "\"pid\": 199616,\n" +
            "\"children\": [],\n" +
            "\"dept_name\": \"测试部\",\n" +
            "\"dept_type\": 1,\n" +
            "\"pt_node_path_code\": \"0/199616\",\n" +
            "\"pt_node_path_name\": \"/测试公司\",\n" +
            "\"school_id\": 101594,\n" +
            "\"show_order\": 3,\n" +
            "\"is_grade\": 0,\n" +
            "\"use_status\": 1,\n" +
            "\"is_leaf\": 0,\n" +
            "\"user_or_dept\": false,\n" +
            "\"child_dept_tree_dtos\": []\n" +
            "}\n" +
            "],\n" +
            "\"dept_name\": \"测试公司\",\n" +
            "\"dept_type\": 0,\n" +
            "\"pt_node_path_code\": \"0\",\n" +
            "\"pt_node_path_name\": \"/\",\n" +
            "\"school_id\": 101594,\n" +
            "\"show_order\": 1,\n" +
            "\"use_status\": 1,\n" +
            "\"is_leaf\": 0,\n" +
            "\"user_or_dept\": false,\n" +
            "\"child_dept_tree_dtos\": []\n" +
            "}]\n";
    public static class DeptTreeJson implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;

        private Long pid;

        private String dept_no;

        private String dept_name;

        private Integer dept_type;

        private String pt_node_path_code;

        private String pt_node_path_name;

        private Long school_id;

        private Long show_order;

        private Integer is_grade;

        private Integer use_status;

        private String selected;

        private String disabled;

        private Integer is_leaf;

        private Boolean user_or_dept;

        private List<DeptTreeJson> children;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getPid() {
            return pid;
        }

        public void setPid(Long pid) {
            this.pid = pid;
        }

        public String getDeptNo() {
            return dept_no;
        }

        public void setDeptNo(String dept_no) {
            this.dept_no = dept_no;
        }

        public String getDeptName() {
            return dept_name;
        }

        public void setDeptName(String dept_name) {
            this.dept_name = dept_name;
        }

        public Integer getDeptType() {
            return dept_type;
        }

        public void setDeptType(Integer dept_type) {
            this.dept_type = dept_type;
        }

        public String getPtNodePathCode() {
            return pt_node_path_code;
        }

        public void setPtNodePathCode(String pt_node_path_code) {
            this.pt_node_path_code = pt_node_path_code;
        }

        public String getPtNodePathName() {
            return pt_node_path_name;
        }

        public void setPtNodePathName(String pt_node_path_name) {
            this.pt_node_path_name = pt_node_path_name;
        }

        public Long getSchoolId() {
            return school_id;
        }

        public void setSchoolId(Long school_id) {
            this.school_id = school_id;
        }

        public Long getShowOrder() {
            return show_order;
        }

        public void setShowOrder(Long show_order) {
            this.show_order = show_order;
        }

        public Integer getIsGrade() {
            return is_grade;
        }

        public void setIsGrade(Integer is_grade) {
            this.is_grade = is_grade;
        }

        public Integer getUseStatus() {
            return use_status;
        }

        public void setUseStatus(Integer use_status) {
            this.use_status = use_status;
        }

        public String getSelected() {
            return selected;
        }

        public void setSelected(String selected) {
            this.selected = selected;
        }

        public String getDisabled() {
            return disabled;
        }

        public void setDisabled(String disabled) {
            this.disabled = disabled;
        }

        public Integer getIsLeaf() {
            return is_leaf;
        }

        public void setIsLeaf(Integer is_leaf) {
            this.is_leaf = is_leaf;
        }

        public Boolean getUserOrDept() {
            return user_or_dept;
        }

        public void setUserOrDept(Boolean user_or_dept) {
            this.user_or_dept = user_or_dept;
        }

        public List<DeptTreeJson> getChildren() {
            return children;
        }

        public void setChildren(List<DeptTreeJson> children) {
            this.children = children;
        }
    }
}
