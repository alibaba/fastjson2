package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.*;
import java.util.function.ObjIntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LambdaGeneratorTest {
    public static void genLambdaASM() throws Throwable {
        ObjIntConsumer<Bean> setId = LambdaGenerator.createSetterInt(Bean.class, "setId");

        Bean bean = new Bean();
        setId.accept(bean, 123);
        assertEquals(123, bean.id);
    }

    public static void genLambda() throws Throwable {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(Bean.class);
        MethodType invokedType = MethodType.methodType(ObjIntConsumer.class);
        MethodHandle target = lookup.findVirtual(Bean.class, "setId", MethodType.methodType(void.class, int.class));
        MethodType instantiatedMethodType = MethodType.methodType(void.class, Bean.class, int.class);
        MethodType samMethodType = MethodType.methodType(void.class, Object.class, int.class);

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "accept",
                invokedType,
                samMethodType,
                target,
                instantiatedMethodType
        );
        ObjIntConsumer function = (ObjIntConsumer) callSite.getTarget().invoke();

        Bean bean = new Bean();
        function.accept(bean, 123);
        assertEquals(123, bean.id);
    }

    private static class Bean {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static void main(String[] args) throws Throwable {
//        genLambda();
        genLambdaASM();
    }
}
