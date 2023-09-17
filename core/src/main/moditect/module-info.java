// https://www.oracle.com/corporate/features/understanding-java-9-modules.html

open module com.alibaba.fastjson2 {
    requires transitive jdk.unsupported;
    requires java.management;

    exports com.alibaba.fastjson2;
    exports com.alibaba.fastjson2.annotation;
    exports com.alibaba.fastjson2.codec;
    exports com.alibaba.fastjson2.filter;
    exports com.alibaba.fastjson2.function;
    exports com.alibaba.fastjson2.modules;
    exports com.alibaba.fastjson2.reader;
    exports com.alibaba.fastjson2.schema;
    exports com.alibaba.fastjson2.support.csv;
    exports com.alibaba.fastjson2.support.money;
    exports com.alibaba.fastjson2.writer;
    exports com.alibaba.fastjson2.util;
}
