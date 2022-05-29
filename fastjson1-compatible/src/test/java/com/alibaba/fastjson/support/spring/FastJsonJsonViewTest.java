package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FastJsonJsonViewTest {
    @Test
    public void test_0() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();

        Map<String, Object> model = new HashMap<String, Object>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(model, request, response);

        view.setRenderedAttributes(null);

        view.render(model, request, response);

        view.setUpdateContentLength(true);
        view.render(model, request, response);

        view.render(Collections.singletonMap("abc", "cde"), request, response);

        view.setDisableCaching(false);
        view.setUpdateContentLength(false);
        view.render(model, request, response);

        view.setRenderedAttributes(new HashSet<String>(Collections.singletonList("abc")));
        view.render(Collections.singletonMap("abc", "cde"), request, response);
    }

    @Test
    public void test_1() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();

        Assertions.assertNotNull(view.getFastJsonConfig());
        view.setFastJsonConfig(new FastJsonConfig());

        Map<String, Object> model = new HashMap<String, Object>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(model, request, response);

        view.setRenderedAttributes(null);
        view.render(model, request, response);

        view.setUpdateContentLength(true);
        view.render(model, request, response);

        view.setExtractValueFromSingleKeyModel(true);
        Assertions.assertEquals(true, view.isExtractValueFromSingleKeyModel());

        view.setDisableCaching(true);
        view.render(Collections.singletonMap("abc", "cde"), request, response);
    }

    @Test
    public void test_jsonp() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();

        Assertions.assertNotNull(view.getFastJsonConfig());
        view.setFastJsonConfig(new FastJsonConfig());
        view.setExtractValueFromSingleKeyModel(true);
        view.setDisableCaching(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("callback", "queryName");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Assertions.assertEquals(true, view.isExtractValueFromSingleKeyModel());

        view.render(Collections.singletonMap("abc", "cde中文"), request, response);
        String contentAsString = response.getContentAsString();
        int contentLength = response.getContentLength();

        Assertions.assertEquals(contentLength, contentAsString.getBytes(view.getFastJsonConfig().getCharset().name()).length);
    }

    @Test
    public void test_jsonp_invalidParam() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();

        Assertions.assertNotNull(view.getFastJsonConfig());
        view.setFastJsonConfig(new FastJsonConfig());
        view.setExtractValueFromSingleKeyModel(true);
        view.setDisableCaching(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("callback", "-methodName");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Assertions.assertEquals(true, view.isExtractValueFromSingleKeyModel());

        view.render(Collections.singletonMap("doesn't matter", Collections.singletonMap("abc", "cde中文")), request, response);
        String contentAsString = response.getContentAsString();
        Assertions.assertTrue(contentAsString.startsWith("{\"abc\":\"cde中文\"}"));
    }

    private SerializeFilter serializeFilter = new ValueFilter() {
        public Object process(Object object, String name, Object value) {
            if (value == null) {
                return "";
            }
            if (value instanceof Number) {
                return String.valueOf(value);
            }
            return value;
        }
    };
}
