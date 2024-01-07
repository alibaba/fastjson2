package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2153 {
    @Test
    public void test() {
        String str = "{\"addTime\":1703124696338,\"updateTime\":1703124696338,\"id\":7}";
        User user = JSON.parseObject(str, User.class);
        assertEquals("{\"id\":7}", JSON.toJSONString(user));
    }

    public interface ID
            extends Serializable {
        Serializable getId();
    }

    public interface Bean<K extends Serializable>
            extends ID {
        @Override
        K getId();

        void setId(K id);
    }

    @Getter
    @Setter
    public abstract static class BaseEntity
            implements Bean<Long> {
        Long id;
    }

    @Getter
    @Setter
    public abstract static class TimeBaseEntity
            extends BaseEntity {
        Date addTime;
        Date updateTime;
    }

    @Getter
    @Setter
    public static class User
            extends BaseEntity {
        String addr;
        Long a, b, c, d, e, f, g;
    }
}
