package com.alibaba.fastjson2.diff.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.diff.*;
import com.alibaba.fastjson2.diff.handle.IntricacyArrayHandle;
import com.alibaba.fastjson2.diff.handle.MultidimensionalArrayHandle;
import com.alibaba.fastjson2.diff.handle.ObjectArrayHandle;
import com.alibaba.fastjson2.diff.handle.SimpleArrayHandle;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static com.alibaba.fastjson2.diff.JsonDiffConstants.SIGN;


public class JsonDiffUtil {

    /**
     * Judge whether the current object is a basic type in the json data format
     * @param obj
     * @return
     */
    public static boolean isPrimitiveType(Object obj){

        if(obj == null){
            return true;
        }

        if(obj instanceof JSONArray || obj instanceof JSONObject){
            return false;
        }

        if (String.class.isAssignableFrom(obj.getClass())) {
            return true;
        }

        try {
            return ((Class<?>)obj.getClass().getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * From two perspectives, 1 Elements are all of one type 2 Whether the element is a basic type
     * Types can only be divided into basic types and complex types. In fact, complex types only have JSONObject
     * @return
     */
    public static Class<?> getArrayHandleClass(JSONArray expect, JSONArray actual) {

        Set<Class<?>> typeSet = new HashSet<>();
        for (Object item: expect) {
            typeSet.add(parseItemClass(item));
        }
        for (Object item: actual) {
            typeSet.add(parseItemClass(item));
        }

        if (typeSet.size() > 1) {
            return IntricacyArrayHandle.class;
        }

        if (typeSet.size() == 0) {
            throw new JsonDiffException("The correct element type was not recognized");
        }
        return typeSet.stream().findAny().get();
    }

    public static Class<?> getObjectHandleClass(JSONObject expect, JSONObject actual) {
        return HandleBucket.getObjectHandle();
    }

    /**
     * Element of array 1 Basic type 2 Object 3 array
     * Multiple types of data are not supported temporarily
     * @param item
     * @return
     */
    public static Class<?> parseItemClass(Object item) {
        if(isPrimitiveType(item)) {
            return SimpleArrayHandle.class;
        }
        if (item instanceof JSONArray) {
            return MultidimensionalArrayHandle.class;
        }
        if (item instanceof JSONObject) {
            return ObjectArrayHandle.class;
        }
        return SimpleArrayHandle.class;
    }


    public static String getCurrentPath(Stack<String> strings) {
        String[] paths = strings.toArray(new String[0]);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            stringBuilder.append(paths[i]);
            if (i >= paths.length -1) {
                continue;
            }
            if (!(paths[i + 1].startsWith("[") && paths[i + 1].endsWith("]"))) {
                stringBuilder.append(SIGN);
            }
        }
        return stringBuilder.toString();
    }

    public static String convertPath(String root, Stack<String> tempPath) {
        if (root.trim().equals("")) {
            return JsonDiffUtil.getCurrentPath(tempPath);
        }
        if (tempPath == null || tempPath.size() == 0) {
            return root;
        }
        return String.format("%s.%s", root, JsonDiffUtil.getCurrentPath(tempPath));
    }


}
