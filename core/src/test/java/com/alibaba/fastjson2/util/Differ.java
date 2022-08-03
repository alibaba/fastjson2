package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class Differ {
    final Object left;
    final Object right;
    boolean skipTransient = true;
    String leftName = "left";
    String rightName = "right";
    boolean referenceDetect = true;

    IdentityHashMap<Object, JSONWriter.Path> leftReferences = new IdentityHashMap();
    IdentityHashMap<Object, JSONWriter.Path> rightReferences = new IdentityHashMap();

    private Comparator comparator;

    PrintStream out = System.out;

    public Differ(Object left, Object right) {
        this.left = left;
        this.right = right;
    }

    public boolean isSkipTransient() {
        return skipTransient;
    }

    public void setSkipTransient(boolean skipTransient) {
        this.skipTransient = skipTransient;
    }

    public boolean isReferenceDetect() {
        return referenceDetect;
    }

    public void setReferenceDetect(boolean referenceDetect) {
        this.referenceDetect = referenceDetect;
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public static boolean diff(Object a, Object b) {
        return new Differ(a, b)
                .diff();
    }

    public String getLeftName() {
        return leftName;
    }

    public void setLeftName(String leftName) {
        this.leftName = leftName;
    }

    public String getRightName() {
        return rightName;
    }

    public void setRightName(String rightName) {
        this.rightName = rightName;
    }

    public Comparator getComparator() {
        return comparator;
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    public boolean diff() {
        return diff(
                left,
                right,
                new JSONWriter.Path(null, "$"),
                null
        );
    }

    boolean diff(Object left, Object right, JSONWriter.Path path, Type type) {
        boolean match = true;

        if (left == right) {
            return true;
        }

        if (left == null) {
            if (out != null) {
                out.println("diff type " + path + ", "
                        + leftName + " is null, "
                        + rightName + " class " + right.getClass());
            }
            return false;
        }

        if (right == null) {
            if (out != null) {
                out.println("diff type " + path
                        + ", " + rightName + " is null, "
                        + leftName + " class " + left.getClass());
            }
            return false;
        }

        Class leftClass = left.getClass();
        if (leftClass != right.getClass()) {
            if (out != null) {
                out.println("diff type " + path + ", " + leftName + " " + leftClass + ", " + rightName + " " + right.getClass());
            }
            return false;
        }

        if (ObjectWriterProvider.isPrimitiveOrEnum(leftClass)) {
            if (!left.equals(right)) {
                if (out != null) {
                    out.println("diff value " + path + ", " + leftName + " " + left + ", " + rightName + " " + right);
                }
                return false;
            }

            if (comparator != null) {
                int cmp = comparator.compare(left, right);
                if (cmp != 0) {
                    if (out != null) {
                        out.println("diff compare " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                    }
                    return false;
                }
            }
            return true;
        }

        if (referenceDetect) {
            JSONWriter.Path leftRefPath = leftReferences.put(left, path);
            JSONWriter.Path rightRefPath = rightReferences.put(right, path);

            // skip key reference detect
            boolean keyPathRef = path != null && path.toString().contains("\\#");
            if (keyPathRef) {
                return true;
            }

            boolean isLeftKeyRef = leftRefPath != null && leftRefPath.toString().contains("\\#");
            if (isLeftKeyRef) {
                return true;
            }

            boolean isRightKeyRef = rightRefPath != null && rightRefPath.toString().contains("\\#");
            if (isRightKeyRef) {
                return true;
            }

            if (leftRefPath != null && rightRefPath != null) {
                if (leftRefPath.equals(rightRefPath)) {
                    return true;
                }

                if (out != null) {
                    out.println("diff reference " + path + ", "
                            + leftName + " " + leftRefPath + ", "
                            + rightName + " " + rightRefPath + ", "
                            + leftName + " class " + left.getClass() + ", "
                            + rightName + " class " + right.getClass()
                    );
                }
                return false;
            }

            if (referenceDetect) {
                if (leftRefPath != null) {
                    if (out != null) {
                        out.println("diff reference " + path + ", "
                                + rightName + " is null, "
                                + leftName + " " + leftRefPath
                                + ", class " + left.getClass());
                    }
                    return false;
                }

                if (rightRefPath != null) {
                    if (out != null) {
                        out.println("diff reference " + path + ", "
                                + leftName + " is null, "
                                + rightName + " " + rightRefPath
                                + ", class " + right.getClass());
                    }
                    return false;
                }
            }
        }

        if (left instanceof HashSet) {
            return diffHashSet((Set) left, (Set) right, path, type, leftClass);
        }

        if (left instanceof Collection) {
            Collection leftCollection = (Collection) left;
            Collection rightCollection = (Collection) right;
            if (leftCollection.size() != rightCollection.size()) {
                if (out != null) {
                    out.println("diff collection size " + path + ", " + leftName + " " + leftCollection.size() + ", " + rightName + " " + rightCollection.size());
                }
                return false;
            }

            Iterator leftIt = leftCollection.iterator();
            Iterator rightIt = rightCollection.iterator();
            for (int i = 0; ; ++i) {
                if (!leftIt.hasNext()) {
                    break;
                }

                Object leftItem = leftIt.next();

                rightIt.hasNext();
                Object rightItem = rightIt.next();

                boolean result = diff(leftItem, rightItem, new JSONWriter.Path(path, i), null);
                if (!result) {
                    out.println("diff collection " + path + ", " + leftClass.getName());
                    return false;
                }
            }

            if (comparator != null) {
                int cmp = comparator.compare(left, right);
                if (cmp != 0) {
                    if (out != null) {
                        out.println("diff collection compare " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                    }
                    return false;
                }
            }
            return match;
        }

        if (left instanceof Object[]) {
            Object[] leftArray = (Object[]) left;
            Object[] rightArray = (Object[]) right;

            if (leftArray.length != rightArray.length) {
                if (out != null) {
                    out.println("diff array size " + path + ", " + leftName + " " + leftArray.length + ", " + rightName + " " + rightArray.length);
                }
                return false;
            }

            for (int i = 0; i < leftArray.length; i++) {
                Object leftItem = leftArray[i];
                Object rightItem = rightArray[i];
                boolean result = diff(leftItem, rightItem, new JSONWriter.Path(path, i), null);
                if (!result) {
                    if (out != null) {
                        out.println("diff array " + path + ", " + leftClass.getName());
                    }
                    return false;
                }
            }

            if (comparator != null) {
                int cmp = comparator.compare(left, right);
                if (cmp != 0) {
                    if (out != null) {
                        out.println("diff array compare " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                    }
                    return false;
                }
            }

            return match;
        }

        if (left instanceof SortedMap || left instanceof LinkedHashMap || left instanceof ConcurrentSkipListMap) {
            Map leftMap = (Map) left;
            Map rightMap = (Map) right;

            if (leftMap.size() != rightMap.size()) {
                if (out != null) {
                    out.println("diff map size " + path + ", " + leftName + " " + leftMap.size() + ", " + rightName + " " + rightMap.size() + ", " + leftMap.getClass().getName());
                }
                return false;
            }

            Iterator rightIt = rightMap.entrySet().iterator();
            for (Iterator leftIt = leftMap.entrySet().iterator(); leftIt.hasNext(); ) {
                Map.Entry leftEntry = (Map.Entry) leftIt.next();
                Map.Entry rightEntry = (Map.Entry) rightIt.next();
                Object key = leftEntry.getKey();

                Object leftValue = leftEntry.getValue();
                Object rightValue = rightEntry.getValue();
                boolean result = diff(leftValue, rightValue, new JSONWriter.Path(path, key.toString()), null);
                if (!result) {
                    if (out != null) {
                        out.println("diff sortedMap " + path + ", " + leftClass.getName());
                    }
                    return false;
                }
            }

            if (comparator != null) {
                int cmp = comparator.compare(left, right);
                if (cmp != 0) {
                    if (out != null) {
                        out.println("diff sortedMap compare map " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                    }
                    return false;
                }
            }

            return match;
        }

        if (left instanceof Map) {
            return diffMap((Map) left, (Map) right, path, match, leftClass);
        }

        for (Class clazz = leftClass; clazz != null; clazz = clazz.getSuperclass()) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);

                if (skipTransient && Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                Object leftFieldValue = null;
                try {
                    leftFieldValue = field.get(left);
                } catch (Throwable ex) {
                    if (out != null) {
                        out.println("get value error " + path + ", " + field.getName() + leftName + " " + left);
                    }
                    return false;
                }
                Object rightFieldValue = null;
                try {
                    rightFieldValue = field.get(right);
                } catch (Throwable ex) {
                    if (out != null) {
                        out.println("get value error " + path + ", " + field.getName() + rightName + " " + right);
                    }
                    return false;
                }

                if ("this$0".equals(field.getName())) {
                    boolean result = (leftFieldValue != null) == (rightFieldValue != null);
                    if (!result) {
                        if (out != null) {
                            out.println("diff this$0 " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                        }
                        return false;
                    }
                    continue;
                }

                boolean result = diff(leftFieldValue, rightFieldValue, new JSONWriter.Path(path, field.getName()), field.getGenericType());
                if (!result) {
                    if (out != null) {
                        out.println("diff object " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                    }
                    return false;
                }
            }
        }

        if (comparator != null) {
            int cmp = comparator.compare(left, right);
            if (cmp != 0) {
                if (out != null) {
                    out.println("diff compare " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                }
                match = false;
            }
        }

        return match;
    }

    boolean diffHashSet(Set left, Set right, JSONWriter.Path path, Type type, Class leftClass) {
        Class itemClass = null;
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                itemClass = (Class) actualTypeArguments[0];
            }
        }

        Set leftSet = left;
        Set rightSet = right;
        if (leftSet.size() != rightSet.size()) {
            if (out != null) {
                out.println("diff collection size " + path + ", " + leftName + " " + leftSet.size() + ", " + rightName + " " + rightSet.size());
            }
            return false;
        }

        Iterator leftIt = leftSet.iterator();
        for (int i = 0; ; ++i) {
            if (!leftIt.hasNext()) {
                break;
            }

            Object leftItem = leftIt.next();

            boolean result = rightSet.contains(leftItem);
            if (!result && !itemClass.isInstance(leftItem)) {
                Object castedItem = TypeUtils.cast(leftItem, itemClass);
                if (rightSet.contains(castedItem)) {
                    result = true;
                }
            }

            if (!result) {
                out.println("diff set " + path + ", " + leftClass.getName());
                return false;
            }
        }

        return true;
    }

    boolean diffMap(Map leftMap, Map rightMap, JSONWriter.Path path, boolean match, Class leftClass) {
        Map rightMapClone = new HashMap(rightMap);

        for (Iterator leftIt = leftMap.entrySet().iterator(); leftIt.hasNext(); ) {
            Map.Entry leftEntry = (Map.Entry) leftIt.next();
            Object leftKey = leftEntry.getKey();

            Object leftValue = leftEntry.getValue();
            Object rightValue = rightMap.get(leftKey);

            if (rightValue == null) {
                JSONWriter.Path keyPath = new JSONWriter.Path(new JSONWriter.Path(path, 0), "key");

                for (Iterator it = rightMapClone.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry rightEntry = (Map.Entry) it.next();
                    final boolean referenceDetect = this.referenceDetect;
                    final PrintStream out = this.out;

                    this.referenceDetect = false;
                    this.out = null;
                    try {
                        if (diff(leftKey, rightEntry.getKey(), keyPath, null)) {
                            it.remove();
                            rightValue = rightEntry.getValue();
                            break;
                        }
                    } finally {
                        this.referenceDetect = referenceDetect;
                        this.out = out;
                    }
                }
            }

            String leftKeyStr = leftKey != null && ObjectWriterProvider.isPrimitiveOrEnum(leftKey.getClass()) ? leftKey.toString() : "#";
            boolean result = diff(leftValue, rightValue, new JSONWriter.Path(path, leftKeyStr), null);
            if (!result) {
                out.println("diff map " + path + ", " + leftClass.getName());
                return false;
            }

            if (comparator != null) {
                int cmp = comparator.compare(leftValue, rightValue);
                if (cmp != 0) {
                    if (out != null) {
                        out.println("diff map value " + path + " " + leftKey + ", class " + leftValue.getClass().getResource(leftValue.getClass().getSimpleName() + ".class"));
                    }
                    return false;
                }
            }
        }

        Object leftInnerMap = null;
        Object rightInnerMap = null;
        if ("com.alibaba.fastjson.JSONObject".equals(leftClass.getName())) {
            Field mapField = BeanUtils.getDeclaredField(leftClass, "map");
            mapField.setAccessible(true);
            try {
                leftInnerMap = mapField.get(leftMap);
                rightInnerMap = mapField.get(rightMap);

                boolean result = diff(leftInnerMap, rightInnerMap, new JSONWriter.Path(path, "map"), null);
                if (!result) {
                    out.println("diff JSONObject1 " + path);
                    return false;
                }

                if (comparator != null) {
                    int cmp = comparator.compare(leftInnerMap, rightInnerMap);
                    if (cmp != 0) {
                        if (out != null) {
                            out.println("diff JSONObject1 map compare " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                        }
                        return false;
                    }
                }
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }
        }

        if (leftMap.size() != rightMap.size()) {
            if (out != null) {
                out.println("diff map size " + path + ", " + leftName + " " + leftMap.size() + ", " + rightName + " " + rightMap.size() + ", " + leftMap.getClass().getName());
            }
            return false;
        }

        if (comparator != null) {
            int cmp = comparator.compare(leftMap, rightMap);
            if (cmp != 0) {
                if (out != null) {
                    out.println("diff map compare " + path + ", class " + leftClass.getResource(leftClass.getSimpleName() + ".class"));
                }
                return false;
            }
        }

        return match;
    }
}
