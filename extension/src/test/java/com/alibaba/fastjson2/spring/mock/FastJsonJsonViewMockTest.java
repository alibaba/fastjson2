package com.alibaba.fastjson2.spring.mock;

import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.alibaba.fastjson2.support.spring.webservlet.view.FastJsonJsonView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class FastJsonJsonViewMockTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac) //
                .addFilter(new CharacterEncodingFilter("UTF-8", true)) // 设置服务器端返回的字符集为：UTF-8
                .build();
    }

    @Test
    public void test() throws Exception {
        mockMvc.perform(
                (get("/fastjson/mocktest").characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                )).andExpect(status().isOk()).andDo(print());
    }

    @Controller
    @RequestMapping("fastjson")
    public static class BeanController {
        @RequestMapping(value = "/mocktest", method = RequestMethod.GET)

        @ResponseBody
        public ModelAndView test7() {
            AuthIdentityRequest authRequest = new AuthIdentityRequest();
            authRequest.setAppId("cert01");
            authRequest.setUserId(2307643);
            authRequest.setIdNumber("34324324234234");
            authRequest.setRealName("victorzeng");
            authRequest.setBusinessLine("");
            authRequest.setIgnoreIdNumberRepeat(false);
            authRequest.setOffline(false);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("message", authRequest);
            modelAndView.addObject("title", "testPage");
            modelAndView.setViewName("test");

            return modelAndView;
        }
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.mock")
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE + 1)
    @EnableWebMvc
    public static class WebMvcConfig
            implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
            converters.add(converter);
        }

        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            FastJsonJsonView fastJsonJsonView = new FastJsonJsonView();
            registry.enableContentNegotiation(fastJsonJsonView);
        }
    }

    static class AuthIdentityRequest {
        private String appId;
        private int userId;
        private String idNumber;
        private String realName;
        private String businessLine;
        private boolean ignoreIdNumberRepeat;
        private boolean offline;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getIdNumber() {
            return idNumber;
        }

        public void setIdNumber(String idNumber) {
            this.idNumber = idNumber;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getBusinessLine() {
            return businessLine;
        }

        public void setBusinessLine(String businessLine) {
            this.businessLine = businessLine;
        }

        public boolean isIgnoreIdNumberRepeat() {
            return ignoreIdNumberRepeat;
        }

        public void setIgnoreIdNumberRepeat(boolean ignoreIdNumberRepeat) {
            this.ignoreIdNumberRepeat = ignoreIdNumberRepeat;
        }

        public boolean isOffline() {
            return offline;
        }

        public void setOffline(boolean offline) {
            this.offline = offline;
        }
    }
}
