package com.alibaba.fastjson2.benchmark.geoip;

import lombok.Data;

@Data
public class GeoIP {
    private String as;
    private String city;
    private String country;
    private String countryCode;
    private String isp;
    private double lat;
    private double lon;
    private String org;
    private String query;
    private String region;
    private String regionName;
    private String status;
    private String timezone;
    private String zip;
}
