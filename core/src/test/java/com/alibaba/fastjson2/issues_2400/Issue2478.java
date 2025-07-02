package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue2478 {
    @Test
    public void jsonFastSerializationTest0() {
        NameFilter namingStrategy = NameFilter.of(PropertyNamingStrategy.LowerCaseWithUnderScores);
        User user = new User(123L, "First", "Last");
        String userAsJsonString = JSON.toJSONString(user, namingStrategy);
        assertEquals("{\"first_name\":\"First\",\"id\":123,\"last_name\":\"Last\"}", userAsJsonString);
    }

    @Test
    public void jsonFastSerializationTest1() {
        User user = new User(123L, "First", "Last");
        Arrays.stream(PropertyNamingStrategy.values()).forEach(naming -> {
            NameFilter namingStrategy = NameFilter.of(naming);
            String userAsJsonString = JSON.toJSONString(user, namingStrategy);
            User deserializedUser = JSON.parseObject(userAsJsonString, User.class, namingStrategy);
            assertEquals(user, deserializedUser);
        });
        Arrays.stream(PropertyNamingStrategy.values()).parallel().forEach(naming -> {
            NameFilter namingStrategy = NameFilter.of(naming);
            String userAsJsonString = JSON.toJSONString(user, namingStrategy);
            User deserializedUser = JSON.parseObject(userAsJsonString, User.class, namingStrategy);
            assertEquals(user, deserializedUser);
        });

        JSONReader.AutoTypeBeforeHandler autoTypeFilter = JSONReader.autoTypeFilter(User.class.getName());
        Arrays.stream(PropertyNamingStrategy.values()).forEach(naming -> {
            NameFilter namingStrategy = NameFilter.of(naming);
            String userAsJsonString = JSON.toJSONString(user, namingStrategy);
            User deserializedUser = JSON.parseObject(userAsJsonString, User.class, null, new Filter[]{namingStrategy, autoTypeFilter});
            assertEquals(user, deserializedUser);
        });
        Arrays.stream(PropertyNamingStrategy.values()).parallel().forEach(naming -> {
            NameFilter namingStrategy = NameFilter.of(naming);
            String userAsJsonString = JSON.toJSONString(user, namingStrategy);
            User deserializedUser = JSON.parseObject(userAsJsonString, User.class, null, new Filter[]{namingStrategy, autoTypeFilter});
            assertEquals(user, deserializedUser);
        });
    }

    @Test
    public void jsonFastSerializationTest2() {
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.CamelCase);
        JSONReader.AutoTypeBeforeHandler autoTypeFilter = JSONReader.autoTypeFilter(User.class.getName());
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(User.class, false, nameFilter);
        ObjectReader objectReader2 = provider.getObjectReader(User.class, false, nameFilter);
        assertSame(objectReader, objectReader2);
        objectReader = provider.getObjectReader(User.class, false, autoTypeFilter);
        objectReader2 = provider.getObjectReader(User.class, false, autoTypeFilter);
        assertSame(objectReader, objectReader2);
        objectReader = provider.getObjectReader(User.class, false, nameFilter, autoTypeFilter);
        objectReader2 = provider.getObjectReader(User.class, false, nameFilter, autoTypeFilter);
        assertSame(objectReader, objectReader2);
    }

    @Data
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String firstName;
        private String lastName;
    }
}
