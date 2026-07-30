package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

import static com.alibaba.fastjson2.writer.ObjectWriterProvider.NAME_COMPATIBLE_WITH_FILED;

/**
 * ObjectWriterCreator is responsible for creating ObjectWriter instances for
 * serializing Java objects into JSON format. It provides factory methods for
 * creating ObjectWriters for various types of objects and fields.
 *
 * <p>This class supports various features including:
 * <ul>
 *   <li>Creation of ObjectWriters for different object types</li>
 *   <li>Creation of FieldWriters for different field types</li>
 *   <li>Lambda expression support for getter methods</li>
 *   <li>Custom field writer creation with various configurations</li>
 *   <li>JIT compilation support for improved performance</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Get default creator
 * ObjectWriterCreator creator = JSONFactory.getDefaultObjectWriterCreator();
 *
 * // Create ObjectWriter for a class
 * ObjectWriter&lt;User&gt; writer = creator.createObjectWriter(User.class);
 *
 * // Create FieldWriter for a field
 * Field field = User.class.getDeclaredField("name");
 * FieldWriter&lt;User&gt; fieldWriter = creator.createFieldWriter("name", null, field);
 * </pre>
 *
 * @since 2.0.0
 */
public class ObjectWriterCreator {
    public static final ObjectWriterCreator INSTANCE = new ObjectWriterCreator();

    static final Map<Class, LambdaInfo> lambdaMapping = new HashMap<>();

    static {
        lambdaMapping.put(boolean.class, new LambdaInfo(boolean.class, Predicate.class, "test"));
        lambdaMapping.put(char.class, new LambdaInfo(char.class, ToCharFunction.class, "applyAsChar"));
        lambdaMapping.put(byte.class, new LambdaInfo(byte.class, ToByteFunction.class, "applyAsByte"));
        lambdaMapping.put(short.class, new LambdaInfo(short.class, ToShortFunction.class, "applyAsShort"));
        lambdaMapping.put(int.class, new LambdaInfo(int.class, ToIntFunction.class, "applyAsInt"));
        lambdaMapping.put(long.class, new LambdaInfo(long.class, ToLongFunction.class, "applyAsLong"));
        lambdaMapping.put(float.class, new LambdaInfo(float.class, ToFloatFunction.class, "applyAsFloat"));
        lambdaMapping.put(double.class, new LambdaInfo(double.class, ToDoubleFunction.class, "applyAsDouble"));
    }

    protected final AtomicInteger jitErrorCount = new AtomicInteger();
    protected volatile Throwable jitErrorLast;

    /**
     * Constructs a new ObjectWriterCreator instance.
     */
    public ObjectWriterCreator() {
    }

    /**
     * Creates an ObjectWriter for the specified list of FieldWriters.
     *
     * @param fieldWriters the list of FieldWriters to use
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(List fieldWriters) {
        return null;
    }

    /**
     * Creates an ObjectWriter for the specified array of FieldWriters.
     *
     * @param fieldWriters the array of FieldWriters to use
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(ObjectWriter... fieldWriters) {
        return null;
    }

    /**
     * Creates an ObjectWriter for the specified object type with names, types, and supplier.
     *
     * @param <T> the type of objects that the ObjectWriter can serialize
     * @param names the field names
     * @param types the field types
     * @param supplier the FieldSupplier to use
     * @return an ObjectWriter instance
     */
    public <T> ObjectWriter<T> createObjectWriter(String[] names, Type[] types, FieldSupplier<T> supplier) {
        return null;
    }

