package com.alibaba.fastjson2.issues_2000;

import lombok.Data;

import java.util.List;

@Data
public class BlooddonationqueryVO {
    /**
     * cardType :
     * name :
     * beginTime :
     * endTime :
     * cardNo :
     * results :
     */
    private String cardType;
    private String name;
    private String beginTime;
    private String endTime;
    private String cardNo;
    private ResultsEntity results;
    private String msg;
    private String code;

    @Data
    public class ResultsEntity {
        private List<DataListEntiy> result;
    }

    @Data
    public static class DataListEntiy {
        private String donateAddress;
        private String bloodCapacity;
        private String donateTime;
    }
}
