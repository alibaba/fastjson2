package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@JSONType(naming = PropertyNamingStrategy.PascalCase)
@JSONCompiled
public class Bean {
    @JSONField(name = "uid")
    public int id;
    public String name;
    public long v0;
    public java.util.Date v1;
    public Long v2;
    public String v3;
    public BigDecimal v4;
    public List<Item> items;
    public List<String> strings;

    public Bean() {
    }

    public Bean(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Bean bean = (Bean) o;

        if (id != bean.id) {
            return false;
        }
        return Objects.equals(name, bean.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public static class Item {
        public int id;
    }
}
