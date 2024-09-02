package org.apache.dubbo.jsonb.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 用户实体
 */
public class UserDTO
        implements Serializable {
    private static final long serialVersionUID = -7661184766955519527L;

    private String name;

    private BigDecimal deposit;

    @JSONField(format = "yyyy-MM-dd")
    private LocalDate birthday;

    @JSONField(format = "yyyy-MM-dd")
    private LocalDateTime actDate;

    private LocalDateTime createDate;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDateTime getActDate() {
        return actDate;
    }

    public void setActDate(LocalDateTime actDate) {
        this.actDate = actDate;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(LocalDateTime modifyDate) {
        this.modifyDate = modifyDate;
    }
}
