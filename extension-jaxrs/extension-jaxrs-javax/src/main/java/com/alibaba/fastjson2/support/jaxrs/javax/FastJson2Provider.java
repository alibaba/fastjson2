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
 * Fastjson2 provider for JAX-RS (javax namespace).
 * Provides JSON serialization and deserialization for JAX-RS REST services.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @ApplicationPath("/api")
 * public class RestApplication extends Application {
 *     @Override
 *     public Set<Class<?>> getClasses() {
 *         Set<Class<?>> classes = new HashSet<>();
 *         classes.add(FastJson2Provider.class);
 *         return classes;
 *     }
 * }
 * }</pre>
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
     * Classes excluded from deserialization.
     * These types are never deserialized by this provider.
     */
    public static final Class<?>[] DEFAULT_UNREADABLES = new Class<?>[]{
            InputStream.class, Reader.class
    };

    /**
     * Classes excluded from serialization.
     * These types are never serialized by this provider.
     */
    public static final Class<?>[] DEFAULT_UNWRITABLES = new Class<?>[]{
            InputStream.class,
            OutputStream.class, Writer.class,
            StreamingOutput.class, Response.class
    };

    /**
     * Injectable context for locating configured FastJsonConfig instances.
     * Used for JAX-RS context resolution.
     */
    @Context
    protected Providers providers;

    /**
     * Configuration for Fastjson2 behavior.
     */
    @Getter
    @Setter
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    /**
     * Whitelist of classes allowed for serialization/deserialization.
     * If null, all types are allowed.
     */
    private Class<?>[] clazzes;

    /**
     * Creates a provider that can handle all types.
     */
    public FastJson2Provider() {
        this(null);
    }

    /**
     * Creates a provider restricted to specific types.
     *
     * @param clazzes whitelist of allowed classes, or null for all types
     */
    public FastJson2Provider(Class<?>[] clazzes) {
        this.clazzes = clazzes;
    }

    /**
     * Checks if a type is assignable from any class in the exclusion list.
     *
     * @param type the type to check
     * @param classes the exclusion classes
     * @return true if the type should be excluded
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
     * Validates whether a class can be serialized or deserialized.
     * Checks against the configured class whitelist if present.
     *
     * @param type the class to check
     * @param classAnnotations annotations on the class
     * @return true if the type is valid for processing
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
     * Checks if the media type is not compatible with JSON.
     * Accepts json, javascript, x-json, x-javascript, and form-urlencoded subtypes.
     *
     * @param mediaType the media type to check
     * @return true if the media type does not match JSON formats
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
     * Determines if this provider can serialize the given type.
     * Called by JAX-RS container to check provider capability.
     *
     * @param type the class to serialize
     * @param genericType the generic type
     * @param annotations annotations on the element
     * @param mediaType the media type
     * @return true if this provider can write the type
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
     * Serializes the given object to JSON.
     * Called by JAX-RS container to write response body.
     *
     * @param object the object to serialize
     * @param type the class type
     * @param genericType the generic type
     * @param annotations annotations on the element
     * @param mediaType the media type
     * @param httpHeaders HTTP headers
     * @param entityStream the output stream
     * @throws IOException if an I/O error occurs
     * @throws WebApplicationException if serialization fails
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
     * Determines if this provider can deserialize the given type.
     * Called by JAX-RS container to check provider capability.
     *
     * @param type the class to deserialize
     * @param genericType the generic type
     * @param annotations annotations on the element
     * @param mediaType the media type
     * @return true if this provider can read the type
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
     * Deserializes JSON to an object.
     * Called by JAX-RS container to read request body.
     *
     * @param type the class type
     * @param genericType the generic type
     * @param annotations annotations on the element
     * @param mediaType the media type
     * @param httpHeaders HTTP headers
     * @param entityStream the input stream
     * @return the deserialized object
     * @throws IOException if an I/O error occurs
     * @throws WebApplicationException if deserialization fails
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
     * Locates the FastJsonConfig to use for the given type and media type.
     * Attempts to resolve from JAX-RS context providers, falls back to default config.
     *
     * @param type the class type
     * @param mediaType the media type
     * @return the FastJsonConfig to use
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
