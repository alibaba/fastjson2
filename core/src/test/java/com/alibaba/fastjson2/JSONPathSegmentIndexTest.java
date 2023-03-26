package com.alibaba.fastjson2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static com.alibaba.fastjson2.JSONReader.EOI;
import static org.junit.jupiter.api.Assertions.*;

public class JSONPathSegmentIndexTest {
    enum ContextRootType {
        t_null,
        t_list,
        t_sortedSet,
        t_linkedHashSet,
        t_collectionSize1,
        t_collection,
        t_objectArray,
        t_array,
        t_arrayInt,
        t_arrayLong,
        t_jsonpathSequence,
        t_hashmap,
        t_hashmapSize1,
        t_linkedHashMap,
        t_sortedMap,
        t_notSupport,
    }

    private static final Map<ContextRootType, HashMap<Integer, Integer>> contextValueMap = new HashMap<>();
    private final Integer defaultObject = 100;

    static {
        for (ContextRootType type : ContextRootType.values()) {
            contextValueMap.put(type, new HashMap<>());
            //last index
            contextValueMap.get(type).put(-1, 5);
            //index out of bounds
            contextValueMap.get(type).put(-6, null);
            //first index
            contextValueMap.get(type).put(0, 1);
            //second index
            contextValueMap.get(type).put(1, 2);
            //index out of bounds
            contextValueMap.get(type).put(6, null);
        }

        ContextRootType nullType = ContextRootType.t_null;
        contextValueMap.put(nullType, new HashMap<>());
        contextValueMap.get(nullType).put(-1, null);
        contextValueMap.get(nullType).put(-6, null);
        contextValueMap.get(nullType).put(0, null);
        contextValueMap.get(nullType).put(1, null);
        contextValueMap.get(nullType).put(6, null);

        ContextRootType notSupportType = ContextRootType.t_notSupport;
        contextValueMap.put(notSupportType, new HashMap<>());
        contextValueMap.get(notSupportType).put(-1, null);
        contextValueMap.get(notSupportType).put(-6, null);
        contextValueMap.get(notSupportType).put(0, 100);
        contextValueMap.get(notSupportType).put(1, null);
        contextValueMap.get(notSupportType).put(6, null);

        ContextRootType collectionType = ContextRootType.t_collection;
        contextValueMap.put(collectionType, new HashMap<>());
        contextValueMap.get(collectionType).put(-1, null);
        contextValueMap.get(collectionType).put(-6, null);
        contextValueMap.get(collectionType).put(0, 1);
        contextValueMap.get(collectionType).put(1, null);
        contextValueMap.get(collectionType).put(6, null);

        ContextRootType sortedSetType = ContextRootType.t_sortedSet;
        contextValueMap.put(sortedSetType, new HashMap<>());
        contextValueMap.get(sortedSetType).put(-1, null);
        contextValueMap.get(sortedSetType).put(-6, null);
        contextValueMap.get(sortedSetType).put(0, 1);
        contextValueMap.get(sortedSetType).put(1, 2);
        contextValueMap.get(sortedSetType).put(6, null);

        ContextRootType linkedHashSetType = ContextRootType.t_linkedHashSet;
        contextValueMap.put(linkedHashSetType, new HashMap<>());
        contextValueMap.get(linkedHashSetType).put(-1, null);
        contextValueMap.get(linkedHashSetType).put(-6, null);
        contextValueMap.get(linkedHashSetType).put(0, 1);
        contextValueMap.get(linkedHashSetType).put(1, 2);
        contextValueMap.get(linkedHashSetType).put(6, null);

        ContextRootType hashmapSize1Type = ContextRootType.t_hashmapSize1;
        contextValueMap.put(hashmapSize1Type, new HashMap<>());
        contextValueMap.get(hashmapSize1Type).put(-1, null);
        contextValueMap.get(hashmapSize1Type).put(-6, null);
        contextValueMap.get(hashmapSize1Type).put(0, 1);
        contextValueMap.get(hashmapSize1Type).put(1, null);
        contextValueMap.get(hashmapSize1Type).put(6, null);

        ContextRootType linkedHashMapType = ContextRootType.t_linkedHashMap;
        contextValueMap.put(linkedHashMapType, new HashMap<>());
        contextValueMap.get(linkedHashMapType).put(-1, null);
        contextValueMap.get(linkedHashMapType).put(-6, null);
        contextValueMap.get(linkedHashMapType).put(0, 1);
        contextValueMap.get(linkedHashMapType).put(1, 2);
        contextValueMap.get(linkedHashMapType).put(6, null);

        ContextRootType sortedMapType = ContextRootType.t_sortedMap;
        contextValueMap.put(sortedMapType, new HashMap<>());
        contextValueMap.get(sortedMapType).put(-1, null);
        contextValueMap.get(sortedMapType).put(-6, null);
        contextValueMap.get(sortedMapType).put(0, 1);
        contextValueMap.get(sortedMapType).put(1, 2);
        contextValueMap.get(sortedMapType).put(6, null);
    }

