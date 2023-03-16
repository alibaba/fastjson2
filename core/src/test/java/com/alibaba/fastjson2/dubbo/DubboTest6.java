package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.apache.dubbo.springboot.demo.BusinessException;
import org.apache.dubbo.springboot.demo.ParamsDTO;
import org.apache.dubbo.springboot.demo.ParamsItemDTO;
import org.apache.dubbo.springboot.demo.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.DefaultAopProxyFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest6 {
    @Test
    public void test() {
        ContextAutoTypeBeforeHandler contextAutoTypeBeforeHandler = new ContextAutoTypeBeforeHandler(true, ServiceException.class.getName(), BusinessException.class.getName());
        byte[] jsonbBytes = Base64.getDecoder().decode(base64);
        System.out.println(JSONB.toJSONString(jsonbBytes));
        ServiceException exception = (ServiceException) JSONB.parseObject(jsonbBytes, Object.class, contextAutoTypeBeforeHandler, readerFeatures);
        assertNotNull(exception);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
    }

    @Test
    public void test1() {
        ParamsDTO paramsDTO = new ParamsDTO();
        ParamsItemDTO paramsItemDTO = new ParamsItemDTO();
        paramsItemDTO.setA("aaa");
        paramsDTO.setParamsItems(Arrays.asList(paramsItemDTO));

        AdvisedSupport config = new AdvisedSupport();
        config.setTarget(paramsDTO);
        DefaultAopProxyFactory factory = new DefaultAopProxyFactory();
        Object proxy = factory.createAopProxy(config).getProxy();
        Object proxy1 = factory.createAopProxy(config).getProxy();

        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(proxy.getClass());
        ObjectWriter objectWriter1 = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(proxy1.getClass());
        assertSame(objectWriter, objectWriter1);

        byte[] jsonbBytes = JSONB.toBytes(proxy, writerFeatures);
        ContextAutoTypeBeforeHandler contextAutoTypeBeforeHandler = new ContextAutoTypeBeforeHandler(true, ParamsDTO.class.getName());

        ParamsDTO paramsDTO1 = (ParamsDTO) JSONB.parseObject(jsonbBytes, Object.class, contextAutoTypeBeforeHandler, readerFeatures);
        assertEquals(paramsDTO.getParamsItems().size(), paramsDTO1.getParamsItems().size());
        assertEquals(paramsDTO.getParamsItems().get(0).getA(), paramsDTO1.getParamsItems().get(0).getA());
    }

    @Test
    public void testEnumSet() {
        ParamsDTO paramsDTO = new ParamsDTO();
        EnumSet<TimeUnit> timeUnitSet = EnumSet.of(TimeUnit.DAYS);
        paramsDTO.setParamsItemSet(timeUnitSet);

        AdvisedSupport config = new AdvisedSupport();
        config.setTarget(paramsDTO);
        DefaultAopProxyFactory factory = new DefaultAopProxyFactory();
        Object proxy = factory.createAopProxy(config).getProxy();
        Object proxy1 = factory.createAopProxy(config).getProxy();

        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(proxy.getClass());
        ObjectWriter objectWriter1 = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(proxy1.getClass());
        assertSame(objectWriter, objectWriter1);

        byte[] jsonbBytes = JSONB.toBytes(proxy, writerFeatures);
        ContextAutoTypeBeforeHandler contextAutoTypeBeforeHandler = new ContextAutoTypeBeforeHandler(true,
                ParamsDTO.class.getName());
        ParamsDTO paramsDTO1 = (ParamsDTO) JSONB.parseObject(jsonbBytes, Object.class, contextAutoTypeBeforeHandler, readerFeatures);
        assertEquals(paramsDTO.getParamsItemSet().size(), paramsDTO1.getParamsItemSet().size());
        assertEquals(paramsDTO.getParamsItemSet().iterator().next(), paramsDTO1.getParamsItemSet().iterator().next());
    }

    static String base64 = "knk4MW9yZy5hcGFjaGUuZHViYm8uc3ByaW5nYm9vdC5kZW1vLlNlcnZpY2VFeGNlcHRpb24Apn9OY2F1c2UBknk4Mm9yZy5hcGFjaGUuZHViYm8uc3ByaW5nYm9vdC5kZW1vLkJ1c2luZXNzRXhjZXB0aW9uAqZ/UG1lc3NhZ2UDeg/miqXplJnllabjgILjgIJ/U3N0YWNrVHJhY2UEpCamf1FmaWxlTmFtZQVdRGVtb1NlcnZpY2VJbXBsLmphdmF/U2xpbmVOdW1iZXIGKn9SY2xhc3NOYW1lB3k4OW9yZy5hcGFjaGUuZHViYm8uc3ByaW5nYm9vdC5kZW1vLnByb3ZpZGVyLkRlbW9TZXJ2aWNlSW1wbH9TbWV0aG9kTmFtZQhRc2F5SGVsbG+lpn8FZ0RlbW9TZXJ2aWNlSW1wbER1YmJvV3JhcDAuamF2YX8G/38HeThDb3JnLmFwYWNoZS5kdWJiby5zcHJpbmdib290LmRlbW8ucHJvdmlkZXIuRGVtb1NlcnZpY2VJbXBsRHViYm9XcmFwMH8IVWludm9rZU1ldGhvZKWmfwVjSmF2YXNzaXN0UHJveHlGYWN0b3J5LmphdmF/BjhJfwd5ODxvcmcuYXBhY2hlLmR1YmJvLnJwYy5wcm94eS5qYXZhc3Npc3QuSmF2YXNzaXN0UHJveHlGYWN0b3J5JDF/CFFkb0ludm9rZaWmfwViQWJzdHJhY3RQcm94eUludm9rZXIuamF2YX8GOGR/B3hvcmcuYXBhY2hlLmR1YmJvLnJwYy5wcm94eS5BYnN0cmFjdFByb3h5SW52b2tlcn8IT2ludm9rZaWmfwVtRGVsZWdhdGVQcm92aWRlck1ldGFEYXRhSW52b2tlci5qYXZhfwY4N38HeTg/b3JnLmFwYWNoZS5kdWJiby5jb25maWcuaW52b2tlci5EZWxlZ2F0ZVByb3ZpZGVyTWV0YURhdGFJbnZva2VyfwhPaW52b2tlpaZ/BVxJbnZva2VyV3JhcHBlci5qYXZhfwY4OH8HdW9yZy5hcGFjaGUuZHViYm8ucnBjLnByb3RvY29sLkludm9rZXJXcmFwcGVyfwhPaW52b2tlpaZ/BWdDbGFzc0xvYWRlckNhbGxiYWNrRmlsdGVyLmphdmF/BiZ/B3k4NW9yZy5hcGFjaGUuZHViYm8ucnBjLmZpbHRlci5DbGFzc0xvYWRlckNhbGxiYWNrRmlsdGVyfwhPaW52b2tlpaZ/BWBGaWx0ZXJDaGFpbkJ1aWxkZXIuamF2YX8GOUd/B3k4TG9yZy5hcGFjaGUuZHViYm8ucnBjLmNsdXN0ZXIuZmlsdGVyLkZpbHRlckNoYWluQnVpbGRlciRDb3B5T2ZGaWx0ZXJDaGFpbk5vZGV/CE9pbnZva2Wlpn8FWVRyYWNlRmlsdGVyLmphdmF/BjhPfwd5ODZvcmcuYXBhY2hlLmR1YmJvLnJwYy5wcm90b2NvbC5kdWJiby5maWx0ZXIuVHJhY2VGaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwVbVGltZW91dEZpbHRlci5qYXZhfwYsfwdyb3JnLmFwYWNoZS5kdWJiby5ycGMuZmlsdGVyLlRpbWVvdXRGaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwVbTW9uaXRvckZpbHRlci5qYXZhfwY4ZH8Hd29yZy5hcGFjaGUuZHViYm8ubW9uaXRvci5zdXBwb3J0Lk1vbml0b3JGaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwVdRXhjZXB0aW9uRmlsdGVyLmphdmF/Bjg2fwd0b3JnLmFwYWNoZS5kdWJiby5ycGMuZmlsdGVyLkV4Y2VwdGlvbkZpbHRlcn8IT2ludm9rZaWmfwVgRmlsdGVyQ2hhaW5CdWlsZGVyLmphdmF/BjlHfwd5OExvcmcuYXBhY2hlLmR1YmJvLnJwYy5jbHVzdGVyLmZpbHRlci5GaWx0ZXJDaGFpbkJ1aWxkZXIkQ29weU9mRmlsdGVyQ2hhaW5Ob2RlfwhPaW52b2tlpaZ/BWJEdWJib0V4Y2VwdGlvbkZpbHRlci5qYXZhfwYgfwd5OD5vcmcuYXBhY2hlLmR1YmJvLnNwcmluZ2Jvb3QuZGVtby5wcm92aWRlci5EdWJib0V4Y2VwdGlvbkZpbHRlcn8IT2ludm9rZaWmfwVgRmlsdGVyQ2hhaW5CdWlsZGVyLmphdmF/BjlHfwd5OExvcmcuYXBhY2hlLmR1YmJvLnJwYy5jbHVzdGVyLmZpbHRlci5GaWx0ZXJDaGFpbkJ1aWxkZXIkQ29weU9mRmlsdGVyQ2hhaW5Ob2RlfwhPaW52b2tlpaZ/BVtHZW5lcmljRmlsdGVyLmphdmF/BjjAfwdyb3JnLmFwYWNoZS5kdWJiby5ycGMuZmlsdGVyLkdlbmVyaWNGaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwVfQ2xhc3NMb2FkZXJGaWx0ZXIuamF2YX8GODZ/B3ZvcmcuYXBhY2hlLmR1YmJvLnJwYy5maWx0ZXIuQ2xhc3NMb2FkZXJGaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwVYRWNob0ZpbHRlci5qYXZhfwYpfwdvb3JnLmFwYWNoZS5kdWJiby5ycGMuZmlsdGVyLkVjaG9GaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwViUHJvZmlsZXJTZXJ2ZXJGaWx0ZXIuamF2YX8GODp/B3k4MG9yZy5hcGFjaGUuZHViYm8ucnBjLmZpbHRlci5Qcm9maWxlclNlcnZlckZpbHRlcn8IT2ludm9rZaWmfwVgRmlsdGVyQ2hhaW5CdWlsZGVyLmphdmF/BjlHfwd5OExvcmcuYXBhY2hlLmR1YmJvLnJwYy5jbHVzdGVyLmZpbHRlci5GaWx0ZXJDaGFpbkJ1aWxkZXIkQ29weU9mRmlsdGVyQ2hhaW5Ob2RlfwhPaW52b2tlpaZ/BVtDb250ZXh0RmlsdGVyLmphdmF/BjiFfwdyb3JnLmFwYWNoZS5kdWJiby5ycGMuZmlsdGVyLkNvbnRleHRGaWx0ZXJ/CE9pbnZva2Wlpn8FYEZpbHRlckNoYWluQnVpbGRlci5qYXZhfwY5R38HeThMb3JnLmFwYWNoZS5kdWJiby5ycGMuY2x1c3Rlci5maWx0ZXIuRmlsdGVyQ2hhaW5CdWlsZGVyJENvcHlPZkZpbHRlckNoYWluTm9kZX8IT2ludm9rZaWmfwVgRmlsdGVyQ2hhaW5CdWlsZGVyLmphdmF/BjjCfwd5OFJvcmcuYXBhY2hlLmR1YmJvLnJwYy5jbHVzdGVyLmZpbHRlci5GaWx0ZXJDaGFpbkJ1aWxkZXIkQ2FsbGJhY2tSZWdpc3RyYXRpb25JbnZva2VyfwhPaW52b2tlpaZ/BVtEdWJib1Byb3RvY29sLmphdmF/Bjicfwd5ODNvcmcuYXBhY2hlLmR1YmJvLnJwYy5wcm90b2NvbC5kdWJiby5EdWJib1Byb3RvY29sJDF/CE5yZXBseaWmfwVjSGVhZGVyRXhjaGFuZ2VIYW5kbGVyLmphdmF/Bjhmfwd5OEdvcmcuYXBhY2hlLmR1YmJvLnJlbW90aW5nLmV4Y2hhbmdlLnN1cHBvcnQuaGVhZGVyLkhlYWRlckV4Y2hhbmdlSGFuZGxlcn8IVmhhbmRsZVJlcXVlc3Slpn8FY0hlYWRlckV4Y2hhbmdlSGFuZGxlci5qYXZhfwY4sX8HeThHb3JnLmFwYWNoZS5kdWJiby5yZW1vdGluZy5leGNoYW5nZS5zdXBwb3J0LmhlYWRlci5IZWFkZXJFeGNoYW5nZUhhbmRsZXJ/CFFyZWNlaXZlZKWmfwVbRGVjb2RlSGFuZGxlci5qYXZhfwY4NX8HeTgxb3JnLmFwYWNoZS5kdWJiby5yZW1vdGluZy50cmFuc3BvcnQuRGVjb2RlSGFuZGxlcn8IUXJlY2VpdmVkpaZ/BWJDaGFubmVsRXZlbnRSdW5uYWJsZS5qYXZhfwY4Pn8HeThDb3JnLmFwYWNoZS5kdWJiby5yZW1vdGluZy50cmFuc3BvcnQuZGlzcGF0Y2hlci5DaGFubmVsRXZlbnRSdW5uYWJsZX8ITHJ1bqWmfwVgVGhyZWFkUG9vbEV4ZWN1dG9yLmphdmF/Bjx9fwdwamF2YS51dGlsLmNvbmN1cnJlbnQuVGhyZWFkUG9vbEV4ZWN1dG9yfwhScnVuV29ya2VypaZ/BWBUaHJlYWRQb29sRXhlY3V0b3IuamF2YX8GOnB/B3dqYXZhLnV0aWwuY29uY3VycmVudC5UaHJlYWRQb29sRXhlY3V0b3IkV29ya2VyfwhMcnVupaZ/BV5JbnRlcm5hbFJ1bm5hYmxlLmphdmF/Bil/B3k4NG9yZy5hcGFjaGUuZHViYm8uY29tbW9uLnRocmVhZGxvY2FsLkludGVybmFsUnVubmFibGV/CExydW6lpn8FVFRocmVhZC5qYXZhfwY67H8HWWphdmEubGFuZy5UaHJlYWR/CExydW6lf11zdXBwcmVzc2VkRXhjZXB0aW9ucwmSeTgyamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZVJhbmRvbUFjY2Vzc0xpc3QKlKV/VmRldGFpbE1lc3NhZ2ULejhDb3JnLmFwYWNoZS5kdWJiby5zcHJpbmdib290LmRlbW8uQnVzaW5lc3NFeGNlcHRpb246IOaKpemUmeWVpuOAguOAgn8EpBGmfwViRHViYm9FeGNlcHRpb25GaWx0ZXIuamF2YX8GKX8HeTg+b3JnLmFwYWNoZS5kdWJiby5zcHJpbmdib290LmRlbW8ucHJvdmlkZXIuRHViYm9FeGNlcHRpb25GaWx0ZXJ/CFNvblJlc3BvbnNlpaZ/BWBGaWx0ZXJDaGFpbkJ1aWxkZXIuamF2YX8GONp/B3k4Um9yZy5hcGFjaGUuZHViYm8ucnBjLmNsdXN0ZXIuZmlsdGVyLkZpbHRlckNoYWluQnVpbGRlciRDYWxsYmFja1JlZ2lzdHJhdGlvbkludm9rZXJ/CFhsYW1iZGEkaW52b2tlJDGlpn8FXEFzeW5jUnBjUmVzdWx0LmphdmF/BjjYfwdsb3JnLmFwYWNoZS5kdWJiby5ycGMuQXN5bmNScGNSZXN1bHR/CGlsYW1iZGEkd2hlbkNvbXBsZXRlV2l0aENvbnRleHQkMKWmfwVfQ29tcGxldGFibGVGdXR1cmUuamF2YX8GOwZ/B29qYXZhLnV0aWwuY29uY3VycmVudC5Db21wbGV0YWJsZUZ1dHVyZX8IWHVuaVdoZW5Db21wbGV0ZaWmfwVfQ29tcGxldGFibGVGdXR1cmUuamF2YX8GOxh/B29qYXZhLnV0aWwuY29uY3VycmVudC5Db21wbGV0YWJsZUZ1dHVyZX8IXXVuaVdoZW5Db21wbGV0ZVN0YWdlpaZ/BV9Db21wbGV0YWJsZUZ1dHVyZS5qYXZhfwZECGl/B29qYXZhLnV0aWwuY29uY3VycmVudC5Db21wbGV0YWJsZUZ1dHVyZX8IVXdoZW5Db21wbGV0ZaWmfwVcQXN5bmNScGNSZXN1bHQuamF2YX8GONR/B2xvcmcuYXBhY2hlLmR1YmJvLnJwYy5Bc3luY1JwY1Jlc3VsdH8IYHdoZW5Db21wbGV0ZVdpdGhDb250ZXh0paZ/BWBGaWx0ZXJDaGFpbkJ1aWxkZXIuamF2YX8GOMN/B3k4Um9yZy5hcGFjaGUuZHViYm8ucnBjLmNsdXN0ZXIuZmlsdGVyLkZpbHRlckNoYWluQnVpbGRlciRDYWxsYmFja1JlZ2lzdHJhdGlvbkludm9rZXJ/CE9pbnZva2Wlpn8FW0R1YmJvUHJvdG9jb2wuamF2YX8GOJx/B3k4M29yZy5hcGFjaGUuZHViYm8ucnBjLnByb3RvY29sLmR1YmJvLkR1YmJvUHJvdG9jb2wkMX8ITnJlcGx5paZ/BWNIZWFkZXJFeGNoYW5nZUhhbmRsZXIuamF2YX8GOGZ/B3k4R29yZy5hcGFjaGUuZHViYm8ucmVtb3RpbmcuZXhjaGFuZ2Uuc3VwcG9ydC5oZWFkZXIuSGVhZGVyRXhjaGFuZ2VIYW5kbGVyfwhWaGFuZGxlUmVxdWVzdKWmfwVjSGVhZGVyRXhjaGFuZ2VIYW5kbGVyLmphdmF/Bjixfwd5OEdvcmcuYXBhY2hlLmR1YmJvLnJlbW90aW5nLmV4Y2hhbmdlLnN1cHBvcnQuaGVhZGVyLkhlYWRlckV4Y2hhbmdlSGFuZGxlcn8IUXJlY2VpdmVkpaZ/BVtEZWNvZGVIYW5kbGVyLmphdmF/Bjg1fwd5ODFvcmcuYXBhY2hlLmR1YmJvLnJlbW90aW5nLnRyYW5zcG9ydC5EZWNvZGVIYW5kbGVyfwhRcmVjZWl2ZWSlpn8FYkNoYW5uZWxFdmVudFJ1bm5hYmxlLmphdmF/Bjg+fwd5OENvcmcuYXBhY2hlLmR1YmJvLnJlbW90aW5nLnRyYW5zcG9ydC5kaXNwYXRjaGVyLkNoYW5uZWxFdmVudFJ1bm5hYmxlfwhMcnVupaZ/BWBUaHJlYWRQb29sRXhlY3V0b3IuamF2YX8GPH1/B3BqYXZhLnV0aWwuY29uY3VycmVudC5UaHJlYWRQb29sRXhlY3V0b3J/CFJydW5Xb3JrZXKlpn8FYFRocmVhZFBvb2xFeGVjdXRvci5qYXZhfwY6cH8Hd2phdmEudXRpbC5jb25jdXJyZW50LlRocmVhZFBvb2xFeGVjdXRvciRXb3JrZXJ/CExydW6lpn8FXkludGVybmFsUnVubmFibGUuamF2YX8GKX8HeTg0b3JnLmFwYWNoZS5kdWJiby5jb21tb24udGhyZWFkbG9jYWwuSW50ZXJuYWxSdW5uYWJsZX8ITHJ1bqWmfwVUVGhyZWFkLmphdmF/BjrsfwdZamF2YS5sYW5nLlRocmVhZH8ITHJ1bqV/CZNlJC5jYXVzZS5zdXBwcmVzc2VkRXhjZXB0aW9uc6U=";

    JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ErrorOnNoneSerializable,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.ErrorOnNoneSerializable,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };
}
