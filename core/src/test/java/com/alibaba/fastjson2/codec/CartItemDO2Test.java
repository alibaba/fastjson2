package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.CartItemDO2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartItemDO2Test {
    private static List<CartItemDO2> newCartsItem() {
        List<CartItemDO2> list = new ArrayList();
        for (long i = 90000000000L; i < 90000000000L + 1000; i++) {
            CartItemDO2 cartItemDO2 = new CartItemDO2();
            cartItemDO2.setUserId(i);
            cartItemDO2.setAttributes(new HashMap());
            cartItemDO2.setCartId(i * 100);
            cartItemDO2.setCityCode(i * 12);
            cartItemDO2.setItemId(i * 3);
            cartItemDO2.setMainType(11);
            cartItemDO2.setQuantity(900);
            cartItemDO2.setSkuId(i * 5);
            cartItemDO2.setSubType(i * 6);
            cartItemDO2.setTpId(i * 7);
            cartItemDO2.setTrackId(String.valueOf(i * 8));
            list.add(cartItemDO2);
        }
        return list;
    }

    @Test
    public void testCartItem() throws Exception {
        int i = 111111111;
        CartItemDO2 cartItemDO2 = new CartItemDO2();
        cartItemDO2.setUserId(i);
        cartItemDO2.setAttributes(new HashMap());
        cartItemDO2.setCartId(i * 100);
        cartItemDO2.setCityCode(i * 12);
        cartItemDO2.setItemId(i * 3);
        cartItemDO2.setMainType(11);
        cartItemDO2.setQuantity(900);
        cartItemDO2.setSkuId(i * 5);
        cartItemDO2.setSubType(i * 6);
        cartItemDO2.setTpId(i * 7);
        cartItemDO2.setTrackId(String.valueOf(i * 8));

        byte[] bytes = JSONB.toBytes(cartItemDO2, JSONWriter.Feature.BeanToArray);
        System.out.println("bytes.length=" + bytes.length);

        {
            byte[] bytes2 = JSONB.toBytes(newCartsItem(), JSONWriter.Feature.BeanToArray);
            System.out.println("bytes.length=" + bytes2.length);
        }
    }

    private void f1() {
        int i = 111111111;
        CartItemDO2 cartItemDO2 = new CartItemDO2();
        cartItemDO2.setUserId(i);
        cartItemDO2.setAttributes(new HashMap());
        cartItemDO2.setCartId(i * 100);
        cartItemDO2.setCityCode(i * 12);
        cartItemDO2.setItemId(i * 3);
        cartItemDO2.setMainType(11);
        cartItemDO2.setQuantity(900);
        cartItemDO2.setSkuId(i * 5);
        cartItemDO2.setSubType(i * 6);
        cartItemDO2.setTpId(i * 7);
        cartItemDO2.setTrackId(String.valueOf(i * 8));

        byte[] bytes = JSONB.toBytes(cartItemDO2, JSONB.symbolTable("id",
                "cartId",
                "userId",
                "trackId",
                "itemId",
                "skuId",
                "quantity",
                "mainType",
                "tpId",
                "subType",
                "cityCode",
                "attributes"));
        System.out.println("bytes.length=" + bytes.length);
    }

    private void f2() {
//        byte[] bytes2 = JSONB.toBytes(newCartsItem(), JSONWriter.Feature.BeanToArray);
//        System.out.println("bytes.length=" + bytes2.length);

        byte[] bytes3 = JSONB.toBytes(
                newCartsItem(),
                JSONB.symbolTable("myId"),
                JSONWriter.Feature.BeanToArray
        );
        System.out.println("bytes3.length=" + bytes3.length);

//        System.out.println(JSON.toJSONString(newCartsItem(), JSONWriter.Feature.BeanToArray));
    }

    private void f1_f2() {
        f1();
        f2();
    }

    @Test
    public void testCartItem2() throws Exception {
        f1();
        f2();

        f1_f2();
    }
}
