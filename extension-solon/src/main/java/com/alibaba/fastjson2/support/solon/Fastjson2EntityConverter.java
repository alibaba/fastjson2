package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.util.Assert;
import org.noear.solon.core.util.LazyReference;
import org.noear.solon.core.wrap.MethodWrap;
import org.noear.solon.core.wrap.ParamWrap;
import org.noear.solon.serialization.AbstractStringEntityConverter;
import org.noear.solon.serialization.SerializerNames;

import java.util.Collection;
import java.util.List;

/**
 * Fastjson2 EntityConverter
 *
 * @author noear
 * @since 3.6
 */
public class Fastjson2EntityConverter
        extends AbstractStringEntityConverter<Fastjson2StringSerializer> {
    public Fastjson2EntityConverter(Fastjson2StringSerializer serializer) {
        super(serializer);

        serializer.getDeserializeConfig().addFeatures(JSONReader.Feature.ErrorOnEnumNotMatch);
        serializer.getSerializeConfig().addFeatures(JSONWriter.Feature.BrowserCompatible);
    }

    /**
     * Suffix or name mapping
     */
    @Override
    public String[] mappings() {
        return new String[]{SerializerNames.AT_JSON};
    }

    /**
     * Change body
     *
     * @param ctx Handling context
     * @param mWrap Method wrappers
     */
    @Override
    protected Object changeBody(Context ctx, MethodWrap mWrap) throws Exception {
        return serializer.deserializeFromBody(ctx);
    }

    /**
     * Change value
     *
     * @param ctx Handling context
     * @param p Parameter wrappers
     * @param pi Parameter index
     * @param pt Parameter type
     * @param bodyRef BodyValue Reference
     */
    @Override
    protected Object changeValue(Context ctx, ParamWrap p, int pi, Class<?> pt, LazyReference bodyRef) throws Throwable {
        if (p.spec().isRequiredPath() || p.spec().isRequiredCookie() || p.spec().isRequiredHeader()) {
            //If path、cookie, header?
            return super.changeValue(ctx, p, pi, pt, bodyRef);
        }

        if (p.spec().isRequiredBody() == false && ctx.paramMap().containsKey(p.spec().getName())) {
            //If path、queryString?
            return super.changeValue(ctx, p, pi, pt, bodyRef);
        }

        Object bodyObj = bodyRef.get();

        if (bodyObj == null) {
            return super.changeValue(ctx, p, pi, pt, bodyRef);
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
                return super.changeValue(ctx, p, pi, pt, bodyRef);
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

    static class Fastjson2Decl<C, F> {
        private final boolean forSerialize;
        private C context;

        public Fastjson2Decl(C context) {
            this.context = context;

            if (context instanceof JSONWriter.Context) {
                forSerialize = true;
            } else {
                forSerialize = false;
            }
        }

        /**
         * Get context
         */
        public C getContext() {
            return context;
        }

        /**
         * Reset context
         */
        public void setContext(C context) {
            Assert.notNull(context, "context can not be null");
            this.context = context;
        }

        /**
         * Set features
         */
        public void setFeatures(F... features) {
            if (forSerialize) {
                ((JSONWriter.Context) context).setFeatures(JSONFactory.getDefaultWriterFeatures());
            } else {
                ((JSONReader.Context) context).setFeatures(JSONFactory.getDefaultReaderFeatures());
            }

            addFeatures(features);
        }

        /**
         * Add features
         */
        public void addFeatures(F... features) {
            if (forSerialize) {
                //序列化
                for (F f1 : features) {
                    JSONWriter.Feature feature = (JSONWriter.Feature) f1;
                    ((JSONWriter.Context) context).config(feature, true);
                }
            } else {
                for (F f1 : features) {
                    JSONReader.Feature feature = (JSONReader.Feature) f1;
                    ((JSONReader.Context) context).config(feature, true);
                }
            }
        }

        /**
         * Remove features
         */
        public void removeFeatures(F... features) {
            if (forSerialize) {
                //序列化
                for (F f1 : features) {
                    JSONWriter.Feature feature = (JSONWriter.Feature) f1;
                    ((JSONWriter.Context) context).config(feature, false);
                }
            } else {
                for (F f1 : features) {
                    JSONReader.Feature feature = (JSONReader.Feature) f1;
                    ((JSONReader.Context) context).config(feature, false);
                }
            }
        }
    }
}
