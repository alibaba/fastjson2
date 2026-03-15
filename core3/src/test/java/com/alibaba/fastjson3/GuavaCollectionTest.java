package com.alibaba.fastjson3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuavaCollectionTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== ImmutableList ====================

    @Test
    public void testImmutableListSerialize() {
        ImmutableList<String> list = ImmutableList.of("a", "b", "c");
        String json = MAPPER.writeValueAsString(list);
        assertEquals("[\"a\",\"b\",\"c\"]", json);
    }

    @Test
    public void testImmutableListDeserialize() {
        ImmutableList<?> list = MAPPER.readValue("[1,2,3]", ImmutableList.class);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertInstanceOf(ImmutableList.class, list);
    }

    @Test
    public void testImmutableListDeserializeStrings() {
        ImmutableList<?> list = MAPPER.readValue("[\"a\",\"b\"]", ImmutableList.class);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertInstanceOf(ImmutableList.class, list);
    }

    // ==================== ImmutableSet ====================

    @Test
    public void testImmutableSetSerialize() {
        ImmutableSet<String> set = ImmutableSet.of("x", "y");
        String json = MAPPER.writeValueAsString(set);
        assertTrue(json.contains("\"x\""));
        assertTrue(json.contains("\"y\""));
    }

    @Test
    public void testImmutableSetDeserialize() {
        ImmutableSet<?> set = MAPPER.readValue("[1,2,3]", ImmutableSet.class);
        assertNotNull(set);
        assertEquals(3, set.size());
        assertInstanceOf(ImmutableSet.class, set);
    }

    // ==================== ImmutableMap ====================

    @Test
    public void testImmutableMapSerialize() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);
        String json = MAPPER.writeValueAsString(map);
        assertTrue(json.contains("\"a\":1"));
        assertTrue(json.contains("\"b\":2"));
    }

    @Test
    public void testImmutableMapDeserialize() {
        ImmutableMap<?, ?> map = MAPPER.readValue("{\"k\":\"v\"}", ImmutableMap.class);
        assertNotNull(map);
        assertEquals("v", map.get("k"));
        assertInstanceOf(ImmutableMap.class, map);
    }

    // ==================== POJO field: ImmutableList ====================

    public static class UserListBean {
        private ImmutableList<String> names;

        public UserListBean() {
        }

        public ImmutableList<String> getNames() {
            return names;
        }

        public void setNames(ImmutableList<String> names) {
            this.names = names;
        }
    }

    @Test
    public void testPojoFieldImmutableList() {
        UserListBean bean = MAPPER.readValue("{\"names\":[\"alice\",\"bob\"]}", UserListBean.class);
        assertNotNull(bean.getNames());
        assertInstanceOf(ImmutableList.class, bean.getNames());
        assertEquals(2, bean.getNames().size());
        assertEquals("alice", bean.getNames().get(0));
    }

    @Test
    public void testPojoFieldImmutableListRoundtrip() {
        UserListBean original = new UserListBean();
        original.setNames(ImmutableList.of("a", "b", "c"));

        String json = MAPPER.writeValueAsString(original);
        UserListBean parsed = MAPPER.readValue(json, UserListBean.class);

        assertEquals(original.getNames(), parsed.getNames());
        assertInstanceOf(ImmutableList.class, parsed.getNames());
    }

    // ==================== POJO field: ImmutableMap ====================

    public static class ConfigBean {
        private ImmutableMap<String, String> settings;

        public ConfigBean() {
        }

        public ImmutableMap<String, String> getSettings() {
            return settings;
        }

        public void setSettings(ImmutableMap<String, String> settings) {
            this.settings = settings;
        }
    }

    @Test
    public void testPojoFieldImmutableMap() {
        ConfigBean bean = MAPPER.readValue("{\"settings\":{\"key\":\"value\"}}", ConfigBean.class);
        assertNotNull(bean.getSettings());
        assertInstanceOf(ImmutableMap.class, bean.getSettings());
        assertEquals("value", bean.getSettings().get("key"));
    }

    // ==================== Null handling ====================

    @Test
    public void testImmutableListNull() {
        ImmutableList<?> list = MAPPER.readValue("null", ImmutableList.class);
        assertNull(list);
    }

    @Test
    public void testImmutableListEmpty() {
        ImmutableList<?> list = MAPPER.readValue("[]", ImmutableList.class);
        assertNotNull(list);
        assertTrue(list.isEmpty());
        assertInstanceOf(ImmutableList.class, list);
    }

    // ==================== ImmutableList<POJO> with element conversion ====================

    public static class SimpleUser {
        private String name;

        public SimpleUser() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class UserListPojoBean {
        private ImmutableList<SimpleUser> users;

        public UserListPojoBean() {
        }

        public ImmutableList<SimpleUser> getUsers() {
            return users;
        }

        public void setUsers(ImmutableList<SimpleUser> users) {
            this.users = users;
        }
    }

    @Test
    public void testPojoFieldImmutableListWithPojoElements() {
        UserListPojoBean bean = MAPPER.readValue(
                "{\"users\":[{\"name\":\"alice\"},{\"name\":\"bob\"}]}", UserListPojoBean.class);
        assertNotNull(bean.getUsers());
        assertInstanceOf(ImmutableList.class, bean.getUsers());
        assertEquals(2, bean.getUsers().size());
        assertEquals("alice", bean.getUsers().get(0).getName());
        assertEquals("bob", bean.getUsers().get(1).getName());
    }

    // ==================== ImmutableSet as POJO field ====================

    public static class TagBean {
        private ImmutableSet<String> tags;

        public TagBean() {
        }

        public ImmutableSet<String> getTags() {
            return tags;
        }

        public void setTags(ImmutableSet<String> tags) {
            this.tags = tags;
        }
    }

    @Test
    public void testPojoFieldImmutableSet() {
        TagBean bean = MAPPER.readValue("{\"tags\":[\"a\",\"b\"]}", TagBean.class);
        assertNotNull(bean.getTags());
        assertInstanceOf(ImmutableSet.class, bean.getTags());
        assertEquals(2, bean.getTags().size());
        assertTrue(bean.getTags().contains("a"));
    }

    // ==================== UTF-8 byte[] input ====================

    @Test
    public void testPojoFieldImmutableListFromBytes() {
        byte[] json = "{\"names\":[\"x\",\"y\"]}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UserListBean bean = MAPPER.readValue(json, UserListBean.class);
        assertNotNull(bean.getNames());
        assertInstanceOf(ImmutableList.class, bean.getNames());
        assertEquals(2, bean.getNames().size());
    }

    // ==================== ImmutableList<POJO> via byte[] (UTF-8 path) ====================

    @Test
    public void testPojoFieldImmutableListWithPojoElementsFromBytes() {
        byte[] json = "{\"users\":[{\"name\":\"alice\"},{\"name\":\"bob\"}]}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UserListPojoBean bean = MAPPER.readValue(json, UserListPojoBean.class);
        assertNotNull(bean.getUsers());
        assertInstanceOf(ImmutableList.class, bean.getUsers());
        assertEquals(2, bean.getUsers().size());
        assertInstanceOf(SimpleUser.class, bean.getUsers().get(0));
        assertEquals("alice", bean.getUsers().get(0).getName());
    }
}
