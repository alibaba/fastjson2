package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class Issue1744 {
    @Test
    public void test() {
        String str = "[{\"ActionType\":2,\"AlgInfo\":\"\",\"AuthorName\":\"瓜不瓜\",\"AuthorTags\":[\"日常文\"],\"BookId\":1036864783,\"BookIntro\":\"陆离凭借自己的努力和系统的帮助走上了人生巅峰。至于青梅？啥，我还有青梅竹马啊？\",\"BookName\":\"被青梅拒绝后，我获得了模拟器\",\"BookStatus\":\"连载\",\"CategoryId\":4,\"CategoryName\":\"都市\",\"Description\":\"所有人都认为陆离会和徐清在一起。毕竟这俩人郎才女貌，还从小认识。\\n可在散伙饭那天，徐清拒绝了陆离的表白。正在陆离怅然之际，突然听到耳边传来的提示音。\\n【恭喜您获\",\"Did\":\"1036864783\",\"Dt\":1,\"Ex2\":\"\",\"LastChapterId\":0,\"LastChapterName\":\"\",\"LastChapterTime\":0,\"Pos\":0,\"RankName\":\"\",\"ShowTime\":1692079200000,\"SubCategoryName\":\"都市生活\",\"Type\":0,\"WordCount\":\"58万\",\"day\":\"15\",\"fbAlg\":\"\",\"fbAppId\":0,\"fbId\":1036864783,\"fbType\":1,\"isReal\":true,\"isVip\":1,\"lastUpdateTimes\":\"\",\"month\":\"08\",\"recommendCol\":\"rengongtuijian\",\"recommendStyle\":0,\"sp\":\"{\\\"exposetime\\\":1692084881634,\\\"scene\\\":\\\"preface_with_editor\\\"}\",\"stability\":0,\"year\":\"2023\"}]";
        List list = JSON.parseObject(str, ArrayList.class);
        JSONObject object = (JSONObject) list.get(0);
        System.out.println(object.get("BookId"));
        System.out.println(JSON.toJSONString(list));
    }
}
