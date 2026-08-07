package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPObject;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;

public class ObjectReaderImplJSONP
        implements ObjectReader {
    private final Class objectClass;

    public ObjectReaderImplJSONP(Class objectClass) {
        this.objectClass = objectClass;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String funcName = jsonReader.readFieldNameUnquote();

        if (jsonReader.nextIfMatch('.')) {
            String name2 = jsonReader.readFieldNameUnquote();
            funcName += '.' + name2;
        }

        char ch = jsonReader.current();
        if (ch == '/') {
            if (jsonReader.nextIfMatchIdent('/', '*', '*', '/')) {
                ch = jsonReader.current();
            }
        }

        if (ch != '(') {
            throw new JSONException(jsonReader.info("illegal jsonp input"));
        }
        jsonReader.next();

        JSONPObject jsonp;
        if (objectClass == JSONObject.class) {
            jsonp = new JSONPObject(funcName);
        } else {
            try {
                jsonp = (JSONPObject) objectClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create jsonp instance error", e);
            }
            jsonp.setFunction(funcName);
        }

        for (;;) {
            if (jsonReader.isEnd()) {
                throw new JSONException(jsonReader.info("illegal jsonp input"));
            }
            if (jsonReader.nextIfMatch(')')) {
                break;
            }
            Object param = jsonReader.readAny();
            jsonp.addParameter(param);
        }

        jsonReader.nextIfMatch(';');
        jsonReader.nextIfMatchIdent('/', '*', '*', '/');

        return jsonp;
    }
}
