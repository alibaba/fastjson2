package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.codec.BeanInfo;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author kraity
 * @since 2.0.39
 */
public class KotlinUtils {
    public static final int STATE;
    private static volatile Class kotlin_metadata;
    private static volatile boolean kotlin_metadata_error;
    private static volatile boolean kotlin_class_klass_error;
    private static volatile Constructor kotlin_kclass_constructor;
    private static volatile Method kotlin_kclass_getConstructors;
    private static volatile Method kotlin_kfunction_getParameters;
    private static volatile Method kotlin_kparameter_getName;
    private static volatile boolean kotlin_error;
    private static volatile Map<Class, String[]> kotlinIgnores;
    private static volatile boolean kotlinIgnores_error;

    static {
        int state = 0;
        try {
            Class.forName("kotlin.Metadata");
            state++;
            Class.forName("kotlin.reflect.jvm.ReflectJvmMapping");
            state++;
        } catch (Throwable e) {
            // Nothing
        }
        STATE = state;
    }

    private KotlinUtils() {
        throw new IllegalStateException();
    }

    /**
     * Gets the target constructor and its
     * parameter names of the specified {@code clazz}
     *
     * @param clazz the specified class for search
     */
    public static void getConstructor(Class<?> clazz, BeanInfo beanInfo) {
        int creatorParams = 0;
        Constructor<?> creatorConstructor = null;

        String[] paramNames = beanInfo.createParameterNames;
        Constructor<?>[] constructors = BeanUtils.getConstructor(clazz);

        for (Constructor<?> constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            if (paramNames != null && paramCount != paramNames.length) {
                continue;
            }

            if (paramCount > 2) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes[paramCount - 2] == int.class &&
                        parameterTypes[paramCount - 1] == DefaultConstructorMarker.class
                ) {
                    beanInfo.markerConstructor = constructor;
                    continue;
                }
            }

            if (creatorConstructor == null || creatorParams < paramCount) {
                creatorParams = paramCount;
                creatorConstructor = constructor;
            }
        }

        if (creatorParams != 0 && STATE == 2) {
            try {
                List<KParameter> params = null;
                KClass<?> kClass = Reflection.getOrCreateKotlinClass(clazz);

                for (KFunction<?> function : kClass.getConstructors()) {
                    List<KParameter> parameters = function.getParameters();
                    if (params == null || creatorParams == parameters.size()) {
                        params = parameters;
                    }
                }

                if (params != null) {
                    String[] names = new String[params.size()];
                    for (int i = 0, m = names.length; i < m; i++) {
                        names[i] = params.get(i).getName();
                    }
                    beanInfo.createParameterNames = names;
                }
            } catch (Throwable e) {
                // Ignore this exception
            }
        }

        beanInfo.creatorConstructor = creatorConstructor;
    }

    public static boolean isKotlin(Class clazz) {
        if (kotlin_metadata == null && !kotlin_metadata_error) {
            try {
                kotlin_metadata = Class.forName("kotlin.Metadata");
            } catch (Throwable e) {
                kotlin_metadata_error = true;
            }
        }
        return kotlin_metadata != null && clazz.isAnnotationPresent(kotlin_metadata);
    }

    public static Constructor getKotlinConstructor(Constructor[] constructors) {
        return getKotlinConstructor(constructors, null);
    }

    public static Constructor getKotlinConstructor(Constructor[] constructors, String[] paramNames) {
        Constructor creatorConstructor = null;
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (paramNames != null && parameterTypes.length != paramNames.length) {
                continue;
            }
            // String equals to Class will always return false !
            if (parameterTypes.length > 0 && "kotlin.jvm.internal.DefaultConstructorMarker".equals(parameterTypes[parameterTypes.length - 1].getName())) {
                continue;
            }
            if (creatorConstructor != null && creatorConstructor.getParameterTypes().length >= parameterTypes.length) {
                continue;
            }
            creatorConstructor = constructor;
        }
        return creatorConstructor;
    }

    public static String[] getKoltinConstructorParameters(Class clazz) {
        if (kotlin_kclass_constructor == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_constructor = class_kotlin_kclass.getConstructor(Class.class);
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kclass_constructor == null) {
            return null;
        }

        if (kotlin_kclass_getConstructors == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_getConstructors = class_kotlin_kclass.getMethod("getConstructors");
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_kfunction_getParameters == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlin_kfunction = Class.forName("kotlin.reflect.KFunction");
                kotlin_kfunction_getParameters = class_kotlin_kfunction.getMethod("getParameters");
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_kparameter_getName == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlinn_kparameter = Class.forName("kotlin.reflect.KParameter");
                kotlin_kparameter_getName = class_kotlinn_kparameter.getMethod("getName");
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_error) {
            return null;
        }

        try {
            Object constructor = null;
            Object kclassImpl = kotlin_kclass_constructor.newInstance(clazz);
            Iterable it = (Iterable) kotlin_kclass_getConstructors.invoke(kclassImpl);
            for (Iterator iterator = it.iterator(); iterator.hasNext();) {
                Object item = iterator.next();
                List parameters = (List) kotlin_kfunction_getParameters.invoke(item);
                if (constructor != null && parameters.size() == 0) {
                    continue;
                }
                constructor = item;
            }

            if (constructor == null) {
                return null;
            }

            List parameters = (List) kotlin_kfunction_getParameters.invoke(constructor);
            String[] names = new String[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                names[i] = (String) kotlin_kparameter_getName.invoke(param);
            }
            return names;
        } catch (Throwable e) {
            e.printStackTrace();
            kotlin_error = true;
        }
        return null;
    }

    public static boolean isKotlinIgnore(Class clazz, String methodName) {
        if (kotlinIgnores == null && !kotlinIgnores_error) {
            try {
                Map<Class, String[]> map = new HashMap<>();
                Class charRangeClass = Class.forName("kotlin.ranges.CharRange");
                map.put(charRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class intRangeClass = Class.forName("kotlin.ranges.IntRange");
                map.put(intRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class longRangeClass = Class.forName("kotlin.ranges.LongRange");
                map.put(longRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class floatRangeClass = Class.forName("kotlin.ranges.ClosedFloatRange");
                map.put(floatRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class doubleRangeClass = Class.forName("kotlin.ranges.ClosedDoubleRange");
                map.put(doubleRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                kotlinIgnores = map;
            } catch (Throwable error) {
                kotlinIgnores_error = true;
            }
        }
        if (kotlinIgnores == null) {
            return false;
        }
        String[] ignores = kotlinIgnores.get(clazz);
        return ignores != null && Arrays.binarySearch(ignores, methodName) >= 0;
    }
}
