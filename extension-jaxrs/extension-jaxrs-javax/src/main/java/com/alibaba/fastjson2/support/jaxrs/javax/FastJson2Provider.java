package com.alibaba.fastjson2.support.jaxrs.javax;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Fastjson for JAX-RS Provider.
 * 参考：com.alibaba.fastjson.support.jaxrs.FastJsonProvider
 *
 * @author 张治保
 * @since 2024/10/16
 * @see MessageBodyReader
 * @see MessageBodyWriter
 */

@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class FastJson2Provider
        implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    /**
     * These are classes that we never use for reading
     * (never try to deserialize instances of these types).
     */
    public static final Class<?>[] DEFAULT_UNREADABLES = new Class<?>[]{
            InputStream.class, Reader.class
    };

    /**
     * These are classes that we never use for writing
     * (never try to serialize instances of these types).
     */
    public static final Class<?>[] DEFAULT_UNWRITABLES = new Class<?>[]{
            InputStream.class,
            OutputStream.class, Writer.class,
            StreamingOutput.class, Response.class
    };

    /**
     * Injectable context object used to locate configured
     * instance of {@link FastJsonConfig} to use for actual
     * serialization.
     */
    @Context
    protected Providers providers;

    /**
     * with fastJson config
     */
    @Getter
    @Setter
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    /**
     * allow serialize/deserialize types in clazzes
     */
    private Class<?>[] clazzes;

    /**
     * Can serialize/deserialize all types.
     */
    public FastJson2Provider() {
        this((Class<?>[]) null);
    }

    /**
     * Only serialize/deserialize all types in clazzes.
     */
    public FastJson2Provider(Class<?>[] clazzes) {
        this.clazzes = clazzes;
    }

    /**
     * Check some are interface/abstract classes to exclude.
     *
     * @param type the type
     * @param classes the classes
     * @return the boolean
     */
    protected boolean isNotAssignableFrom(Class<?> type, Class<?>[] classes) {
        if (type == null) {
            return true;
        }
        //  there are some other abstract/interface types to exclude too:
        for (Class<?> cls : classes) {
            if (cls.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a class can be serialized or deserialized. It can check
     * based on packages, annotations on entities or explicit classes.
     *
     * @param type class need to check
     * @return true if valid
     */
    protected boolean isValidType(Class<?> type, Annotation[] classAnnotations) {
        if (type == null) {
            return false;
        }
        if (clazzes != null) {
            for (Class<?> cls : clazzes) {
                // must strictly equal. Don't check inheritance
                if (cls == type) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Check media type like "application/json".
     *
     * @param mediaType media type
     * @return true if the media type is valid
     */
    protected boolean hasNotMatchingMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        String subtype = mediaType.getSubtype();
        return !(("json".equalsIgnoreCase(subtype))
                || (subtype.endsWith("+json"))
                || ("javascript".equals(subtype))
                || ("x-javascript".equals(subtype))
                || ("x-json".equals(subtype))
                || ("x-www-form-urlencoded".equalsIgnoreCase(subtype))
                || (subtype.endsWith("x-www-form-urlencoded")));
    }

    /**
     * Method that JAX-RS container calls to try to check whether given value
     * (of specified type) can be serialized by this provider.
     */
    @Override
    public boolean isWriteable(
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType) {
        if (hasNotMatchingMediaType(mediaType)) {
            return false;
        }
        if (isNotAssignableFrom(type, DEFAULT_UNWRITABLES)) {
            return false;
        }
        return isValidType(type, annotations);
    }

    /**
     * Method that JAX-RS container calls to serialize given value.
     */
    @Override
    public void writeTo(
            Object object,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream
    ) throws IOException, WebApplicationException {
        FastJsonConfig config = locateConfigProvider(type, mediaType);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (object instanceof String && JSON.isValidObject((String) object)) {
                byte[] strBytes = ((String) object).getBytes(config.getCharset());
                baos.write(strBytes, 0, strBytes.length);
            } else if (object instanceof byte[] && JSON.isValid((byte[]) object)) {
                byte[] strBytes = (byte[]) object;
                baos.write(strBytes, 0, strBytes.length);
            } else {
                JSON.writeTo(
                        baos,
                        object,
                        config.getDateFormat(),
                        config.getWriterFilters(),
                        config.getWriterFeatures()
                );
            }
            baos.writeTo(entityStream);
        }
    }

    /**
     * Method that JAX-RS container calls to try to check whether values of
     * given type (and media type) can be deserialized by this provider.
     */
    @Override
    public boolean isReadable(
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType) {
        if (hasNotMatchingMediaType(mediaType)) {
            return false;
        }
        if (isNotAssignableFrom(type, DEFAULT_UNREADABLES)) {
            return false;
        }
        return isValidType(type, annotations);
    }

    /**
     * Method that JAX-RS container calls to deserialize given value.
     */
    @Override
    public Object readFrom(
            Class<Object> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream
    ) throws IOException, WebApplicationException {
        FastJsonConfig config = locateConfigProvider(type, mediaType);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024 * 64];
            for (; ; ) {
                int len = entityStream.read(buf);
                if (len == -1) {
                    break;
                }
                if (len > 0) {
                    baos.write(buf, 0, len);
                }
            }
            byte[] bytes = baos.toByteArray();
            return JSON.parseObject(bytes, genericType, config.getDateFormat(), config.getReaderFilters(), config.getReaderFeatures());
        } catch (JSONException ex) {
            throw new WebApplicationException(ex);
        }
    }

    /**
     * Helper method that is called if no config has been explicitly configured.
     */
    protected FastJsonConfig locateConfigProvider(Class<?> type, MediaType mediaType) {
        if (providers != null) {
            ContextResolver<FastJsonConfig> resolver = providers.getContextResolver(FastJsonConfig.class, mediaType);
            if (resolver == null) {
                resolver = providers.getContextResolver(FastJsonConfig.class, null);
            }
            if (resolver != null) {
                return resolver.getContext(type);
            }
        }
        return fastJsonConfig;
    }
}
