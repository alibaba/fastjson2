package com.alibaba.fastjson2.v1issues.issue_4100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4194 {
    @Test
    public void typeRefTestCase() {
        DataRow<SubModel> dataRow = new DataRow<>();
        dataRow.setSampleNum("22B00123");
        SubModel subModel = new SubModel();
        subModel.setRowIndex(1);
        subModel.setAge(30);
        dataRow.setModel(subModel);

        String jsonString = JSON.toJSONString(dataRow);
        System.out.println(jsonString);

        JSONObject jsonObject = JSON.parseObject(jsonString);
        DataRow<SubModel> parseDataRow = jsonObject.toJavaObject(new TypeReference<DataRow<SubModel>>() {
        });
        SubModel model = parseDataRow.getModel();
        assertEquals(30, model.getAge());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BaseModel {
        private Integer rowIndex;

        public Integer getRowIndex() {
            return rowIndex;
        }

        public BaseModel setRowIndex(Integer rowIndex) {
            this.rowIndex = rowIndex;
            return this;
        }
    }

    public static class DataRow<T extends BaseModel> {
        private String sampleNum;
        private T model;

        public String getSampleNum() {
            return sampleNum;
        }

        public DataRow<T> setSampleNum(String sampleNum) {
            this.sampleNum = sampleNum;
            return this;
        }

        public T getModel() {
            return model;
        }

        public DataRow<T> setModel(T model) {
            this.model = model;
            return this;
        }
    }

    public static class SubModel
            extends BaseModel {
        private int age;

        public int getAge() {
            return age;
        }

        public SubModel setAge(int age) {
            this.age = age;
            return this;
        }
    }
}
