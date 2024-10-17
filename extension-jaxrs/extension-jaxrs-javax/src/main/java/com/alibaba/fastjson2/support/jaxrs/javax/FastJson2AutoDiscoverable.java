package com.alibaba.fastjson2.support.jaxrs.javax;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

/**
 * <p>Title: FastJsonAutoDiscoverable</p>
 * <p>Description: FastJsonAutoDiscoverable</p>
 *
 * @author 张治保
 * @since 2024/10/16
 * @see com.alibaba.fastjson.support.jaxrs.FastJsonAutoDiscoverable
 * @see AutoDiscoverable
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY - 1)
public class FastJson2AutoDiscoverable
        implements AutoDiscoverable {
    public static final String FASTJSON_AUTO_DISCOVERABLE = "fastjson.auto.discoverable";
    public static volatile boolean autoDiscover;

    static {
        autoDiscover = Boolean.parseBoolean(
                System.getProperty(FASTJSON_AUTO_DISCOVERABLE, Boolean.TRUE.toString()));
    }

    @Override
    public void configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        // Register FastJson.
        if (!config.isRegistered(FastJson2Feature.class) && autoDiscover) {
            context.register(FastJson2Feature.class);
        }
    }
}
