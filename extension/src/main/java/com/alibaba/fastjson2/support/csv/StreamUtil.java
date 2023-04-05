package com.alibaba.fastjson2.support.csv;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
    public static <T> Stream<T> stream(Consumer<Consumer<? super T>> consumer) {
        Iterator<T> iterator = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                throw new UnsupportedOperationException();
            }

            @Override
            public T next() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                consumer.accept(action);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
