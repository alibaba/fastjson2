package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.mvc.ActionExecuteHandlerDefault;
import org.noear.solon.core.wrap.MethodWrap;
import org.noear.solon.core.wrap.ParamWrap;

import java.util.Collection;
import java.util.List;

/**
 * Json ActionExecuteHandler
 *
 * @author noear
 * @author 夜の孤城
 * @author 暮城留风
 * @since 1.9
 * @since 2024-10-01
 * */
public class Fastjson2ActionExecutor extends ActionExecuteHandlerDefault {
    private final Fastjson2StringSerializer serializer = new Fastjson2StringSerializer();

    public Fastjson2ActionExecutor() {
        serializer.getDeserializeConfig().config();
        serializer.getDeserializeConfig().config(JSONReader.Feature.ErrorOnEnumNotMatch);
    }

    /**
     * Gets the serialization interface
     */
    public Fastjson2StringSerializer getSerializer() {
        return serializer;
    }

    /**
     * Deserialize the configuration
     */
    public JSONReader.Context config() {
        return getSerializer().getDeserializeConfig();
    }

    /**
     * Match or not
     *
     * @param ctx Handling context
     * @param mime Content type
     */
    @Override
    public boolean matched(Context ctx, String mime) {
        return serializer.matched(ctx, mime);
    }

    /**
     * Converting body
     *
     * @param ctx Handling context
     * @param mWrap Method wrappers
     */
    @Override
    protected Object changeBody(Context ctx, MethodWrap mWrap) throws Exception {
        return serializer.deserializeFromBody(ctx);
    }

    /**
     * 转换 value
     *
     * @param ctx Handling context
     * @param p Parameter wrappers
     * @param pi Parameter index
     * @param pt Parameter type
     * @param bodyObj Body object
     */
    @Override
    protected Object changeValue(Context ctx, ParamWrap p, int pi, Class<?> pt, Object bodyObj) throws Exception {
        if (p.spec().isRequiredPath() || p.spec().isRequiredCookie() || p.spec().isRequiredHeader()) {
            //If path、cookie, header?
            return super.changeValue(ctx, p, pi, pt, bodyObj);
        }

        if (p.spec().isRequiredBody() == false && ctx.paramMap().containsKey(p.spec().getName())) {
            //If path、queryString?
            return super.changeValue(ctx, p, pi, pt, bodyObj);
        }

        if (bodyObj == null) {
            return super.changeValue(ctx, p, pi, pt, bodyObj);
        }

        if (bodyObj instanceof JSONObject) {
            JSONObject tmp = (JSONObject) bodyObj;

            if (p.spec().isRequiredBody() == false) {
                //
                //If there is no body requirement; Try to find by attribute
                //
                if (tmp.containsKey(p.spec().getName())) {
                    //Supports conversions of generic types
                    if (p.spec().isGenericType()) {
                        return tmp.getObject(p.spec().getName(), p.getGenericType());
                    } else {
                        return tmp.getObject(p.spec().getName(), pt);
                    }
                }
            }

            //Try the body conversion
            if (pt.isPrimitive() || pt.getTypeName().startsWith("java.lang.")) {
                return super.changeValue(ctx, p, pi, pt, bodyObj);
            } else {
                if (List.class.isAssignableFrom(pt)) {
                    return null;
                }

                if (pt.isArray()) {
                    return null;
                }

                //Generic transformations such as Map<T>
                if (p.spec().isGenericType()) {
                    return tmp.to(p.getGenericType());
                } else {
                    return tmp.to(pt);
                }
            }
        }

        if (bodyObj instanceof JSONArray) {
            JSONArray tmp = (JSONArray) bodyObj;
            //If the argument is a non-collection type
            if (!Collection.class.isAssignableFrom(pt)) {
                return null;
            }
            //Collection Type Conversions
            if (p.spec().isGenericType()) {
                //Transforms a collection with generics
                return tmp.to(p.getGenericType());
            } else {
                //Can be converted not only to a List but also to a Set
                return tmp.to(pt);
            }
        }

        return bodyObj;
    }
}
