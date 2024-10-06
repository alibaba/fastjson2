package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.JSONWriter;
import org.noear.solon.core.handle.Render;
import org.noear.solon.serialization.StringSerializerRender;

/**
 * Json typed RenderFactory (Typically used with RPC)
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2024-10-01
 */
public class Fastjson2RenderTypedFactory
        extends Fastjson2RenderFactoryBase {
    public Fastjson2RenderTypedFactory() {
        serializer.cfgSerializeFeatures(false, true,
                JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.ReferenceDetection
        );
    }

    /**
     * Suffix or name mapping
     */
    @Override
    public String[] mappings() {
        return new String[]{"@type_json"};
    }

    /**
     * Create Render
     */
    @Override
    public Render create() {
        return new StringSerializerRender(true, serializer);
    }
}
