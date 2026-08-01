package com.alibaba.fastjson2.support.solon.integration;

import com.alibaba.fastjson2.support.solon.Fastjson2EntityConverter;
import com.alibaba.fastjson2.support.solon.Fastjson2StringSerializer;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.serialization.SerializerNames;
import org.noear.solon.serialization.prop.JsonProps;

/**
 * Fastjson2 for solon extension integration plugin
 *
 * @author noear
 * */
public class Fastjson2Plugin
        implements Plugin {
    @Override
    public void start(AppContext context) {
        JsonProps jsonProps = JsonProps.create(context);

        //::serializer
        Fastjson2StringSerializer serializer = new Fastjson2StringSerializer(jsonProps);
        context.wrapAndPut(Fastjson2StringSerializer.class, serializer); //用于扩展
        context.app().serializers().register(SerializerNames.AT_JSON, serializer);

        //::entityConverter
        Fastjson2EntityConverter entityConverter = new Fastjson2EntityConverter(serializer);
        context.wrapAndPut(Fastjson2EntityConverter.class, entityConverter); //用于扩展
        context.app().chains().addEntityConverter(entityConverter);
    }
}
