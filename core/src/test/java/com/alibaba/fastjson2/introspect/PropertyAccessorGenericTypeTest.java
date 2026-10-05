package com.alibaba.fastjson2.introspect;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PropertyAccessorGenericTypeTest {
    @Test
    public void getterRejectsMismatchedGenericType() throws Exception {
        PropertyAccessorFactory factory = new PropertyAccessorFactory();
        Method getter = Bean.class.getMethod("getValues");
        Type wrongType = new TypeReference<List<Integer>>() { }.getType();
        assertThrows(JSONException.class, () -> factory.create("values", List.class, wrongType, getter, null));
    }

    @Test
    public void getterAcceptsMatchingAndInferredGenericTypes() throws Exception {
        PropertyAccessorFactory factory = new PropertyAccessorFactory();
        Method getter = Bean.class.getMethod("getValues");
        Type expectedType = new TypeReference<List<String>>() { }.getType();
        PropertyAccessor explicit = factory.create("values", List.class, expectedType, getter, null);
        PropertyAccessor inferred = factory.create("values", List.class, null, getter, null);
        assertEquals(expectedType, explicit.propertyType());
        assertEquals(expectedType, inferred.propertyType());
        assertEquals(Collections.singletonList("value"), explicit.getObject(new Bean()));
        assertEquals(Collections.singletonList("value"), inferred.getObject(new Bean()));
    }

    public static class Bean {
        public List<String> getValues() {
            return Collections.singletonList("value");
        }
    }
}
