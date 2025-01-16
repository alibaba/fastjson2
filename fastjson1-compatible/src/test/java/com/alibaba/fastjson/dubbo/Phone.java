/**
 * Project: morgan.domain
 * <p>
 * File Created at 2009-6-11
 * $Id: Phone.java 77622 2011-03-03 08:31:45Z ding.lid $
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
 * 电话号码
 *
 * @author xk1430
 */
public class Phone {
    private String country;

    private String area;

    private String number;

    private String extensionNumber;

    public Phone() {
    }

    public Phone(String country, String area, String number, String extensionNumber) {
        this.country = country;
        this.area = area;
        this.number = number;
        this.extensionNumber = extensionNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getExtensionNumber() {
        return extensionNumber;
    }

    public void setExtensionNumber(String extensionNumber) {
        this.extensionNumber = extensionNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, area, number, extensionNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Phone phone = (Phone) o;
        return Objects.equals(country, phone.country) && Objects.equals(area, phone.area) && Objects.equals(number, phone.number) && Objects.equals(extensionNumber, phone.extensionNumber);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (country != null && country.length() > 0) {
            sb.append(country);
            sb.append("-");
        }
        if (area != null && area.length() > 0) {
            sb.append(area);
            sb.append("-");
        }
        if (number != null && number.length() > 0) {
            sb.append(number);
        }
        if (extensionNumber != null && extensionNumber.length() > 0) {
            sb.append("-");
            sb.append(extensionNumber);
        }
        return sb.toString();
    }
}
