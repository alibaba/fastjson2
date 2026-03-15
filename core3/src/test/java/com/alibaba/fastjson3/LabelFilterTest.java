package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LabelFilterTest {
    public static class UserProfile {
        private String name;
        @JSONField(label = "admin")
        private String email;
        @JSONField(label = "internal")
        private String internalNote;
        private int age;

        public UserProfile() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getInternalNote() {
            return internalNote;
        }

        public void setInternalNote(String internalNote) {
            this.internalNote = internalNote;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    public void testLabelFilterIncludes() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addLabelFilter(label -> "admin".equals(label))
                .build();

        UserProfile user = new UserProfile();
        user.setName("Alice");
        user.setEmail("alice@test.com");
        user.setInternalNote("secret");
        user.setAge(30);

        String json = mapper.writeValueAsString(user);
        // name and age have no label → always included
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"age\""), json);
        // email has label "admin" → included (filter matches)
        assertTrue(json.contains("\"email\""), json);
        // internalNote has label "internal" → excluded (filter doesn't match)
        assertFalse(json.contains("internalNote"), json);
    }

    @Test
    public void testNoLabelFilterIncludesAll() {
        ObjectMapper mapper = ObjectMapper.shared();

        UserProfile user = new UserProfile();
        user.setName("Alice");
        user.setEmail("alice@test.com");
        user.setInternalNote("secret");

        String json = mapper.writeValueAsString(user);
        // Without LabelFilter, all fields are included
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"email\""), json);
        assertTrue(json.contains("\"internalNote\""), json);
    }

    @Test
    public void testLabelFilterExcludesAll() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addLabelFilter(label -> false) // reject all labels
                .build();

        UserProfile user = new UserProfile();
        user.setName("Alice");
        user.setEmail("alice@test.com");
        user.setInternalNote("secret");
        user.setAge(30);

        String json = mapper.writeValueAsString(user);
        // Fields without labels are still included
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"age\""), json);
        // All labeled fields excluded
        assertFalse(json.contains("email"), json);
        assertFalse(json.contains("internalNote"), json);
    }
}
