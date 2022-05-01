package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Differ;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Currency;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest11 {
    @Test
    public void test_0() {
        String json = "{\"sesameBlock_1\":{\"ref\":\"f813053\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"},\"itemInfo_fbac305d6d2c580ede612097e58e9a42\":{\"ref\":\"f36c1fb\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"bizCode\":\"ali.china.taobao.game.smc\"}},\"type\":\"dinamicx$546$buyitem\",\"fields\":{\"skuLevel\":[],\"timeLimit\":\"\",\"price\":\"￥45.00\",\"icon\":\"//img.daily.taobaocdn.net/imgextra/i3/656082759/O1CN013wKARk1WFfTb0WCAi_!!656082759.jpg\",\"count\":\"x1\",\"weight\":\"\",\"disabled\":\"false\",\"title\":\"三国杀帐号三万元宝号盒子号三万元宝号开局号移动版\"}},\"alicomItemBlock_fbac305d6d2c580ede612097e58e9a42\":{\"ref\":\"dab643e\",\"type\":\"block$null$emptyBlock\",\"fields\":{}},\"cuntaoBlock_1\":{\"ref\":\"6c30eea\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"},\"storeCollectBlock_1_storeCollectBlock\":{\"ref\":\"75db316\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"},\"gameBizOrder_fbac305d6d2c580ede612097e58e9a42\":{\"ref\":\"4fd6e58\",\"type\":\"block$null$emptyBlock\",\"fields\":{}},\"anonymous_1\":{\"ref\":\"c1973e0\",\"submit\":true,\"type\":\"dinamicx$561$buyprotocolcheckbox\",\"fields\":{\"title\":\"匿名购买\",\"isChecked\":true},\"events\":{\"itemClick\":[{\"type\":\"select\",\"fields\":{\"isChecked\":\"true\"}}]}},\"topReminds_1\":{\"ref\":\"d15acbf\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"},\"stickyTopBlock_1\":{\"ref\":\"g83292\",\"position\":\"stickyTop\",\"type\":\"block$null$emptyBlock\",\"fields\":{}},\"memo_97cb3b87cd6c728a190c1af6861f4715\":{\"ref\":\"b642b1e\",\"submit\":true,\"type\":\"dinamicx$554$buyinput\",\"fields\":{\"placeholder\":\"选填,请先和商家协商一致\",\"title\":\"订单备注\",\"value\":\"\"},\"events\":{\"itemClick\":[{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2101\",\"arg1\":\"Page_ConfirmOrder_Button-beizhu\",\"page\":\"Page_Order\"}}],\"exposureItem\":[{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2201\",\"arg1\":\"Page_ConfirmOrder_Button-beizhu\",\"page\":\"Page_Order\"}}],\"onFinish\":[{\"type\":\"input\",\"fields\":{\"value\":\"\"}}]}},\"orderPay_97cb3b87cd6c728a190c1af6861f4715\":{\"ref\":\"c99100a\",\"type\":\"dinamicx$560$buysubtotal\",\"fields\":{\"price\":\"￥45.00\",\"title\":\"小计: \",\"desc\":\"共1件\"}},\"debug_1\":{\"ref\":\"f5a9540\",\"type\":\"dinamicx$460$buylabel\",\"fields\":{\"richText\":[{\"text\":\"测试环境信息,建议开启天启对比忽略\\n\"},{\"text\":\"host=buy2011122042010.nt12/11.122.42.10\\n\"},{\"text\":\"platformType=WIRELESS_3_0\\n\"},{\"text\":\"pageType=general\\n\"},{\"text\":\"ultron2VersionCode=buy2_20211227095825794_buy2_2021-12-29_167837\\n\"},{\"text\":\"traceId=0b7a2a0a16413655049901114d182e\\n\"},{\"text\":\"userId=2208783519682\\n\"},{\"text\":\"ultronDebugModel=true\"}]}},\"confirmOrder_1\":{\"ref\":\"8318d7a\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"pageType\":\"GENERAL\",\"umid\":\"\",\"__ex_params__\":\"{\\\"tradeProtocolFeatures\\\":\\\"5\\\",\\\"coupon\\\":\\\"true\\\",\\\"alipayCashierParams\\\":\\\"{\\\\\\\"extinfo\\\\\\\":\\\\\\\"{}\\\\\\\",\\\\\\\"has_alipay\\\\\\\":1,\\\\\\\"pa\\\\\\\":\\\\\\\"(com.taobao.taobao;10.7.10)\\\\\\\",\\\\\\\"tid\\\\\\\":\\\\\\\"f41ba2b33152bc643c480636897defdaa04b9275da5691c091776ac787915f5e\\\\\\\",\\\\\\\"ua\\\\\\\":\\\\\\\"10.8.53.4(a 10;6;RTGKVlNCm9;000000000000000;000000000000000;1751f8fe7339059;1751f8fe7335425;LTE;02:00:00:00:00:00;0;HUAWEI;VOG-AL00)(1)(Xy5zZhecUpHRhcaLOMRiWUDgB5NUTJaA3GEnTt3jqZg3D/obLfgnfgEB)\\\\\\\",\\\\\\\"utdid\\\\\\\":\\\\\\\"XweN+TtBPacDAM3IBcm4vXJU\\\\\\\"}\\\",\\\"websiteLanguage\\\":\\\"zh_CN_#Hans\\\",\\\"apdidToken\\\":\\\"Xy5zZhecUpHRhcaLOMRiWUDgB5NUTJaA3GEnTt3jqZg3D/obLfgnfgEB\\\",\\\"coVersion\\\":\\\"2.0\\\",\\\"addressId\\\":\\\"14682277874\\\",\\\"areaId\\\":\\\"321322\\\",\\\"EXCLUDE_BY_NEWTON_GRAY_KEY\\\":true,\\\"lbsInfo\\\":\\\"{\\\\\\\"areaDivisionCode\\\\\\\":\\\\\\\"320115\\\\\\\",\\\\\\\"cityDivisionCode\\\\\\\":\\\\\\\"320100\\\\\\\",\\\\\\\"lat\\\\\\\":\\\\\\\"31.908242\\\\\\\",\\\\\\\"lng\\\\\\\":\\\\\\\"118.830331\\\\\\\",\\\\\\\"provinceDivisionCode\\\\\\\":\\\\\\\"320000\\\\\\\",\\\\\\\"townDivisionCode\\\\\\\":\\\\\\\"320115002\\\\\\\"}\\\",\\\"agencyPayRouter\\\":\\\"1\\\",\\\"tradeTemplates\\\":\\\"[]\\\",\\\"umfVersions\\\":\\\"{\\\\\\\"features\\\\\\\":{},\\\\\\\"version\\\\\\\":\\\\\\\"0.1.0\\\\\\\"}\\\",\\\"detailAddressId\\\":\\\"14682277874\\\",\\\"bcflsrc\\\":\\\"1012_Initiactive\\\"}\",\"joinId\":\"fbac305d6d2c580ede612097e58e9a42\"}},\"type\":\"block$null$emptyBlock\",\"fields\":{}},\"agencyPayV2_1\":{\"ref\":\"g39dd4\",\"submit\":true,\"type\":\"dinamicx$568$buypayforanother\",\"fields\":{\"icon\":\"https://img.alicdn.com/imgextra/i4/O1CN01U4WSFq1cwcGj2zGOS_!!6000000003665-2-tps-66-66.png\",\"title\":\"找朋友帮忙付\",\"isChecked\":\"false\"},\"events\":{\"itemClick\":[{\"type\":\"select\",\"fields\":{\"isChecked\":\"false\"}},{\"type\":\"userTrack\",\"fields\":{\"args\":{\"agcV2Checked\":\"false\",\"isConfirm2_0\":\"false\"},\"eventId\":\"2101\",\"arg1\":\"Page_ConfirmOrder-agencyPayV2\",\"page\":\"Page_Order\"}}],\"exposureItem\":[{\"type\":\"userTrack\",\"fields\":{\"args\":{\"$ref\":\"$.agencyPayV2\\\\_1.events.itemClick[1].fields.args\"},\"eventId\":\"2201\",\"arg1\":\"Page_ConfirmOrder-agencyPayV2\",\"page\":\"Page_Order\"}}]}},\"RoleInputCtrl_fbac305d6d2c580ede612097e58e9a42_RoleInputCtrl\":{\"ref\":\"52d7efc\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"gameBizCode\":\"ali.china.taobao.game.smc\"}},\"type\":\"dinamicx$554$buyinput\",\"fields\":{\"placeholder\":\"请输入角色\",\"title\":\"游戏角色:\"},\"events\":{\"onFinish\":[{\"type\":\"input\",\"fields\":{}}]},\"validate\":{\"msg\":[\"游戏角色必填，100字以内\"],\"regex\":[\"^(?!undefined)[^\\\\s]{1,100}$\"],\"fields\":[\"value\"]}},\"submitBlock_1\":{\"ref\":\"0bb8011\",\"position\":\"footer\",\"type\":\"block$null$emptyBlock\",\"fields\":{}},\"addressBlock_1\":{\"ref\":\"2e04132\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"},\"GameAreaServerInputCtrl_fbac305d6d2c580ede612097e58e9a42_GameAreaServerInputCtrl\":{\"ref\":\"3a2aaa8\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"gameBizCode\":\"ali.china.taobao.game.smc\"}},\"type\":\"dinamicx$554$buyinput\",\"fields\":{\"placeholder\":\"请输入区服\",\"title\":\"区/服:\"},\"events\":{\"onFinish\":[{\"type\":\"input\",\"fields\":{}}]},\"validate\":{\"msg\":[\"游戏区服必填，100字以内\"],\"regex\":[\"^(?!undefined)[^\\\\s]{1,100}$\"],\"fields\":[\"value\"]}},\"quantity_fbac305d6d2c580ede612097e58e9a42\":{\"ref\":\"8f2af9f\",\"type\":\"dinamicx$580$buyquantity\",\"fields\":{\"min\":\"1\",\"quantity\":\"1\",\"max\":\"66621\",\"step\":\"1\",\"title\":\"购买数量\"},\"events\":{\"changeQuantity\":[{\"type\":\"changeQuantity\",\"fields\":{\"quantity\":\"1\",\"min\":\"1\",\"max\":\"66621\",\"step\":\"1\"}},{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2101\",\"page\":\"Page_Order\"}}]}},\"confirmPromotionAndService_1\":{\"ref\":\"73cfc13\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"},\"submitOrder_1\":{\"ref\":\"40aa9e9\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"showPrice\":\"43.65\",\"submitOrderType\":\"UNITY\"}},\"type\":\"dinamicx$475$buysubmit\",\"fields\":{\"isShowFamilyPayBtn\":\"false\",\"price\":\"￥43.65\",\"priceTitle\":\"合计:\",\"count\":\"共1件，\",\"useSpecialPay\":\"false\",\"payBtn\":{\"enable\":true,\"title\":\"提交订单\"},\"descCss\":{\"color\":\"#333333\"},\"desc\":\"\"},\"events\":{\"itemClick\":[{\"type\":\"submit\",\"fields\":{}},{\"type\":\"userTrack\",\"fields\":{\"args\":{\"totalPay\":\"￥43.65\",\"itemIds\":\"665084984847-0-656082759-ali.china.taobao.game.smc\",\"hasService\":\"false\"},\"eventId\":\"2101\",\"arg1\":\"Page_ConfirmOrder_buyGlobalUtData\",\"page\":\"Page_Order\"}}],\"exposureItem\":[{\"type\":\"userTrack\",\"fields\":{\"args\":{\"totalPay\":\"￥43.65\",\"itemIds\":\"665084984847-0-656082759-ali.china.taobao.game.smc\",\"hasService\":\"false\"},\"eventId\":\"2201\",\"arg1\":\"Page_ConfirmOrder_buyGlobalUtData\",\"page\":\"Page_Order\"}}]}},\"coupon_3\":{\"ref\":\"eee6072\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"totalValue\":\"不可用\",\"couponOptTag\":\"1\",\"couponIds\":\"[\\\"0\\\"]\",\"isSupportBizCouponCO\":\"true\",\"promotionDetailTitle\":\"详情\",\"value\":\"0\"}},\"type\":\"dinamicx$550$buyimageselect\",\"fields\":{\"confirm\":\"完成\",\"componentType\":\"dinamicx$3122$buypopupcheckbox2simple\",\"components\":[{\"title\":\"不可用（展示最多20个红包）\",\"disabled\":\"true\",\"id\":\"false\",\"checkboxVisible\":\"gone\"},{\"priceCss\":{\"color\":\"#999999\"},\"titleCss\":{\"color\":\"#999999\"},\"title\":\"省钱卡卡费抵扣红包\",\"isCardStyle\":false,\"subTitle\":\"订单中有商品不支持此红包\",\"price\":\"￥6.10\",\"disabled\":\"true\",\"checkboxVisible\":\"gone\",\"exttitle\":\"使用门槛：满6.2元使用\"},{\"priceCss\":{\"color\":\"#999999\"},\"titleCss\":{\"color\":\"#999999\"},\"title\":\"省钱卡卡费抵扣红包\",\"isCardStyle\":false,\"subTitle\":\"订单中有商品不支持此红包\",\"price\":\"￥6.10\",\"disabled\":\"true\",\"checkboxVisible\":\"gone\",\"exttitle\":\"使用门槛：满6.2元使用\"}],\"priceCss\":{},\"price\":\"不可用\",\"dialogTitle\":\"红包详情\",\"iconUrl\":\"https://gw.alicdn.com/tfs/TB166HQiMHqK1RjSZFEXXcGMXXa-74-95.png\",\"asSelect\":{\"selectedIds\":[\"false\"]},\"title\":\"红包\"},\"events\":{\"itemClick\":[{\"type\":\"openSimplePopup\",\"fields\":{}},{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2101\",\"arg1\":\"Page_ConfirmOrder_Button-hongbao\",\"page\":\"Page_Order\"}}],\"exposureItem\":[{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2201\",\"arg1\":\"Page_ConfirmOrder_Button-hongbao\",\"page\":\"Page_Order\"}}]}},\"orderInfo_97cb3b87cd6c728a190c1af6861f4715\":{\"ref\":\"2c311f0\",\"type\":\"dinamicx$473$buyimagetext\",\"fields\":{\"iconUrl\":\"//img.alicdn.com/tps/i2/TB1wopUHVXXXXXyXpXXAAT2HVXX-63-63.png\",\"title\":\"心悦网游店\"}},\"item_fbac305d6d2c580ede612097e58e9a42\":{\"ref\":\"360f46f\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"valid\":\"true\",\"itemId\":\"665084984847\",\"bizCode\":\"ali.china.taobao.game.smc\",\"cartId\":\"0\",\"shoppingOrderId\":\"0\",\"villagerId\":\"0\",\"skuId\":\"0\"}},\"type\":\"block$null$emptyBlock\",\"fields\":{}},\"tbgold_1\":{\"ref\":\"ea834e6\",\"submit\":true,\"hidden\":{\"extensionMap\":{\"sellerAvailablePoint\":\"-1\",\"totalPoint\":\"300\",\"usePoint\":\"135\",\"idValue\":\"uppAcrossPromotion-123456_11111-123\",\"selected\":\"true\"}},\"type\":\"dinamicx$568$buypayforanother\",\"fields\":{\"icon\":\"https://gw.alicdn.com/tfs/TB1x9ULiCslXu8jSZFuXXXg7FXa-72-72.png\",\"title\":\"可用135淘金币抵用1.35元\",\"isChecked\":\"true\"},\"events\":{\"itemClick\":[{\"type\":\"select\",\"fields\":{\"isChecked\":\"true\"}},{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2101\",\"arg1\":\"Page_ConfirmOrder_Button-taojinbi\",\"page\":\"Page_Order\"}}],\"exposureItem\":[{\"type\":\"userTrack\",\"fields\":{\"eventId\":\"2201\",\"arg1\":\"Page_ConfirmOrder_Button-taojinbi\",\"page\":\"Page_Order\"}}]}},\"order_97cb3b87cd6c728a190c1af6861f4715\":{\"ref\":\"a9dc6bd\",\"type\":\"block$null$emptyBlock\",\"fields\":{},\"cardGroup\":\"true\"}}";

        com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSONObject.parseObject(json);

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        //System.out.println(object2);

        Differ.diff(object, object2);
    }

    @Test
    public void test_1() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        object.put("val", new ArrayList<>());
        object.put("val1", object.get("val"));

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertSame(object2.get("val"), object2.get("val1"));
    }

    @Test
    public void test_currency() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        object.put("val", java.util.Currency.getInstance("CNY"));
        object.put("val1", object.get("val"));

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertSame(object2.get("val"), object2.get("val1"));
        assertEquals(java.util.Currency.class, object2.get("val").getClass());
    }

    @Test
    public void test_currency_2() {
        Currency object = Currency.getInstance("CNY");

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Currency object2 = (Currency) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(object, object2);
    }

    @Test
    public void test_enum_0() {
        Object[] array = new Object[1];
        array[0] = TimeUnit.DAYS;

        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Object[] array2 = (Object[]) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(array.length, array2.length);
        assertEquals(array[0], array2[0]);
    }

    @Test
    public void test_enum_1() {
        Bean bean = new Bean();
        bean.unit = TimeUnit.DAYS;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(bean.unit, bean2.unit);
    }

    static class Bean {
        public TimeUnit unit;
    }
}
