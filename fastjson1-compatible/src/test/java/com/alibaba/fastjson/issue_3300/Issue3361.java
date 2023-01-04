package com.alibaba.fastjson.issue_3300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class Issue3361 {
    private static String ORIGIN_JSON_DEFAULT_DATE_FORMAT;

    @BeforeEach
    public void setUp() throws Exception {
        ORIGIN_JSON_DEFAULT_DATE_FORMAT = JSON.DEFFAULT_DATE_FORMAT;
    }

    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.setOldDate(new Date(1667920430928L));
        assertEquals("{\"oldDate\":1667920430928}", JSON.toJSONString(model));

        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(SerializerFeature.WriteMapNullValue);
        config.setWriteContentLength(false);
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS";
        config.setDateFormat(JSON.DEFFAULT_DATE_FORMAT);
        String string = JSON.toJSONString(model,
                config.getSerializeConfig(),
                config.getSerializeFilters(),
                config.getDateFormat(),
                JSON.DEFAULT_GENERATE_FEATURE,
                config.getSerializerFeatures());
        assertEquals("{\"oldDate\":\"2022-11-08T23:13:50.928000000\"}", string);

        Model model2 = JSON.parseObject(string, Model.class);
        assertEquals("{\"oldDate\":1668848430000}", JSON.toJSONString(model2));

        Model model3 = JSON.parseObject(string, new TypeReference<Model>() {
        }.getType());
        assertEquals("{\"oldDate\":1668848430000}", JSON.toJSONString(model3));
    }

    @AfterEach
    public void tearDown() throws Exception {
        JSON.DEFFAULT_DATE_FORMAT = ORIGIN_JSON_DEFAULT_DATE_FORMAT;
    }

    @Getter
    @Setter
    @ToString
    public static class Model {
        private Date oldDate;
    }
}
