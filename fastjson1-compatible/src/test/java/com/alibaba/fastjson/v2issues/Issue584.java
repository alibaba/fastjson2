package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class Issue584 {
    @Test
    public void test() {
        List<ConfigModel> configModels = JSON.parseArray(str, ConfigModel.class);
        assertEquals(4, configModels.size());
    }

    @Data
    public static class FieLdsModel {
        private ConfigModel config;
        private SlotModel slot;
        private String placeholder;
        private Object style;
        private Boolean clearable;
        private String prefixicon;
        private Integer precision;
        private String suffixicon;
        private String maxlength;
        private Boolean showWordLimit;
        private Boolean readonly;
        private Boolean disabled;

        private String vModel = "";

        private String modelId = "";

        private String relationField;
        private Boolean hasPage;
        private String pageSize;
        private String type;
        private Object autosize;
        private Integer step;
        private Boolean stepstrictly;
        private String controlsposition;
        private Object textStyle;
        private Integer lineHeight;
        private Integer fontSize;
        private Boolean showChinese;
        private Boolean showPassword;

        private String size;
        private Boolean filterable;

        private String showField;
    }

    @Data
    public static class ConfigModel {
        private String label;
        private String labelWidth;
        private Boolean showLabel;
        private Boolean changeTag;
        private Boolean border;
        private String tag;
        private String tagIcon;
        private boolean required;
        private String layout;
        private String dataType;
        private Integer span = 24;
        private String pfKey;
        private String dictionaryType;
        private Integer formId;
        private Long renderKey;
        private Integer columnWidth;
        private List<RegListModel> regList;
        private Object defaultValue;
        private String active;

        private String options;

        private String valueType;
        private String propsUrl;
        private String optionType;
        private ConfigPropsModel props;

        private Boolean showTitle;
        private String tableName;
        private List<FieLdsModel> children;

        private String visibility = "[\"app\",\"pc\"]";

        private String rule;

        private String trigger = "blur";

        private Boolean noShow;

        private int childNum;
        private String model;

        private boolean app = true;
        private boolean pc = true;
    }

    public static class SlotModel {
    }

    public static class RegListModel {
    }

    public static class ConfigPropsModel {
    }

    public static String str = "[{\n" +
            "\"config\": {\n" +
            "\"formId\": 115,\n" +
            "\"visibility\": [\"pc\", \"app\"],\n" +
            "\"pfKey\": \"select\",\n" +
            "\"defaultValue\": \"\",\n" +
            "\"dataType\": \"static\",\n" +
            "\"dictionaryType\": \"\",\n" +
            "\"dragDisabled\": false,\n" +
            "\"label\": \"所属项目\",\n" +
            "\"propsUrl\": \"\",\n" +
            "\"showLabel\": true,\n" +
            "\"required\": false,\n" +
            "\"props\": {\n" +
            "\"label\": \"fullName\",\n" +
            "\"value\": \"id\"\n" +
            "},\n" +
            "\"renderKey\": 1611127618069,\n" +
            "\"tableName\": \"test_subpackage\",\n" +
            "\"layout\": \"colFormItem\",\n" +
            "\"tagIcon\": \"icon-ym icon-ym-generator-select\",\n" +
            "\"tag\": \"el-select\",\n" +
            "\"span\": 24\n" +
            "},\n" +
            "\"vModel\": \"projectS\",\n" +
            "\"disabled\": false,\n" +
            "\"placeholder\": \"请选择\",\n" +
            "\"slot\": {\n" +
            "\"options\": [{\n" +
            "\"fullName\": \"选项一\",\n" +
            "\"id\": \"1\"\n" +
            "}, {\n" +
            "\"fullName\": \"选项二\",\n" +
            "\"id\": \"2\"\n" +
            "}]\n" +
            "}\n" +
            "}, {\n" +
            "\"config\": {\n" +
            "\"formId\": 101,\n" +
            "\"visibility\": [\"pc\", \"app\"],\n" +
            "\"pfKey\": \"depSelect\",\n" +
            "\"dragDisabled\": false,\n" +
            "\"label\": \"所属部门\",\n" +
            "\"showLabel\": true,\n" +
            "\"required\": false,\n" +
            "\"renderKey\": 1606979361829,\n" +
            "\"tableName\": \"test_subpackage\",\n" +
            "\"layout\": \"colFormItem\",\n" +
            "\"tagIcon\": \"icon-ym icon-ym-generator-department\",\n" +
            "\"changeTag\": true,\n" +
            "\"tag\": \"dep-select\",\n" +
            "\"regList\": [],\n" +
            "\"span\": 12\n" +
            "},\n" +
            "\"vModel\": \"depNameA\",\n" +
            "\"disabled\": false,\n" +
            "\"placeholder\": \"请选择\"\n" +
            "}, {\n" +
            "\"clearable\": true,\n" +
            "\"config\": {\n" +
            "\"formId\": 116,\n" +
            "\"visibility\": [\"pc\", \"app\"],\n" +
            "\"pfKey\": \"userSelect\",\n" +
            "\"dragDisabled\": false,\n" +
            "\"label\": \"填单人\",\n" +
            "\"trigger\": \"click\",\n" +
            "\"showLabel\": true,\n" +
            "\"required\": false,\n" +
            "\"tableName\": \"test_subpackage\",\n" +
            "\"renderKey\": 1652256065583,\n" +
            "\"layout\": \"colFormItem\",\n" +
            "\"tagIcon\": \"icon-ym icon-ym-generator-user\",\n" +
            "\"tag\": \"user-select\",\n" +
            "\"regList\": [],\n" +
            "\"span\": 12\n" +
            "},\n" +
            "\"multiple\": false,\n" +
            "\"vModel\": \"applys\",\n" +
            "\"disabled\": false,\n" +
            "\"placeholder\": \"请选择\",\n" +
            "\"on\": {\n" +
            "\"change\": \"({ data, formData, setFormData, setShowOrHide, setRequired, setDisabled, request, getFieldOptions, setFieldOptions }) => {\\n // 在此编写代码\\n \\n}\"\n" +
            "}\n" +
            "}, {\n" +
            "\"clearable\": true,\n" +
            "\"prefix-icon\": \"\",\n" +
            "\"show-password\": false,\n" +
            "\"config\": {\n" +
            "\"formId\": 118,\n" +
            "\"visibility\": [\"pc\", \"app\"],\n" +
            "\"pfKey\": \"comInput\",\n" +
            "\"noShow\": false,\n" +
            "\"dragDisabled\": false,\n" +
            "\"label\": \"承包方\",\n" +
            "\"trigger\": \"blur\",\n" +
            "\"showLabel\": true,\n" +
            "\"required\": false,\n" +
            "\"tableName\": \"test_subpackage\",\n" +
            "\"renderKey\": 1652256104063,\n" +
            "\"layout\": \"colFormItem\",\n" +
            "\"tagIcon\": \"icon-ym icon-ym-generator-input\",\n" +
            "\"tag\": \"el-input\",\n" +
            "\"regList\": [],\n" +
            "\"span\": 24\n" +
            "},\n" +
            "\"readonly\": false,\n" +
            "\"vModel\": \"contractor\",\n" +
            "\"style\": {\n" +
            "\"width\": \"100%\"\n" +
            "},\n" +
            "\"disabled\": false,\n" +
            "\"placeholder\": \"请输入\",\n" +
            "\"show-word-limit\": false,\n" +
            "\"slot\": {\n" +
            "\"prepend\": \"\",\n" +
            "\"append\": \"\"\n" +
            "},\n" +
            "\"suffix-icon\": \"\",\n" +
            "\"on\": {\n" +
            "\"change\": \"({ data, formData, setFormData, setShowOrHide, setRequired, setDisabled, request, getFieldOptions, setFieldOptions }) => {\\n // 在此编写代码\\n \\n}\",\n" +
            "\"blur\": \"({ data, formData, setFormData, setShowOrHide, setRequired, setDisabled, request, getFieldOptions, setFieldOptions }) => {\\n // 在此编写代码\\n \\n}\"\n" +
            "}\n" +
            "\n" +
            "}]";
}
