package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONType;

import java.util.Objects;

@JSONCompiled
@JSONType(naming = PropertyNamingStrategy.PascalCase)
public class Bean {
    public int id;
    public String name;

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
}