    /**
     * Creates an ObjectWriter for the specified object type.
     *
     * @param objectType the class of objects to serialize
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(Class objectType) {
        return createObjectWriter(
                objectType,
                0,
                JSONFactory.getDefaultObjectWriterProvider()
        );
    }

    /**
     * Creates an ObjectWriter for the specified object type and field writers.
     *
     * @param objectType the class of objects to serialize
     * @param fieldWriters the field writers to use
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(Class objectType,
                                           ObjectWriter... fieldWriters) {
        return null;
    }

    /**
     * Creates an ObjectWriter for the specified object class, features, and field writers.
     *
     * @param objectClass the class of objects to serialize
     * @param features the features to use
     * @param fieldWriters the field writers to use
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(
            Class objectClass,
            long features,
            ObjectWriter... fieldWriters
    ) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified field.
     *
     * @param objectClass the class containing the field
     * @param writerFeatures the writer features to use
     * @param provider the ObjectWriterProvider to use
     * @param beanInfo the BeanInfo to use
     * @param fieldInfo the FieldInfo to use
     * @param field the Field to create a writer for
     * @return a FieldWriter instance, or null if the field should be ignored
     */
    protected ObjectWriter createFieldWriter(
            Class objectClass,
            long writerFeatures,
            ObjectWriterProvider provider,
            BeanInfo beanInfo,
            FieldInfo fieldInfo,
            Field field
    ) {
        return null;
    }

    /**
     * Creates an ObjectWriter for the specified object class, features, and modules.
     *
     * @param objectClass the class of objects to serialize
     * @param features the features to use
     * @param modules the ObjectWriterModules to use
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(
            Class objectClass,
            long features,
            final List<ObjectWriterModule> modules
    ) {
        ObjectWriterProvider provider = null;
        for (ObjectWriterModule module : modules) {
            if (provider == null) {
                provider = module.getProvider();
            }
        }
        return createObjectWriter(objectClass, features, provider);
    }

    /**
     * Sets default values for the specified field writers using the default constructor of the object class.
     *
     * @param fieldWriters the list of FieldWriters to set default values for
     * @param objectClass the class of objects to create default instances from
     */
    protected void setDefaultValue(List fieldWriters, Class objectClass) {
    }

    /**
     * Creates an ObjectWriter for the specified object class, features, and provider.
     * This is the main method for creating ObjectWriters that handles all the complexity
     * of analyzing the class structure and creating appropriate FieldWriters.
     *
     * @param objectClass the class of objects to serialize
     * @param features the features to use
     * @param provider the ObjectWriterProvider to use
     * @return an ObjectWriter instance
     */
    public ObjectWriter createObjectWriter(
            final Class objectClass,
            final long features,
            final ObjectWriterProvider provider
    ) {
        BeanInfo beanInfo = provider.createBeanInfo();
        beanInfo.readerFeatures |= FieldInfo.JIT;

        provider.getBeanInfo(beanInfo, objectClass);

        Class serializer = beanInfo.serializer;
        if (serializer != null && ObjectWriter.class.isAssignableFrom(serializer)) {
            try {
                Constructor constructor = serializer.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectWriter) constructor.newInstance();
            } catch (Exception e) {
                throw new JSONException("create serializer error", e);
            }
        }

