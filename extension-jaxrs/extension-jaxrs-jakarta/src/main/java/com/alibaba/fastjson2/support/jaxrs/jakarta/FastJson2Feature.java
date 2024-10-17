package com.alibaba.fastjson2.support.jaxrs.jakarta;

import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
/**
 *  FastJson2Feature
 *
 * @author 张治保
 * @since 2024/10/16
 * @see com.alibaba.fastjson.support.jaxrs.FastJsonFeature
 * @see Feature
 */
public class FastJson2Feature
        implements Feature {
    private static final String JSON_FEATURE = FastJson2Feature.class.getSimpleName();

    @Override
    public boolean configure(final FeatureContext context) {
        try {
            final Configuration config = context.getConfiguration();

            final String jsonFeature = CommonProperties.getValue(
                    config.getProperties(),
                    config.getRuntimeType(),
                    InternalProperties.JSON_FEATURE,
                    JSON_FEATURE,
                    String.class
            );

            // Other JSON providers registered.
            if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
                return false;
            }

            // Disable other JSON providers.
            context.property(
                    PropertiesHelper.getPropertyNameForRuntime(
                            InternalProperties.JSON_FEATURE,
                            config.getRuntimeType()
                    ),
                    JSON_FEATURE
            );

            // Register FastJson.
            if (!config.isRegistered(FastJson2Provider.class)) {
                context.register(FastJson2Provider.class, MessageBodyReader.class, MessageBodyWriter.class);
            }
        } catch (NoSuchMethodError e) {
            // skip
        }

        return true;
    }
}
