package com.alibaba.fastjson2.benchmark;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class HessianTest {
    @Test
    public void test0() throws Exception {
        Bean bean = new Bean();
        bean.f0 = "abc";
        bean.f1 = "abc";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(bean);
        hessian2Output.flush();

        byte[] bytes = byteArrayOutputStream.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(bytesIn);

        Bean bean1 = (Bean) hessian2Input.readObject();
        System.out.println(bean1.f0);
        System.out.println(bean1.f1);
    }

    public static class Bean
            implements Serializable {
        public String f0;
        public String f1;
    }
}
