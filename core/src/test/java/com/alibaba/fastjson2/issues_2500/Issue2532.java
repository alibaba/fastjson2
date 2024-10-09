package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2532 {
    String jsonString = "{\"collectionField\": [1, 2, 3]}";

    class ImmutableCollectionExample {
        private final Collection<Integer> collectionField;

        public ImmutableCollectionExample(Collection<Integer> collectionField) {
            this.collectionField = Collections.unmodifiableCollection(new ArrayList<>(collectionField));
        }

        public Collection<Integer> getCollectionField() {
            return collectionField;
        }
    }

    @Test
    public void testParseObjectWithImmutableCollectionField() throws Exception {
        ImmutableCollectionExample result = JSON.parseObject(jsonString, ImmutableCollectionExample.class);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<>(result.getCollectionField()));
    }
}
