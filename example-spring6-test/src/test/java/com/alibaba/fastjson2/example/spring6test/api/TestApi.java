package com.alibaba.fastjson2.example.spring6test.api;

import com.alibaba.fastjson2.example.spring6test.entity.WebFluxMockBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("http://127.0.0.1:8080")
public interface TestApi {
    @GetExchange("/webFluxGet")
    WebFluxMockBean webFluxGet();
    @PostExchange("/webFluxPost")
    WebFluxMockBean webFluxPost(@RequestBody WebFluxMockBean request);
}
