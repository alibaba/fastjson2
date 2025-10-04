package com.alibaba.fastjson2.example.solontest.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.solon.Fastjson2StringSerializer;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

/**
 * @author noear
 * @since 2024-10-01
 */
@Configuration
public class JsonConfigurer {
    @Bean
    public void fastjson2(Fastjson2StringSerializer serializer) {
//        serializer.getDeserializeConfig().addFeatures(
//                JSONReader.Feature.FieldBased,
//                JSONReader.Feature.SupportArrayToBean);
//
//        serializer.getSerializeConfig().addFeatures(
//                JSONWriter.Feature.WriteMapNullValue,
//                JSONWriter.Feature.PrettyFormat);
    }
}