    @Test
    public void test_constructor() {
        assertEquals(0, new JSONPathSegmentIndex(0).index);
        assertEquals(1, new JSONPathSegmentIndex(1).index);
        assertEquals(2, new JSONPathSegmentIndex(2).index);
        assertEquals(-1, new JSONPathSegmentIndex(-1).index);
        assertEquals(5, new JSONPathSegmentIndex(5).index);
    }

    @Test
    public void test_of() {
        assertEquals(JSONPathSegmentIndex.ZERO, JSONPathSegmentIndex.of(0));
        assertEquals(JSONPathSegmentIndex.ONE, JSONPathSegmentIndex.of(1));
        assertEquals(JSONPathSegmentIndex.TWO, JSONPathSegmentIndex.of(2));
        assertEquals(JSONPathSegmentIndex.LAST, JSONPathSegmentIndex.of(-1));
        assertEquals(5, JSONPathSegmentIndex.of(5).index);
    }

    @Test
    public void test_eval() {
        for (ContextRootType type : ContextRootType.values()) {
            HashMap<Integer, Integer> values = contextValueMap.get(type);
            values.forEach((index, val) -> {
                JSONPath.Context context = getNewContext(type, index);
                //System.out.printf("%s : %d\n", type, index);//debug
                if (type == ContextRootType.t_notSupport || type == ContextRootType.t_collectionSize1 || type == ContextRootType.t_collection) {
                    if (index == 0) {
                        context.current.eval(context);
                        if (type == ContextRootType.t_collectionSize1) {
                            assertEvalAndValue(context, val);
                        } else if (type == ContextRootType.t_collection) {
                            assertEvalAndValueNeq(context, val);
                        } else {
                            //lax mode
                            assertEvalAndValue(context, defaultObject);
                        }
                        return;
                    }
                    assertThrows(JSONException.class, () -> {
                        context.current.eval(context);
                    });
                } else if (type == ContextRootType.t_jsonpathSequence) {
                    //not all covered
                    assertFalse(context.eval);
                } else if (type == ContextRootType.t_hashmap) {
                    //not all covered
                    context.current.eval(context);
                    assertTrue(context.eval);
                    assertNull(context.value);
                } else if (type == ContextRootType.t_hashmapSize1) {
                    //not all covered
                    if (index == 0) {
                        context.current.eval(context);
                        assertEquals(context.value, 1);
                        return;
                    }
                    context.current.eval(context);
                    assertTrue(context.eval);
                } else if (type == ContextRootType.t_sortedMap) {
                    //fix: ignore
                    assertFalse(context.eval);
                } else {
                    //System.out.printf("%s :  %d : %d : %d : %b\n", type.toString(), index, context.value, val, context.eval);//debug
                    context.current.eval(context);
                    assertEvalAndValue(context, val);
                }
            });
        }
    }

    @Test
    public void test_set() {
        //list
        setTest(ContextRootType.t_list, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }

            List<Integer> list = (List<Integer>) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(list.get(realIndex), paramArray[1]);
                return null;
            }

            if (realIndex < 0) {
                assertEquals(list, getNewArrayList());
            } else {
                ArrayList<Integer> compare = getNewArrayList();
                compare.addAll(Lists.newArrayList(null, 100));
                assertEquals(list, compare);
            }

