package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.*;
import com.google.common.collect.Lists;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2187 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.items = new ArrayList<>();
        bean.items.add(new Item());
        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean);
        JSONArray items = (JSONArray) jsonObject.get("items");
        assertEquals(1, items.size());
    }

    @Data
    public static class Bean {
        List<Item> items;
    }

    public static class Item {
    }

    @Test
    public void test1() throws Exception {
        JSONObject object = new JSONObject();
        object.put("ref", object);
        assertSame(object, JSON.toJSON(object));
    }

    @Test
    public void test2() throws Exception {
        JSONArray array = new JSONArray();
        array.add(array);
        assertSame(array, JSON.toJSON(array));
    }

    @Test
    public void test3() throws Exception {
        Bean3 bean = new Bean3();
        bean.value = bean;
        JSONObject json = (JSONObject) JSON.toJSON(bean);
        assertSame(json, json.get("value"));
    }

    public static class Bean3 {
        public Bean3 value;
    }

    @Test
    public void test4() throws Exception {
        Bean4 bean = new Bean4();
        bean.values = Arrays.asList(bean);
        JSONObject json = (JSONObject) JSON.toJSON(bean);
        assertSame(json, json.getJSONArray("values").get(0));
    }

    public static class Bean4 {
        public List<Bean4> values;
    }

    @Data
    public class DynFormDto {
        private String sysName;
        private String name;
        private boolean enableTab;
        private int labelColSize;
        private String title;
        private String hint;
        private List<DynFormComponentDto> componentList;
        private String notes;
    }

    @Data
    public class DynFormComponentDto {
        private String name;
        private String nameText;
        private String functionName;
        private String compTypeName;
        private JSONObject props; // 组件完整配置
        private int widthColSize;
        protected int widthPercentage; //宽度百分比
        private String tabName;
        private boolean enabled;
        private long uid;
        private long parentFieldId;

        private List<DynFormComponentDto> subComponentList; //子组件集合

        public List<DynFormComponentDto> getFlatComponentDtos() {
            List<DynFormComponentDto> flatList = Lists.newArrayList();
            flatList.add(this);
            if (this.subComponentList != null) {
                for (DynFormComponentDto tmpDto : this.subComponentList) {
                    tmpDto.setParentFieldId(this.uid);
                    flatList.addAll(tmpDto.getFlatComponentDtos());
                }
            }
            return flatList;
        }
    }

    @Test
    public void test5() {
        String dfDtoJsonStr = "{\"componentList\":[{\"compTypeName\":\"Text\",\"enabled\":true,\"name\":\"notes\",\"nameText\":\"备注\",\"parentFieldId\":0,\"props\":{\"compTypeNameText\":\"文本框\",\"fieldName\":\"notes\",\"notes\":\"\",\"widthColSize\":2,\"widthPercentage\":\"80\",\"nameText\":\"备注\",\"type\":\"SingleField\",\"compTypeName\":\"Text\",\"parentFieldId\":0,\"enabled\":true,\"uid\":156592457733,\"structType\":\"Single\",\"valueType\":\"String\",\"name\":\"notes\",\"readonly\":false,\"required\":false},\"structType\":\"Single\",\"subComponentList\":[],\"uid\":156592457733,\"widthColSize\":2,\"widthPercentage\":0}],\"enableTab\":false,\"hint\":\"\",\"id\":0,\"labelColSize\":2,\"name\":\"test_test\",\"notes\":\"\",\"sysName\":\"StandingBook\",\"title\":\"\"}";
        DynFormDto dfDto = JSON.parseObject(dfDtoJsonStr, DynFormDto.class, JSONReader.Feature.FieldBased);
        JSONObject dfDtoJson = (JSONObject) JSON.toJSON(dfDto);
        assertNotNull(dfDtoJson);
        JSONArray jsonArray = dfDtoJson.getJSONArray("componentList").getJSONObject(0).getJSONArray("flatComponentDtos");
        JSON.toJSONString(jsonArray, JSONWriter.Feature.ReferenceDetection);
    }
}
