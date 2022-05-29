package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by kimmking on 02/07/2017.
 */
public class Issue1303 {
    @Test
    public void test_for_issue() {
        String jsonString = "[{\"author\":{\"__type\":\"Pointer\",\"className\":\"_User\",\"objectId\":\"a876c49c18\"},\"createdAt\":\"2017-07-02 20:06:13\",\"imgurl\":\"https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=11075891,34401011&fm=117&gp=0.jpg\",\"name\":\"衣架\",\"objectId\":\"029d5493cd\",\"prices\":\"1\",\"updatedAt\":\"2017-07-02 20:06:13\"}]";
        JSONArray jsonArray = JSON.parseArray(jsonString);
        //jsonArray = new JSONArray(jsonArray);//这一句打开也一样是正确的
        double total = 0;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            assertEquals("衣架", object.getString("name"));
            total = total + Double.valueOf(object.getString("prices"));
        }
        assertEquals(1.0d, total);
    }
}
