package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.JSONWriter;
import org.noear.solon.core.handle.Render;
import org.noear.solon.serialization.StringSerializerRender;

/**
 * Json 类型化渲染器工厂（一般用于 RPC）
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2024-10-01
 */
public class Fastjson2RenderTypedFactory extends Fastjson2RenderFactoryBase {

    public Fastjson2RenderTypedFactory() {
        serializer.cfgSerializeFeatures(false, true,
                JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.ReferenceDetection
        );
    }

    /**
     * 后缀或名字映射
     */
    @Override
    public String[] mappings() {
        return new String[]{"@type_json"};
    }

    /**
     * 创建
     */
    @Override
    public Render create() {
        return new StringSerializerRender(true, serializer);
    }
}
