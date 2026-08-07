package com.alibaba.fastjson.support.jaxrs;

import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.*;

public class FastJsonProviderTest {
    @Test
    public void test() throws Exception {
        FastJsonProvider provider = new FastJsonProvider();
        assertNotNull(provider.getFastJsonConfig());
        assertTrue(provider.hasMatchingMediaType(MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.hasMatchingMediaType(MediaType.MULTIPART_FORM_DATA_TYPE));
        assertFalse(provider.hasMatchingMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertFalse(provider.hasMatchingMediaType(MediaType.TEXT_PLAIN_TYPE));
        assertTrue(provider.hasMatchingMediaType(null));
        assertEquals(-1, provider.getSize(null, null, null, null, null));
        assertFalse(provider.isReadable(null, null, null, null));

        assertNull(provider.readFrom(null, null, null, null, null, null));

        provider.setFeatures(SerializerFeature.BeanToArray);
        assertNotNull(provider.getFeatures());
        assertNotNull(provider.getFilters());
        provider.setFilters(new SerializeFilter[0]);
        assertNotNull(provider.getDateFormat());
        assertNotNull(provider.getCharset());
    }
}
