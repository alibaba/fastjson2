package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue683 {
    @Test
    public void test() {
        AiResultBean aiResultBean = new AiResultBean();
        aiResultBean.setCode("0");
        aiResultBean.setMessage("Success");
        List<AiResultBean.Data> dataList = new ArrayList<>();
        AiResultBean.Data dataItem = new AiResultBean.Data();
        dataItem.setLabel("DaoXianYiWu");
        dataItem.setConfidence(0.5646306276321411);
        dataItem.setXmin(1024);
        dataItem.setYmin(1280);
        dataItem.setXmax(1536);
        dataItem.setYmax(1920);
        dataList.add(dataItem);
        AiResultBean.Data dataItem1 = new AiResultBean.Data();
        dataItem1.setLabel("DaoXianYiWu");
        dataItem1.setConfidence(0.6646306276321411);
        dataItem1.setXmin(1025);
        dataItem1.setYmin(1281);
        dataItem1.setXmax(1537);
        dataItem1.setYmax(1921);
        dataList.add(dataItem1);
        AiResultBean.Data dataItem2 = new AiResultBean.Data();
        dataItem2.setLabel("DaoXianYiWu");
        dataItem2.setConfidence(0.7646306276321411);
        dataItem2.setXmin(1026);
        dataItem2.setYmin(1282);
        dataItem2.setXmax(1538);
        dataItem2.setYmax(1922);
        dataList.add(dataItem2);
        aiResultBean.setData(dataList);

        String str = JSON.toJSONString(aiResultBean, JSONWriter.Feature.PrettyFormat);
        JSONObject object = JSON.parseObject(str);
        JSONArray data = object.getJSONArray("data");
        assertEquals(3, data.size());
    }

    public static class AiResultBean {
        @JSONField
        private String code;
        @JSONField(ordinal = 1)
        private String message;
        @JSONField(ordinal = 2)
        private List<Data> data;

        public void setCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setData(List<Data> data) {
            this.data = data;
        }

        public List<Data> getData() {
            return data;
        }

        public static class Data {
            private String label;
            private double confidence;
            private int xmin;
            private int xmax;
            private int ymin;
            private int ymax;

            public void setLabel(String label) {
                this.label = label;
            }

            public String getLabel() {
                return label;
            }

            public void setConfidence(double confidence) {
                this.confidence = confidence;
            }

            public double getConfidence() {
                return confidence;
            }

            public void setXmin(int xmin) {
                this.xmin = xmin;
            }

            public int getXmin() {
                return xmin;
            }

            public void setXmax(int xmax) {
                this.xmax = xmax;
            }

            public int getXmax() {
                return xmax;
            }

            public void setYmin(int ymin) {
                this.ymin = ymin;
            }

            public int getYmin() {
                return ymin;
            }

            public void setYmax(int ymax) {
                this.ymax = ymax;
            }

            public int getYmax() {
                return ymax;
            }
        }
    }
}
