package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3611 {
    @Test
    public void test() {
        String str = "{\"二级指标\":\"一、主要经济指标\",\"核验扣分\":0,\"任务分配\":\"测试分解具体任务修改\",\"目标类型\":\"发展目标\",\"行政区划编码\":\"000000000000\",\"目标要求和评分标准\":\"A组其他6个设区市总量和增速分别占40%和60%的权重，B组7个设区市（xx、xx、xx、xx、xx、xx、xx）总量和增速分别占20%和80%的权重。计分公式：{[（某市变量-组内最小值）÷（组内最大值-组内最小值）]×0.4+0.6}×权重×分值\",\"完成标志\":\"未完成\",\"SZ_责任科室及联系人\":\"测试\",\"分解ID\":\"b649a98655a911f0fjw4e558814863f4\",\"SZ_是否分解给区县\":\"是\",\"分值\":5,\"得分值\":0}";
        JSONObject jsonObject = JSON.parseObject(str);
        String str1 = jsonObject.toJSONString();
        JSONObject jsonObject1 = JSON.parseObject(str1);
        assertEquals(jsonObject, jsonObject1);
    }
}
