// https://www.oracle.com/corporate/features/understanding-java-9-modules.html

open module com.alibaba.fastjson2 {
    requires transitive jdk.unsupported;
    requires java.management;
    requires com.alibaba.fastjson2;

    exports com.alibaba.fastjson2.support.airlift;
    exports com.alibaba.fastjson2.support.arrow;
    exports com.alibaba.fastjson2.support.config;
    exports com.alibaba.fastjson2.support.geo;
    exports com.alibaba.fastjson2.support.odps;
    exports com.alibaba.fastjson2.support.retrofit;
}
