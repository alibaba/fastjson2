package com.alibaba.fastjson.support.hsf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HSFJSONUtils {
    static final long HASH_ARGS_TYPES = Fnv.hashCode64("argsTypes");
    static final long HASH_ARGS_OBJS = Fnv.hashCode64("argsObjs");

    public static Object[] parseInvocationArguments(String json, MethodLocator methodLocator) {
        JSONReader jsonReader = JSONReader.of(json);

        Method method = null;
        Object[] values;
        String[] typeNames;

        if (jsonReader.nextIfMatch('{')) {
            long hash0 = jsonReader.readFieldNameHashCode();
            if (hash0 == HASH_ARGS_TYPES) {
                if (jsonReader.nextIfMatch('[')) {
                    String name0 = null, name1 = null;
                    List<String> nameList = null;
                    int i = 0;
                    for (; ; ++i) {
                        if (jsonReader.nextIfMatch(']')) {
                            jsonReader.nextIfMatch(',');
                            break;
                        } else if (jsonReader.isEnd()) {
                            throw new JSONException("illegal format");
                        }
                        String name = jsonReader.readString();
                        if (i == 0) {
                            name0 = name;
                        } else if (i == 1) {
                            name1 = name;
                        } else if (i == 2) {
                            nameList = new ArrayList<>();
                            nameList.add(name0);
                            nameList.add(name1);
                            nameList.add(name);
                        } else {
                            nameList.add(name);
                        }
                    }

                    if (i == 0) {
                        typeNames = new String[0];
                    } else if (i == 1) {
                        typeNames = new String[]{name0};
                    } else if (i == 2) {
                        typeNames = new String[]{name0, name1};
                    } else {
                        typeNames = new String[nameList.size()];
                        nameList.toArray(typeNames);
                    }
                } else {
                    throw new JSONException("illegal format");
                }
                method = methodLocator.findMethod(typeNames);
            }

            if (method != null) {
                Type[] argTypes = method.getGenericParameterTypes();
                long hash1 = jsonReader.readFieldNameHashCode();
                if (hash1 == HASH_ARGS_OBJS) {
                    values = jsonReader.readArray(argTypes);
                } else {
                    throw new JSONException("illegal format");
                }
            } else {
                JSONObject jsonObject = JSON.parseObject(json);
                typeNames = jsonObject.getObject("argsTypes", String[].class);
                method = methodLocator.findMethod(typeNames);

                JSONArray argsObjs = jsonObject.getJSONArray("argsObjs");
                if (argsObjs == null) {
                    values = null;
                } else {
                    Type[] argTypes = method.getGenericParameterTypes();
                    values = new Object[argTypes.length];
                    for (int i = 0; i < argTypes.length; i++) {
                        Type type = argTypes[i];
                        values[i] = argsObjs.getObject(i, type);
                    }
                }
            }
        } else if (jsonReader.nextIfMatch('[')) {
            if (jsonReader.nextIfMatch('[')) {
                String name0 = null, name1 = null;
                List<String> nameList = null;
                int i = 0;
                for (; ; ++i) {
                    if (jsonReader.nextIfMatch(']')) {
                        jsonReader.nextIfMatch(',');
                        break;
                    } else if (jsonReader.isEnd()) {
                        throw new JSONException("illegal format");
                    }
                    String name = jsonReader.readString();
                    if (i == 0) {
                        name0 = name;
                    } else if (i == 1) {
                        name1 = name;
                    } else if (i == 2) {
                        nameList = new ArrayList<>();
                        nameList.add(name0);
                        nameList.add(name1);
                        nameList.add(name);
                    } else {
                        nameList.add(name);
                    }
                }

                if (i == 0) {
                    typeNames = new String[0];
                } else if (i == 1) {
                    typeNames = new String[]{name0};
                } else if (i == 2) {
                    typeNames = new String[]{name0, name1};
                } else {
                    typeNames = new String[nameList.size()];
                    nameList.toArray(typeNames);
                }
            } else {
                throw new JSONException("illegal format");
            }

            method = methodLocator.findMethod(typeNames);

            Type[] argTypes = method.getGenericParameterTypes();
            values = jsonReader.readArray(argTypes);
        } else {
            values = null;
        }

        return values;
    }
}
