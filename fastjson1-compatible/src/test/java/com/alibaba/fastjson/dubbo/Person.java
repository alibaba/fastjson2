/**
 * Project: dubbo.test
 * <p>
 * File Created at 2010-11-17
 * $Id: Person.java 77622 2011-03-03 08:31:45Z ding.lid $
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

import java.io.Serializable;
import java.util.Objects;

/**
 * TODO Comment of Person
 *
 * @author tony.chenl
 */
public class Person
        implements Serializable {
    String personId;

    String loginName;

    PersonStatus status;

    String email;

    String penName;

    PersonInfo infoProfile;

    public Person() {
    }

    public Person(String id) {
        this.personId = id;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public PersonInfo getInfoProfile() {
        return infoProfile;
    }

    public void setInfoProfile(PersonInfo infoProfile) {
        this.infoProfile = infoProfile;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public PersonStatus getStatus() {
        return this.status;
    }

    public void setStatus(PersonStatus status) {
        this.status = status;
    }

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, loginName, status, email, penName, infoProfile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(personId, person.personId)
                && Objects.equals(loginName, person.loginName)
                && status == person.status
                && Objects.equals(email, person.email)
                && Objects.equals(penName, person.penName)
                && Objects.equals(infoProfile, person.infoProfile);
    }

}
