package com.alibaba.fastjson2.spring;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.webservlet.view.FastJsonJsonView;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FastJsonJsonViewUnitTest {
    @Test
    public void test_0() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();
        FastJsonConfig config = new FastJsonConfig();

        Map<String, Object> model = new HashMap<String, Object>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.setFastJsonConfig(config);
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

        assertNotNull(view.getFastJsonConfig());
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
        assertTrue(view.isExtractValueFromSingleKeyModel());

        view.setDisableCaching(true);
        view.render(Collections.singletonMap("abc", "cde"), request, response);
    }

    @Test
    public void test_jsonp() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();

        assertNotNull(view.getFastJsonConfig());
        view.setFastJsonConfig(new FastJsonConfig());
        view.setExtractValueFromSingleKeyModel(true);
        view.setDisableCaching(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("callback", "queryName");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(view.isExtractValueFromSingleKeyModel());

        view.render(Collections.singletonMap("abc", "cde中文"), request, response);
        String contentAsString = response.getContentAsString();
        int contentLength = response.getContentLength();

        assertEquals(contentLength, contentAsString.getBytes(view.getFastJsonConfig().getCharset().name()).length);
    }

    @Test
    public void test_jsonp_invalidParam() throws Exception {
        FastJsonJsonView view = new FastJsonJsonView();

        assertNotNull(view.getFastJsonConfig());
        view.setFastJsonConfig(new FastJsonConfig());
        view.setExtractValueFromSingleKeyModel(true);
        view.setDisableCaching(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("callback", "-methodName");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(view.isExtractValueFromSingleKeyModel());

        view.render(Collections.singletonMap("doesn't matter", Collections.singletonMap("abc", "cde中文")), request, response);
        String contentAsString = response.getContentAsString();
        assertTrue(contentAsString.startsWith("{\"abc\":\"cde中文\"}"));
    }
}
