package com.alibaba.fastjson.issus_913;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jiangqiang
 * @version 1.0
 * @date 2022/11/9 13:58
 */
@Data
public class TestJson implements Serializable {
    private static final long serialVersionUID = 1L;

    private PathObj pathObj = new PathObj();

    @Data
    public static class PathObj implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<String> homePathList = CmsConstants.homePathList;

    }

    public static void main(String[] args) {

        System.out.println(toJsonStrWithClass(new TestJson()));
        TestJson parse = (TestJson) parse(toJsonStrWithClass(new TestJson()));
        System.out.println(toJsonStrWithClass(parse));

    }

    public static final class CmsConstants {
        public final static List<String> homePathList = Arrays.asList("/index");
//        public final static List<String> homePathList = Collections.singletonList("/index");
//        public final static List<String> homePathList = List.of("/index");

    }

    public static Object parse(String jsonStr, JSONReader.Feature... features) {
        ArrayList<JSONReader.Feature> tmpFeatures = Arrays.stream(features).collect(Collectors.toCollection(ArrayList::new));
        tmpFeatures.add(JSONReader.Feature.SupportAutoType);
        return JSON.parseObject(jsonStr, Object.class, tmpFeatures.toArray(features));
    }

    public static String toJsonStrWithClass(Object object, JSONWriter.Feature... features) {
        ArrayList<JSONWriter.Feature> tmpFeatures = Arrays.stream(features).collect(Collectors.toCollection(ArrayList::new));
        tmpFeatures.add(JSONWriter.Feature.WriteClassName);
        return JSON.toJSONString(object, tmpFeatures.toArray(new JSONWriter.Feature[0]));
    }
}