            return null;
        });

        //object array
        setTest(ContextRootType.t_objectArray, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }

            Integer[] arr = (Integer[]) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], paramArray[1]);
                return null;
            }
            assertTrue(Arrays.equals(arr, getNewArrayList().toArray(new Integer[0])));

            return null;
        });

        //array
        setTest(ContextRootType.t_array, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0) {
                realIndex = paramArray[0];
            } else {
                realIndex = 5 + paramArray[0];
            }
            int[] arr = (int[]) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], paramArray[1]);
                return null;
            }
            assertTrue(Arrays.equals(arr, new int[]{1, 2, 3, 4, 5}));

            return null;
        });

        //not support
        setTest(ContextRootType.t_notSupport, null);
    }

    @Test
    public void test_setCallback() {
        //list
        setCallbackTest(ContextRootType.t_list, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            List<Integer> list = (List<Integer>) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(list.get(realIndex), paramArray[1] + 10);
                return null;
            }
            assertEquals(list, getNewArrayList());

            return null;
        });

        //object array
        setCallbackTest(ContextRootType.t_objectArray, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            Integer[] arr = (Integer[]) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], paramArray[1] + 10);
                return null;
            }
            assertTrue(Arrays.equals(arr, getNewArrayList().toArray(new Integer[0])));

            return null;
        });

        //array
        setCallbackTest(ContextRootType.t_array, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            int[] arr = (int[]) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], paramArray[1] + 10);
                return null;
            }
            assertTrue(Arrays.equals(arr, new int[]{1, 2, 3, 4, 5}));

            return null;
        });

        //not support
        setCallbackTest(ContextRootType.t_notSupport, null);
    }

    @Test
    public void test_remove() {
        //list
        removeTest(ContextRootType.t_list, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(paramArray[1], 1);
                return null;
            }
            assertEquals(paramArray[1], 0);

            return null;
        });

        //not support
        setCallbackTest(ContextRootType.t_notSupport, null);
    }

    @Test
    public void test_setInt() {
        //int array
        setIntTest(ContextRootType.t_arrayInt, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            int[] arr = (int[]) root;
            //System.out.printf("%d : %d : %d\n", realIndex, paramArray[0], paramArray[1]);//debug
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], paramArray[1]);
                return null;
            }
            assertTrue(Arrays.equals(arr, new int[]{1, 2, 3, 4, 5}));

            return null;
        });

        //long array
        setIntTest(ContextRootType.t_arrayLong, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            long[] arr = (long[]) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], (long) paramArray[1]);
                return null;
            }
            assertTrue(Arrays.equals(arr, new long[]{1, 2, 3, 4, 5}));

            return null;
        });

        //other
        setIntTest(ContextRootType.t_null, null);
        setIntTest(ContextRootType.t_list, null);
        setIntTest(ContextRootType.t_collection, null);
        setIntTest(ContextRootType.t_objectArray, null);
        setIntTest(ContextRootType.t_jsonpathSequence, null);
        setIntTest(ContextRootType.t_linkedHashSet, null);
        setIntTest(ContextRootType.t_sortedSet, null);
        setIntTest(ContextRootType.t_notSupport, null);
        setIntTest(ContextRootType.t_collectionSize1, null);
        setIntTest(ContextRootType.t_array, null);
    }

    @Test
    public void test_setLong() {
        //int array
        setLongTest(ContextRootType.t_arrayInt, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            int[] arr = (int[]) root;
            //System.out.printf("%d : %d : %d\n", realIndex, paramArray[0], paramArray[1]);//debug
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], paramArray[1]);
                return null;
            }
            assertTrue(Arrays.equals(arr, new int[]{1, 2, 3, 4, 5}));

            return null;
        });

        //long array
        setLongTest(ContextRootType.t_arrayLong, (root, param) -> {
            int[] paramArray = (int[]) param;
            int realIndex;

            if (paramArray[0] >= 0 && paramArray[0] < 5) {
                realIndex = paramArray[0];
            } else if (paramArray[0] < 0) {
                realIndex = 5 + paramArray[0];
            } else {
                realIndex = paramArray[0];
            }
            long[] arr = (long[]) root;
            if (realIndex >= 0 && realIndex < 5) {
                assertEquals(arr[realIndex], (long) paramArray[1]);
                return null;
            }
            assertTrue(Arrays.equals(arr, new long[]{1, 2, 3, 4, 5}));

            return null;
        });

        //other
        setLongTest(ContextRootType.t_null, null);
        setLongTest(ContextRootType.t_list, null);
        setLongTest(ContextRootType.t_collection, null);
        setLongTest(ContextRootType.t_objectArray, null);
        setLongTest(ContextRootType.t_jsonpathSequence, null);
        setLongTest(ContextRootType.t_linkedHashSet, null);
        setLongTest(ContextRootType.t_sortedSet, null);
        setLongTest(ContextRootType.t_notSupport, null);
        setLongTest(ContextRootType.t_collectionSize1, null);
        setLongTest(ContextRootType.t_array, null);
    }

    @Test
    public void test_accept() {
        //not all covered
        acceptTest(0);
        acceptTest(-1);
        acceptTest(1);
    }

    @Test
    public void test_toString() {
        assertEquals(JSONPathSegmentIndex.ZERO.toString(), "[0]");
        assertEquals(JSONPathSegmentIndex.ONE.toString(), "[1]");
        assertEquals(JSONPathSegmentIndex.TWO.toString(), "[2]");
        assertEquals(JSONPathSegmentIndex.LAST.toString(), "[-1]");
        assertEquals(new JSONPathSegmentIndex(6).toString(), "[6]");
        assertEquals(new JSONPathSegmentIndex(-6).toString(), "[-6]");
    }

    private void acceptTest(Integer index) {
        ContextRootType nullType = ContextRootType.t_null;
        for (String str : getStringContent()) {
            //System.out.println(str);//debug
            JSONPath.Context context = getNewContext(nullType, index);
            assertDoesNotThrow(() -> {
                context.current.accept(JSONReader.of(str), context);
            });
        }

        JSONPath.Context context1 = getNewContext(nullType, index);
        assertThrows(JSONException.class, () -> {
            context1.current.accept(JSONReader.of("aaaaaa"), context1);
        });
        JSONPath.Context context2 = getNewContext(nullType, index);
        assertThrows(JSONException.class, () -> {
            context2.current.accept(JSONReader.of("%%%"), context2);
        });
    }

    private void setLongTest(ContextRootType type, BiFunction callback) {
        HashMap<Integer, Integer> values = contextValueMap.get(type);
        values.forEach((index, val) -> {
            JSONPath.Context context = getNewContext(type, index);
            //print(((List) context.root).iterator());//debug
            if (callback == null) {
                //other
                //not covered:call set(context,value) method
                return;
            }
            //print(((List) context.root).iterator());//debug
            context.current.setLong(context, 100L);
            callback.apply(context.root, new int[]{index, 100});
        });
    }

    private void setIntTest(ContextRootType type, BiFunction callback) {
        HashMap<Integer, Integer> values = contextValueMap.get(type);
        values.forEach((index, val) -> {
            JSONPath.Context context = getNewContext(type, index);
            //print(((List) context.root).iterator());//debug
            if (callback == null) {
                //other
                //not covered:call set(context,value) method
                return;
            }
            //print(((List) context.root).iterator());//debug
            context.current.setInt(context, 100);
            callback.apply(context.root, new int[]{index, 100});
        });
    }

    private void removeTest(ContextRootType type, BiFunction callback) {
        HashMap<Integer, Integer> values = contextValueMap.get(type);
        values.forEach((index, val) -> {
            JSONPath.Context context = getNewContext(type, index);
            //print(((List) context.root).iterator());//debug
            if (callback == null) {
                //not support
                assertThrows(JSONException.class, () -> {
                    context.current.set(context, 100);
                });
                return;
            }
            //print(((List) context.root).iterator());//debug
            callback.apply(context.root, new int[]{index, context.current.remove(context) ? 1 : 0});
        });
    }

    private void setCallbackTest(ContextRootType type, BiFunction callback) {
        HashMap<Integer, Integer> values = contextValueMap.get(type);
        values.forEach((index, val) -> {
            JSONPath.Context context = getNewContext(type, index);
            //print(((List) context.root).iterator());//debug
            if (callback == null) {
                //not support
                assertThrows(JSONException.class, () -> {
                    context.current.setCallback(context, (root, value) -> {
                        return (int) value + 10;
                    });
                });
                return;
            }
            AtomicInteger oldValue = new AtomicInteger();
            context.current.setCallback(context, (root, value) -> {
                oldValue.set((int) value);
                return (int) value + 10;
            });
            //print(((List) context.root).iterator());//debug
            callback.apply(context.root, new int[]{index, oldValue.get()});
        });
    }

    private void setTest(ContextRootType type, BiFunction callback) {
        HashMap<Integer, Integer> values = contextValueMap.get(type);
        values.forEach((index, val) -> {
            JSONPath.Context context = getNewContext(type, index);
            //print(((List) context.root).iterator());//debug
            if (callback == null) {
                //not support
                assertThrows(JSONException.class, () -> {
                    context.current.set(context, 100);
                });
                return;
            }
            context.current.set(context, 100);
            //print(((List) context.root).iterator());//debug
            callback.apply(context.root, new int[]{index, 100});
        });
    }

    private void assertEvalAndValue(JSONPath.Context context, Integer value) {
        //System.out.printf("%s : %s : %s\n", String.valueOf(context.eval), String.valueOf(context.value), String.valueOf(value));//debug
        assertTrue(context.eval);
        assertEquals(context.value != null ? Integer.parseInt("" + context.value) : null, value);
    }

    private void assertEvalAndValueNeq(JSONPath.Context context, Integer value) {
        //System.out.printf("%s : %s : %s\n", String.valueOf(context.eval), String.valueOf(context.value), String.valueOf(value));//debug
        assertTrue(context.eval);
        assertNotEquals(context.value, value);
    }

    private ArrayList<Integer> getNewArrayList() {
        return Lists.newArrayList(1, 2, 3, 4, 5);
    }

    private JSONPath.Context getNewContext(ContextRootType typeEnum, Integer index) {
        Type type;
        Object root;

        switch (typeEnum) {
            case t_null:
                type = Object.class;
                root = null;
                break;
            case t_list:
                type = List.class;
                root = getNewArrayList();
                break;
            case t_sortedSet:
                type = SortedSet.class;
                root = Sets.newTreeSet(getNewArrayList());
                break;
            case t_linkedHashSet:
                type = LinkedHashSet.class;
                root = Sets.newLinkedHashSet(getNewArrayList());
                break;
            case t_collection:
                type = HashSet.class;
                root = Sets.newHashSet(getNewArrayList());
                break;
            case t_collectionSize1:
                type = HashSet.class;
                root = Sets.newHashSet(1);
                break;
            case t_arrayInt:
            case t_array:
                type = int.class;
                root = new int[]{1, 2, 3, 4, 5};
                break;
            case t_arrayLong:
                type = long.class;
                root = new long[]{1, 2, 3, 4, 5};
                break;
            case t_objectArray:
                type = Integer.TYPE;
                root = new Integer[]{1, 2, 3, 4, 5};
                break;
            case t_jsonpathSequence:
                type = JSONPath.Sequence.class;
                root = new JSONPath.Sequence(getNewArrayList());
                break;
            case t_hashmap:
                type = HashMap.class;
                HashMap<String, Integer> map1 = Maps.newHashMap();
                map1.put("a", 1);
                map1.put("b", 2);
                map1.put("c", 3);
                map1.put("d", 4);
                map1.put("e", 5);
                root = map1;
                break;
            case t_hashmapSize1:
                type = HashMap.class;
                HashMap<String, Integer> map2 = Maps.newHashMap();
                map2.put("a", 1);
                root = map2;
                break;
            case t_linkedHashMap:
                type = LinkedHashMap.class;
                LinkedHashMap<String, Integer> map3 = Maps.newLinkedHashMap();
                map3.put("a", 1);
                map3.put("b", 2);
                map3.put("c", 3);
                map3.put("d", 4);
                map3.put("e", 5);
                root = map3;
                break;
            case t_sortedMap:
                type = SortedMap.class;
                SortedMap<String, Integer> map4 = Maps.newTreeMap();
                map4.put("a", 1);
                map4.put("b", 2);
                map4.put("c", 3);
                map4.put("d", 4);
                map4.put("e", 5);
                root = map4;
                break;
            case t_notSupport:
                //same with default
            default:
                type = Integer.class;
                root = defaultObject;
                break;
        }

        JSONPathSegmentIndex segment = index == null ? JSONPathSegmentIndex.ONE : new JSONPathSegmentIndex(index);
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$", type), null, segment, null, 0);
        context.root = root;
        return context;
    }

    private void print(Iterator iterator) {
        while (iterator.hasNext()) {
            System.out.print(iterator.next());
        }
        System.out.println();
    }

    private String[] getStringContent() {
        return new String[]{
                "{'age':12}",
                "{'age':12}",
                "{'name':'xiaoming','age':33}",
                "{'a':true}",
                "{'a':false}",
                "{'a':null}",
                "" + EOI,
        };
    }
}
