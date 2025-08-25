package com.alibaba.fastjson2.support.spring.http.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPObject;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Fastjson for Spring MVC Converter.
 *
 * @author Victor.Zxy
 * @see AbstractHttpMessageConverter
 * @see GenericHttpMessageConverter
 * @since 2.0.2
 */
public class FastJsonHttpMessageConverter
        extends AbstractHttpMessageConverter<Object>
        implements GenericHttpMessageConverter<Object> {
    public static final MediaType APPLICATION_JAVASCRIPT = new MediaType("application", "javascript");

    /**
     * with fastJson config
     */
    private FastJsonConfig config = new FastJsonConfig();

    /**
     * Can serialize/deserialize all types.
     */
    public FastJsonHttpMessageConverter() {
        super(MediaType.ALL);
        setDefaultCharset(StandardCharsets.UTF_8);
    }

    /**
     * @return the fastJsonConfig.
     */
    public FastJsonConfig getFastJsonConfig() {
        return config;
    }

    /**
     * @param fastJsonConfig the fastJsonConfig to set.
     */
    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.config = fastJsonConfig;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return super.canRead(contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return super.canWrite(clazz, mediaType);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readType(getType(type, contextClass), inputMessage);
    }

    @Override
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        // support StreamingHttpOutputMessage in spring4.0+
        super.write(o, contentType, outputMessage);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readType(getType(clazz, null), inputMessage);
    }

    /** Default initialization capacity when content-length is not specified */
    private static int REQUEST_BODY_INITIAL_CAPACITY = 1024;

    public static void setRequestBodyInitialCapacity(int initialCapacity) {
        if (initialCapacity < 128 || initialCapacity > 1024 * 1024) {
            throw new IllegalArgumentException("invalid initialCapacity: " + initialCapacity);
        }
        REQUEST_BODY_INITIAL_CAPACITY = initialCapacity;
    }

    /**
     * @param contentLength The content length of the request message. If -1 is passed, it means unknown.
     */
    protected static int calcInitialCapacity(long contentLength) {
        return contentLength == -1 || contentLength > Integer.MAX_VALUE
                ? REQUEST_BODY_INITIAL_CAPACITY
                // The maximum limit is 1MB to prevent fake request headers
                : (int) Math.min(contentLength, 1024 * 1024);
    }

    /**
     * @param in the specified input stream
     * @param contentLength -1 means unknown
     */
    protected static byte[] fastRead(final InputStream in, final long contentLength) throws IOException {
        final int expectSize = calcInitialCapacity(contentLength);
        byte[] body = new byte[expectSize];

        int offset = in.read(body, 0, body.length);
        if (offset == -1) {
            body = new byte[0];
        } else if (contentLength == -1 || offset != contentLength) {
            final byte[] buf = new byte[1024];
            int len = in.read(buf);
            while (len != -1) { // Refer to the implementation of ByteArrayOutputStream
                final int minRequired = offset + len;
                final int oldLength = body.length;
                if (minRequired > oldLength) {
                    int newLength = newLength(oldLength, minRequired - oldLength, oldLength);
                    byte[] newBody = Arrays.copyOf(body, newLength);
                    System.arraycopy(buf, 0, newBody, offset, len);
                    body = newBody;
                } else {
                    System.arraycopy(buf, 0, body, offset, len);
                }
                offset = minRequired;
                len = in.read(buf);
            }
            if (offset != body.length) {
                body = Arrays.copyOf(body, offset);
            }
        }
        return body;
    }

    // see jdk.internal.util.ArraysSupport.SOFT_MAX_ARRAY_LENGTH
    public static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    // see jdk.internal.util.ArraysSupport.newLength( )
    public static int newLength(int oldLength, int minGrowth, int prefGrowth) {
        // preconditions not checked because of inlining
        // assert oldLength >= 0
        // assert minGrowth > 0

        int prefLength = oldLength + Math.max(minGrowth, prefGrowth); // might overflow
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            // put code cold in a separate method
            return hugeLength(oldLength, minGrowth);
        }
    }

    // see jdk.internal.util.ArraysSupport.hugeLength( )
    private static int hugeLength(int oldLength, int minGrowth) {
        int minLength = oldLength + minGrowth;
        if (minLength < 0) { // overflow
            throw new OutOfMemoryError("Required array length " + oldLength + " + " + minGrowth + " is too large");
        } else if (minLength <= SOFT_MAX_ARRAY_LENGTH) {
            return SOFT_MAX_ARRAY_LENGTH;
        } else {
            return minLength;
        }
    }

    protected Object readType(Type type, HttpInputMessage inputMessage) {
        final long contentLength = inputMessage.getHeaders().getContentLength(); // -1 表示未知
        try {
            final byte[] body = fastRead(inputMessage.getBody(), contentLength);
            return JSON.parseObject(body, type, config.readerContext());
        } catch (JSONException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex, inputMessage);
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        HttpHeaders headers = outputMessage.getHeaders();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int contentLength;
            if (object instanceof String && JSON.isValidObject((String) object)) {
                byte[] strBytes = ((String) object).getBytes(config.getCharset());
                contentLength = strBytes.length;
                outputMessage.getBody().write(strBytes, 0, strBytes.length);
            } else if (object instanceof byte[] && JSON.isValid((byte[]) object)) {
                byte[] strBytes = (byte[]) object;
                contentLength = strBytes.length;
                outputMessage.getBody().write(strBytes, 0, strBytes.length);
            } else {
                if (object instanceof JSONPObject) {
                    headers.setContentType(APPLICATION_JAVASCRIPT);
                }

                contentLength = JSON.writeTo(
                        baos, object, config.writerContext()
                );
            }

            if (headers.getContentLength() < 0 && config.isWriteContentLength()) {
                headers.setContentLength(contentLength);
            }
            baos.writeTo(outputMessage.getBody());
        } catch (JSONException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new HttpMessageNotWritableException("I/O error while writing output message", ex);
        }
    }

    protected Type getType(Type type, Class<?> contextClass) {
        if (Spring4TypeResolvableHelper.isSupport()) {
            return Spring4TypeResolvableHelper.getType(type, contextClass);
        }
        return type;
    }

    private static class Spring4TypeResolvableHelper {
        private static boolean hasClazzResolvableType;

        static {
            try {
                Class.forName("org.springframework.core.ResolvableType");
                hasClazzResolvableType = true;
            } catch (ClassNotFoundException e) {
                hasClazzResolvableType = false;
            }
        }

        private static boolean isSupport() {
            return hasClazzResolvableType;
        }

        private static Type getType(Type type, Class<?> contextClass) {
            if (contextClass != null) {
                ResolvableType resolvedType = ResolvableType.forType(type);
                if (type instanceof TypeVariable) {
                    ResolvableType resolvedTypeVariable = resolveVariable((TypeVariable) type, ResolvableType.forClass(contextClass));
                    if (resolvedTypeVariable != ResolvableType.NONE) {
                        return resolvedTypeVariable.resolve();
                    }
                } else if (type instanceof ParameterizedType && resolvedType.hasUnresolvableGenerics()) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Class<?>[] generics = new Class[parameterizedType.getActualTypeArguments().length];
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();

                    for (int i = 0; i < typeArguments.length; ++i) {
                        Type typeArgument = typeArguments[i];
                        if (typeArgument instanceof TypeVariable) {
                            ResolvableType resolvedTypeArgument = resolveVariable((TypeVariable) typeArgument, ResolvableType.forClass(contextClass));
                            if (resolvedTypeArgument != ResolvableType.NONE) {
                                generics[i] = resolvedTypeArgument.resolve();
                            } else {
                                generics[i] = ResolvableType.forType(typeArgument).resolve();
                            }
                        } else {
                            generics[i] = ResolvableType.forType(typeArgument).resolve();
                        }
                    }

                    return ResolvableType.forClassWithGenerics(resolvedType.getRawClass(), generics).getType();
                }
            }

            return type;
        }

        private static ResolvableType resolveVariable(TypeVariable<?> typeVariable, ResolvableType contextType) {
            ResolvableType resolvedType;
            if (contextType.hasGenerics()) {
                resolvedType = ResolvableType.forType(typeVariable, contextType);
                if (resolvedType.resolve() != null) {
                    return resolvedType;
                }
            }

            ResolvableType superType = contextType.getSuperType();
            if (superType != ResolvableType.NONE) {
                resolvedType = resolveVariable(typeVariable, superType);
                if (resolvedType.resolve() != null) {
                    return resolvedType;
                }
            }
            for (ResolvableType ifc : contextType.getInterfaces()) {
                resolvedType = resolveVariable(typeVariable, ifc);
                if (resolvedType.resolve() != null) {
                    return resolvedType;
                }
            }
            return ResolvableType.NONE;
        }
    }
}
