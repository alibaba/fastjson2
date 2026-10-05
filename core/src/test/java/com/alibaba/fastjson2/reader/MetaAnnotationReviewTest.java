package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaAnnotationReviewTest {
    @Test
    public void composedJsonTypeEnablesReaderFeatures() {
        assertEquals(123, JSON.parseObject("[123]", Bean.class).id);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @JSONType(deserializeFeatures = JSONReader.Feature.SupportArrayToBean)
    public @interface ArrayBean {
    }

    @ArrayBean
    public static class Bean {
        public int id;
    }

    @Test
    public void interfaceSetterUsesInterfaceMixIn() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.mixIn(Named.class, NamedMixIn.class);
        ObjectReader<NamedBean> reader = provider.getObjectReader(NamedBean.class);
        assertEquals(123, reader.readObject("{\"renamed\":123}").value);
    }

    public interface Named {
        void setValue(int value);
    }

    public interface NamedMixIn {
        @JSONField(name = "renamed")
        void setValue(int value);
    }

    public static class NamedBean implements Named {
        private int value;

        @Override
        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    public void unrelatedAnnotationDoesNotDiscardJsonCreator() {
        assertEquals(133, JSON.parseObject("{\"value\":123}", FactoryBean.class).value);
    }

    public static class FactoryBean {
        public final int value;

        private FactoryBean(int value) {
            this.value = value;
        }

        @JSONCreator(parameterNames = "value")
        @Deprecated
        public static FactoryBean create(int value) {
            return new FactoryBean(value + 10);
        }
    }
}