        return null;
    }

    /**
     * Gets the field name for the specified method based on various naming strategies and configurations.
     *
     * @param objectClass the class containing the method
     * @param provider the ObjectWriterProvider to use
     * @param beanInfo the BeanInfo containing configuration
     * @param record whether the class is a record
     * @param fieldInfo the FieldInfo containing field configuration
     * @param method the method to get the field name for
     * @return the field name
     */
    protected static String getFieldName(
            Class objectClass,
            ObjectWriterProvider provider,
            BeanInfo beanInfo,
            boolean record,
            FieldInfo fieldInfo,
            Method method
    ) {
        String fieldName;
        if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
            if (record) {
                fieldName = method.getName();
            } else {
                fieldName = BeanUtils.getterName(method, beanInfo.kotlin, beanInfo.namingStrategy);

                Field field;
                if ((provider.userDefineMask & NAME_COMPATIBLE_WITH_FILED) != 0
                        && (field = BeanUtils.getField(objectClass, method)) != null) {
                    fieldName = field.getName();
                } else {
                    char c0 = '\0', c1;
                    int len = fieldName.length();
                    if (len > 0) {
                        c0 = fieldName.charAt(0);
                    }

                    if ((len == 1 && c0 >= 'a' && c0 <= 'z')
                            || (len > 1 && c0 >= 'A' && c0 <= 'Z' && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z')
                    ) {
                        char[] chars = fieldName.toCharArray();
                        if (c0 >= 'a') {
                            chars[0] = (char) (chars[0] - 32);
                        } else {
                            chars[0] = (char) (chars[0] + 32);
                        }
                        String fieldName1 = new String(chars);
                        field = BeanUtils.getDeclaredField(objectClass, fieldName1);

                        if (field != null) {
                            boolean ucaseAll = true;
                            for (int i = 2; i < chars.length; i++) {
                                char c = chars[i];
                                if (c >= 'a' && c <= 'z') {
                                    ucaseAll = false;
                                    break;
                                }
                            }
                            if (ucaseAll || Modifier.isPublic(field.getModifiers())) {
                                fieldName = field.getName();
                            }
                        }
                    }
                }
            }
        } else {
            fieldName = fieldInfo.fieldName;
        }
        return fieldName;
    }

    /**
     * Configures serialize filters for the specified ObjectWriterAdapter.
     *
     * @param beanInfo the BeanInfo containing filter configuration
     * @param writerAdapter the ObjectWriterAdapter to configure filters for
     */
    protected static void configSerializeFilters(BeanInfo beanInfo, ObjectWriter writerAdapter) {
    }

    /**
     * Handles field ignores based on the BeanInfo configuration.
     *
     * @param beanInfo the BeanInfo containing ignore configuration
     * @param fieldWriters the list of FieldWriters to process
     */
    protected void handleIgnores(BeanInfo beanInfo, List fieldWriters) {
    }

    /**
     * Creates a FieldWriter for the specified field with default configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param format the date format to use
     * @param field the Field to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(String fieldName, String format, Field field) {
        return createFieldWriter(JSONFactory.getDefaultObjectWriterProvider(), fieldName, 0, 0L, format, null, field, null);
    }

    /**
     * Creates a FieldWriter for the specified field with ordinal and features.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param field the Field to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Field field
    ) {
        return createFieldWriter(JSONFactory.getDefaultObjectWriterProvider(), fieldName, ordinal, features, format, null, field, null);
    }

    /**
     * Creates a FieldWriter for the specified field with comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param label the label for the field
     * @param field the Field to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            ObjectWriter initObjectWriter
    ) {
        return createFieldWriter(JSONFactory.getDefaultObjectWriterProvider(), fieldName, ordinal, features, format, label, field, initObjectWriter);
    }

    /**
     * Creates a FieldWriter for the specified field with provider and comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param provider the ObjectWriterProvider to use
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param label the label for the field
     * @param field the Field to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @return a FieldWriter instance
     */
    public final <T> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            ObjectWriter initObjectWriter
    ) {
        return createFieldWriter(
                provider,
                fieldName,
                ordinal,
                features,
                format,
                null,
                label,
                field,
                initObjectWriter
        );
    }

    /**
     * Creates a FieldWriter for the specified field with locale and comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param provider the ObjectWriterProvider to use
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param locale the locale to use
     * @param label the label for the field
     * @param field the Field to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Field field,
            ObjectWriter initObjectWriter
    ) {
        return createFieldWriter(
                provider,
                fieldName,
                ordinal,
                features,
                format,
                locale,
                label,
                field,
                initObjectWriter,
                null
        );
    }

    /**
     * Creates a FieldWriter for the specified field with contentAs and comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param provider the ObjectWriterProvider to use
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param locale the locale to use
     * @param label the label for the field
     * @param field the Field to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @param contentAs the contentAs class
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Field field,
            ObjectWriter initObjectWriter,
            Class<?> contentAs
    ) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified method with default configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param objectType the class containing the method
     * @param fieldName the name of the field
     * @param dateFormat the date format to use
     * @param method the Method to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(Class<T> objectType,
                                                String fieldName,
                                                String dateFormat,
                                                Method method) {
        return createFieldWriter(objectType, fieldName, 0, 0, dateFormat, method);
    }

    public <T> ObjectWriter<T> createFieldWriter(
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Method method) {
        return createFieldWriter(null, objectType, fieldName, ordinal, features, format, null, method, null);
    }

    /**
     * Creates a FieldWriter for the specified method with comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param provider the ObjectWriterProvider to use
     * @param objectType the class containing the method
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param label the label for the field
     * @param method the Method to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            ObjectWriter initObjectWriter
    ) {
        return createFieldWriter(
                provider,
                objectType,
                fieldName,
                ordinal,
                features,
                format,
                null,
                label,
                method,
                initObjectWriter
        );
    }

    /**
     * Creates a FieldWriter for the specified method with locale and comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param provider the ObjectWriterProvider to use
     * @param objectType the class containing the method
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param locale the locale to use
     * @param label the label for the field
     * @param method the Method to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Method method,
            ObjectWriter initObjectWriter
    ) {
        return createFieldWriter(provider, objectType, fieldName, ordinal, features, format, locale, label, method, initObjectWriter, null);
    }

    /**
     * Creates a FieldWriter for the specified method with contentAs and comprehensive configuration.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param provider the ObjectWriterProvider to use
     * @param objectType the class containing the method
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param locale the locale to use
     * @param label the label for the field
     * @param method the Method to create a writer for
     * @param initObjectWriter the initial ObjectWriter to use
     * @param contentAs the contentAs class
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Method method,
            ObjectWriter initObjectWriter,
            Class<?> contentAs
    ) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns a long value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToLongFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToLongFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns an int value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToIntFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToIntFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified field, method, and function that returns an int value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param field the Field to create a writer for
     * @param method the Method to create a writer for
     * @param function the ToIntFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, Field field, Method method, ToIntFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns a short value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToShortFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToShortFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns a byte value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToByteFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToByteFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns a float value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToFloatFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToFloatFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns a double value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToDoubleFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToDoubleFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function that returns a char value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the ToCharFunction to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, ToCharFunction<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified predicate function that returns a boolean value.
     *
     * @param <T> the type of objects that the FieldWriter can serialize
     * @param fieldName the name of the field
     * @param function the Predicate to create a writer for
     * @return a FieldWriter instance
     */
    public <T> ObjectWriter createFieldWriter(String fieldName, Predicate<T> function) {
        return null;
    }

    /**
     * Creates a FieldWriter for the specified function with default configuration.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param fieldName the name of the field
     * @param fieldClass the class of the field
     * @param function the Function to create a writer fork
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter createFieldWriter(
            String fieldName,
            Class fieldClass,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, 0, null, null, fieldClass, fieldClass, null, function);
    }

    /**
     * Creates a FieldWriter for the specified field, method, and function.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param fieldName the name of the field
     * @param fieldClass the class of the field
     * @param field the Field to create a writer for
     * @param method the Method to create a writer for
     * @param function the Function to create a writer for
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter createFieldWriter(
            String fieldName,
            Class fieldClass,
            Field field,
            Method method,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, 0, null, null, fieldClass, fieldClass, field, method, function);
    }

    /**
     * Creates a FieldWriter for the specified function with field type and class.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param fieldName the name of the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param function the Function to create a writer for
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter createFieldWriter(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, 0, null, null, fieldType, fieldClass, null, function);
    }

    /**
     * Creates a FieldWriter for the specified function with features and format.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param fieldName the name of the field
     * @param features the features to use
     * @param format the date format to use
     * @param fieldClass the class of the field
     * @param function the Function to create a writer for
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter createFieldWriter(
            String fieldName,
            long features,
            String format,
            Class fieldClass,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, features, format, null, fieldClass, fieldClass, null, function);
    }

    /**
     * Creates a FieldWriter for the specified function with provider, object class, and comprehensive configuration.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param provider the ObjectWriterProvider to use
     * @param objectClass the class containing the field
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param label the label for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param method the Method to create a writer for
     * @param function the Function to create a writer for
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class<V> fieldClass,
            Method method,
            Function<T, V> function
    ) {
        return createFieldWriter(
                provider, objectClass, fieldName, ordinal, features, format, null, label, fieldType, fieldClass, null, method, function);
    }

    /**
     * Creates a FieldWriter for the specified function with provider, object class, field, method, and comprehensive configuration.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param provider the ObjectWriterProvider to use
     * @param objectClass the class containing the field
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param label the label for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param field the Field to create a writer for
     * @param method the Method to create a writer for
     * @param function the Function to create a writer for
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class<V> fieldClass,
            Field field,
            Method method,
            Function<T, V> function
    ) {
        return createFieldWriter(
                provider,
                objectClass,
                fieldName,
                ordinal,
                features,
                format,
                null,
                label,
                fieldType,
                fieldClass,
                field,
                method,
                function
        );
    }

    /**
     * Creates a FieldWriter for the specified function with provider, object class, locale, and comprehensive configuration.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param provider the ObjectWriterProvider to use
     * @param objectClass the class containing the field
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param locale the locale to use
     * @param label the label for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param field the Field to create a writer for
     * @param method the Method to create a writer for
     * @param function the Function to create a writer for
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class<V> fieldClass,
            Field field,
            Method method,
            Function<T, V> function
    ) {
        return createFieldWriter(
                provider,
                objectClass,
                fieldName,
                ordinal,
                features,
                format,
                locale,
                label,
                fieldType,
                fieldClass,
                field,
                method,
                function,
                null
        );
    }

    /**
     * Creates a FieldWriter for the specified function with provider, object class, contentAs, and comprehensive configuration.
     *
     * @param <T> the type of objects that owns the field
     * @param <V> the type of field values
     * @param provider the ObjectWriterProvider to use
     * @param objectClass the class containing the field
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use
     * @param format the date format to use
     * @param locale the locale to use
     * @param label the label for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param field the Field to create a writer for
     * @param method the Method to create a writer for
     * @param function the Function to create a writer for
     * @param contentAs the contentAs class
     * @return a FieldWriter instance
     */
    public <T, V> ObjectWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class<V> fieldClass,
            Field field,
            Method method,
            Function<T, V> function,
            Class<?> contentAs
    ) {
        return null;
    }

    static class LambdaInfo {
        final Class fieldClass;
        final Class supplierClass;
        final String methodName;
        final MethodType methodType;
        final MethodType invokedType;
        final MethodType samMethodType;

        LambdaInfo(Class fieldClass, Class supplierClass, String methodName) {
            this.fieldClass = fieldClass;
            this.supplierClass = supplierClass;
            this.methodName = methodName;
            this.methodType = MethodType.methodType(fieldClass);
            this.invokedType = MethodType.methodType(supplierClass);
            this.samMethodType = MethodType.methodType(fieldClass, Object.class);
        }
    }

    Object lambdaGetter(Class objectClass, Class fieldClass, Method method) {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);

        LambdaInfo buildInfo = lambdaMapping.get(fieldClass);

        MethodType methodType;
        MethodType invokedType;
        String methodName;
        MethodType samMethodType;
        if (buildInfo != null) {
            methodType = buildInfo.methodType;
            invokedType = buildInfo.invokedType;
            methodName = buildInfo.methodName;
            samMethodType = buildInfo.samMethodType;
        } else {
            methodType = MethodType.methodType(fieldClass);
            invokedType = METHOD_TYPE_FUNCTION;
            methodName = "apply";
            samMethodType = METHOD_TYPE_OBJECT_OBJECT;
        }

        try {
            MethodHandle target = lookup.findVirtual(objectClass, method.getName(), methodType);
            MethodType instantiatedMethodType = target.type();

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    methodName,
                    invokedType,
                    samMethodType,
                    target,
                    instantiatedMethodType
            );

            return callSite
                    .getTarget()
                    .invoke();
        } catch (Throwable e) {
            throw new JSONException("create fieldLambdaGetter error, method : " + method, e);
        }
    }

    protected ObjectWriter getInitWriter(ObjectWriterProvider provider, Class fieldClass) {
        return null;
    }

    <T> ObjectWriter<T> createFieldWriterLambda(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            ObjectWriter initObjectWriter,
            Class<?> contentAs
    ) {
        return null;
    }

    <T> ObjectWriter<T> createFieldWriterLambda(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Method method,
            ObjectWriter initObjectWriter,
            Class<?> contentAs
    ) {
        return null;
    }
}
