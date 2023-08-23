package com.alibaba.fastjson2.example.spring6test;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson2.example.spring6test.api.TestApi;
import com.alibaba.fastjson2.example.spring6test.codec.Fastjson2Codec;
import com.alibaba.fastjson2.example.spring6test.entity.WebFluxMockBean;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class WebFluxTest {
    private Fastjson2Codec fastjson2Codec;
    private HttpServiceProxyFactory factory;

    @Before
    public void init() {
        fastjson2Codec = new Fastjson2Codec();

        // init WebClient
        var webClient = WebClient.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                    configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024);
                    configurer.defaultCodecs().jackson2JsonDecoder(this.fastjson2Codec.getDecoder());
                    configurer.defaultCodecs().jackson2JsonEncoder(this.fastjson2Codec.getEncoder());
                })
                .build();
        //根据web客户端去构建服http服务的代理工厂
        this.factory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
    }

    @Test
    public void testSearchWisdom() {
        TestApi api = factory.createClient(TestApi.class);
        try {
            WebFluxMockBean bean1 = api.webFluxGet();
            Assert.notNull(bean1);
            Assert.isTrue("NAME".equals(bean1.getUpperName()));
            System.out.println(bean1);

            WebFluxMockBean bean2 = api.webFluxPost(bean1);
            Assert.notNull(bean2);
            Assert.isTrue("RES=ID2".equals(bean2.getUpperInnerBean().getUpperId()));
            System.out.println(bean2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
