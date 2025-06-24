package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2399 {
    @Test
    public void test() {
        User user = new User.UserBuilderImpl().id(123L).firstName("wenshao").build();
        String str = JSON.toJSONString(user);
        assertEquals("{\"first_name\":\"wenshao\",\"id\":123}", str);
        System.out.println(str);
        User user1 = JSON.parseObject(str, User.class);
        assertEquals(user.id, user1.id);
        assertEquals(user.firstName, user1.firstName);
    }

    @JsonIgnoreProperties(
            ignoreUnknown = true
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public interface ApiObject
            extends Serializable {
    }

    @JsonDeserialize(builder = User.UserBuilderImpl.class)
    public static class User
            implements ApiObject {
        private static final String ID_FIELD = "id";
        private static final String FIRST_NAME_FIELD = "first_name";
        @JsonProperty("id")
        private @NonNull Long id;
        @JsonProperty("first_name")
        private @NonNull String firstName;

        protected User(User.UserBuilder<?, ?> b) {
            this.id = b.id;
            if (this.id == null) {
                throw new NullPointerException("id is marked non-null but is null");
            } else {
                this.firstName = b.firstName;
                if (this.firstName == null) {
                    throw new NullPointerException("firstName is marked non-null but is null");
                }
            }
        }

        public static User.UserBuilder<?, ?> builder() {
            return new User.UserBuilderImpl();
        }

        public @NonNull Long getId() {
            return this.id;
        }

        public @NonNull String getFirstName() {
            return this.firstName;
        }

        @JsonProperty("id")
        public void setId(@NonNull Long id) {
            if (id == null) {
                throw new NullPointerException("id is marked non-null but is null");
            } else {
                this.id = id;
            }
        }

        @JsonProperty("first_name")
        public void setFirstName(@NonNull String firstName) {
            if (firstName == null) {
                throw new NullPointerException("firstName is marked non-null but is null");
            } else {
                this.firstName = firstName;
            }
        }

        public String toString() {
            Long var10000 = this.getId();
            return "User(id=" + var10000 + ", firstName=" + this.getFirstName() + ")";
        }

        public User(@NonNull Long id, @NonNull String firstName) {
            if (id == null) {
                throw new NullPointerException("id is marked non-null but is null");
            } else if (firstName == null) {
                throw new NullPointerException("firstName is marked non-null but is null");
            }
        }

        public abstract static class UserBuilder<C extends User, B extends User.UserBuilder<C, B>> {
            private Long id;
            private String firstName;

            public UserBuilder() {
            }

            @JsonProperty("id")
            public B id(@NonNull Long id) {
                if (id == null) {
                    throw new NullPointerException("id is marked non-null but is null");
                } else {
                    this.id = id;
                    return this.self();
                }
            }

            @JsonProperty("first_name")
            public B firstName(@NonNull String firstName) {
                if (firstName == null) {
                    throw new NullPointerException("firstName is marked non-null but is null");
                } else {
                    this.firstName = firstName;
                    return this.self();
                }
            }

            protected abstract B self();

            public abstract C build();

            public String toString() {
                return "User.UserBuilder(id=" + this.id + ", firstName=" + this.firstName + ")";
            }
        }

        @JsonPOJOBuilder(
                withPrefix = "",
                buildMethodName = "build"
        )
        static final class UserBuilderImpl
                extends User.UserBuilder<User, User.UserBuilderImpl> {
            private UserBuilderImpl() {
            }

            protected User.UserBuilderImpl self() {
                return this;
            }

            public User build() {
                return new User(this);
            }
        }
    }
}
