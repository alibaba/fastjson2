package com.alibaba.fastjson2.example.solontest.config;


import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.solon.Fastjson2ActionExecutor;
import com.alibaba.fastjson2.support.solon.Fastjson2RenderFactory;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

/**
 * @author noear
 * @since 2024-10-01
 */
@Configuration
public class JsonConfigurer {

    @Bean
    public void fastjson2(Fastjson2ActionExecutor executor, Fastjson2RenderFactory render) {
//        executor.config().config(
//                JSONReader.Feature.FieldBased,
//                JSONReader.Feature.SupportArrayToBean);
//
//        render.addFeatures(
//                JSONWriter.Feature.WriteMapNullValue,
//                JSONWriter.Feature.PrettyFormat);
    }
}
