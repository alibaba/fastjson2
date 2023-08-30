package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class Issue1812 {
    @Test
    public void test() throws Exception {
        URL resource = Issue1812.class.getClassLoader().getResource("issues/issue1812.json");
        String str = FileUtils.readFileToString(new File(resource.getFile()));
        List<DriverEntity> entity = JSON.parseArray(str, DriverEntity.class);
        JSONB.toBytes(entity);
    }

    @Data
    public class DriverEntity
            implements Serializable {
        private String id;
        private String uid;
        private Integer type;
        private String userName;
        private String gender;
        private String qualificationcertificatenumber;
        private String phoneNumber;
        private String iconUrl;
        private String carNumber;
        private Integer carMode;
        private Integer carDetailMode;
        //    private Map<String, String> images;
        private Integer state;
        private Integer cmsState;
        private String verifyReason;
        private Integer freeze;
        private String driversLicense;
        private String drivingLicense;
        private String operationLicense;
        private String idCard;
        private Double maxWeight;
        private Double maxVolume;
        private Integer created;
        private Integer updated;
        private Integer applyTime;
        private Integer verifiedTime;
        //    private PathEntity path;
        private Integer cityCode;
        private String cityName;
        private Double weight;
        private Double volume;
        private List<String> virtualGidList;
        private List<String> virtualGidList2;
        private Integer carNumberType;
        private Set<String> vehicleTagIdList;
        private String bindingVehicle;
        private Integer vehicleState;
        private Integer vehicleFreeze;
        private Integer carType;
        private String colour;
        private String validityTime;
        private Integer registerPlace;
        private Integer auditstatus;
        private String noPassCheckReason;
        private String carModeName;
        private String carDetilModeName;
        private String permitnumber;
        private Integer lbsStatus;
    }
}
