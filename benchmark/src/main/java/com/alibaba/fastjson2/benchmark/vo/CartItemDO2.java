package com.alibaba.fastjson2.benchmark.vo;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CartItemDO2
        implements Serializable {
    private static final long serialVersionUID = -3291877592429392571L;
    private String id = "myId";
    private long cartId;
    @JSONField(format = "symbol")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public long getCartId() {
        return cartId;
    }
    public void setCartId(long cartId) {
        this.cartId = cartId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getTrackId() {
        return trackId;
    }
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }
    public long getItemId() {
        return itemId;
    }
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
    public long getSkuId() {
        return skuId;
    }
    public void setSkuId(long skuId) {
        this.skuId = skuId;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getMainType() {
        return mainType;
    }
    public void setMainType(int mainType) {
        this.mainType = mainType;
    }
    public long getTpId() {
        return tpId;
    }

    public void setTpId(long tpId) {
        this.tpId = tpId;
    }

    public long getSubType() {
        return subType;
    }

    public void setSubType(long subType) {
        this.subType = subType;
    }

    public long getCityCode() {
        return cityCode;
    }

    public void setCityCode(long cityCode) {
        this.cityCode = cityCode;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    private long userId;
    private String trackId = "myTrackId";
    private long itemId;
    private long skuId;
    private int quantity;
    private int mainType;
    private long tpId;
    private long subType;
    public long cityCode;
    private Map<String, String> attributes = new HashMap();
}
