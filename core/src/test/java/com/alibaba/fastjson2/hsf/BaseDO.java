package com.alibaba.fastjson2.hsf;

import java.util.Date;

public class BaseDO {
    private Integer id;
    private Boolean isRemoved;
    private Long creatorId;
    private Date gmtCreate;
    private Date gmtModified;
    private String gmtCreateString;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsRemoved() {
        /*33*/
        return this.isRemoved;
    }

    public void setIsRemoved(Boolean isRemoved) {
        /*37*/
        this.isRemoved = isRemoved;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getGmtCreateString() {
        /*18*/
        if (this.gmtCreate != null) {
            /*19*/
            return this.gmtCreate.toLocaleString();
        }
        /*21*/
        return null;
    }

    public void setGmtCreateString(String gmtCreateString) {
        this.gmtCreateString = gmtCreateString;
    }
}
