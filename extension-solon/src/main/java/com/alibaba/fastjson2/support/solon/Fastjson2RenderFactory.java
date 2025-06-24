package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.JSONWriter;
import org.noear.solon.core.handle.Render;
import org.noear.solon.serialization.StringSerializerRender;
import org.noear.solon.serialization.prop.JsonProps;
import org.noear.solon.serialization.prop.JsonPropsUtil;

/**
 * Json RenderFactory
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2024-10-01
 */
public class Fastjson2RenderFactory
        extends Fastjson2RenderFactoryBase {
    public Fastjson2RenderFactory(JsonProps jsonProps) {
        serializer.cfgSerializeFeatures(false, true,
                JSONWriter.Feature.BrowserCompatible);
        applyProps(jsonProps);
    }

    /**
     * Suffix or name mapping
     */
    @Override
    public String[] mappings() {
        return new String[]{"@json"};
    }

    /**
     * Create Render
     */
    @Override
    public Render create() {
        return new StringSerializerRender(false, serializer);
    }

    /**
     * Resetting features
     */
    public void setFeatures(JSONWriter.Feature... features) {
        serializer.cfgSerializeFeatures(true, true, features);
    }

    /**
     * Adding features
     */
    public void addFeatures(JSONWriter.Feature... features) {
        serializer.cfgSerializeFeatures(false, true, features);
    }

    /**
     * Removing features
     */
    public void removeFeatures(JSONWriter.Feature... features) {
        serializer.cfgSerializeFeatures(false, false, features);
    }

    protected void applyProps(JsonProps jsonProps) {
        if (jsonProps != null && jsonProps.dateAsTicks) {
            jsonProps.dateAsTicks = false;
            this.getSerializer().getSerializeConfig()
                    .setDateFormat("millis");
        }

        if (JsonPropsUtil.apply(this, jsonProps)) {
            boolean writeNulls = jsonProps.nullAsWriteable ||
                    jsonProps.nullNumberAsZero ||
                    jsonProps.nullArrayAsEmpty ||
                    jsonProps.nullBoolAsFalse ||
                    jsonProps.nullStringAsEmpty;

            if (jsonProps.nullStringAsEmpty) {
                this.addFeatures(JSONWriter.Feature.WriteNullStringAsEmpty);
            }

            if (jsonProps.nullBoolAsFalse) {
                this.addFeatures(JSONWriter.Feature.WriteNullBooleanAsFalse);
            }

            if (jsonProps.nullNumberAsZero) {
                this.addFeatures(JSONWriter.Feature.WriteNullNumberAsZero);
            }

            if (jsonProps.boolAsInt) {
                this.addFeatures(JSONWriter.Feature.WriteBooleanAsNumber);
            }

            if (jsonProps.longAsString) {
                this.addFeatures(JSONWriter.Feature.WriteLongAsString);
            }

            if (jsonProps.nullArrayAsEmpty) {
                this.addFeatures(JSONWriter.Feature.WriteNullListAsEmpty);
            }

            if (jsonProps.enumAsName) {
                this.addFeatures(JSONWriter.Feature.WriteEnumsUsingName);
            }

            if (writeNulls) {
                this.addFeatures(JSONWriter.Feature.WriteNulls);
            }
        }
    }
}
