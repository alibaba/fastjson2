package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

/**
 * @author 张治保
 * @since 2024/2/5
 */
public class Issue2230 {
    @Test
    void  test(){
        JSONObject dfDtoJson = new JSONObject();
        dfDtoJson.put(
                "componentList",
                new JSONArray().fluentAdd(
                        new JSONObject().fluentPut(
                                "props",
                                new JSONObject()
                                        .fluentPut("compTypeName", "test11")
                                        .fluentPut("name", "test22")
                        )
                )
        );
        DynFormDto newDfDto = dfDtoJson.toJavaObject(DynFormDto.class);
        System.out.println(newDfDto.getComponentList().get(0).getProps());
    }
    @Data
    static class DynFormDto {
        private List<DynFormComponentDto> componentList;
        private String notes;
    }
    @Data
    static class DynFormComponentDto implements Serializable {
        private JSONObject props; // 组件完整配置
        // 这个方法会导致props字段反序列化结果出现{"h":{***}}结构
        public DynFormComponentDto props(IFormComponent comp) {
            this.props = (JSONObject) JSONObject.from(comp, JSONWriter.Feature.FieldBased);
            return this;
        }
    }
    interface  IFormComponent {
        String getCompTypeName();
        String getName();
    }
}
