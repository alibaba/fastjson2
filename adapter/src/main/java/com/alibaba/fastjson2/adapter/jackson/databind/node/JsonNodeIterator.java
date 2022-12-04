package com.alibaba.fastjson2.adapter.jackson.databind.node;

import java.util.Iterator;

class JsonNodeIterator
        implements Iterator {
    final Iterator it;

    public JsonNodeIterator(Iterator it) {
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Object next() {
        Object value = it.next();
        return TreeNodeUtils.as(value);
    }
}
