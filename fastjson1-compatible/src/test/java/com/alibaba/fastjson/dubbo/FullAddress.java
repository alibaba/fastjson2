/**
 * Project: morgan.domain
 * <p>
 * File Created at 2009-6-11
 * $Id: FullAddress.java 77622 2011-03-03 08:31:45Z ding.lid $
 * <p>
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.fastjson.dubbo;

import java.util.Objects;

/**
 * @author xk1430
 */
public class FullAddress {
    private String countryId;

    private String countryName;

    private String provinceName;

    private String cityId;

    private String cityName;

    private String streetAddress;

    private String zipCode;

    public FullAddress() {
    }

    public FullAddress(
            String countryId,
            String provinceName,
            String cityId,
            String streetAddress,
            String zipCode) {
        this.countryId = countryId;
        this.countryName = countryId;
        this.provinceName = provinceName;
        this.cityId = cityId;
        this.cityName = cityId;
        this.streetAddress = streetAddress;
        this.zipCode = zipCode;
    }

    public FullAddress(String countryId, String countryName, String provinceName, String cityId,
                       String cityName, String streetAddress, String zipCode) {
        this.countryId = countryId;
        this.countryName = countryName;
        this.provinceName = provinceName;
        this.cityId = cityId;
        this.cityName = cityName;
        this.streetAddress = streetAddress;
        this.zipCode = zipCode;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryId, countryName, provinceName, cityId, cityName, streetAddress, zipCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FullAddress other = (FullAddress) obj;
        return Objects.equals(cityId, other.cityId)
                && Objects.equals(cityName, other.cityName)
                && Objects.equals(countryId, other.countryId)
                && Objects.equals(countryName, other.countryName)
                && Objects.equals(provinceName, other.provinceName)
                && Objects.equals(streetAddress, other.streetAddress)
                && Objects.equals(zipCode, other.zipCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (countryName != null && countryName.length() > 0) {
            sb.append(countryName);
        }
        if (provinceName != null && provinceName.length() > 0) {
            sb.append(" ");
            sb.append(provinceName);
        }
        if (cityName != null && cityName.length() > 0) {
            sb.append(" ");
            sb.append(cityName);
        }
        if (streetAddress != null && streetAddress.length() > 0) {
            sb.append(" ");
            sb.append(streetAddress);
        }
        return sb.toString();
    }
}
