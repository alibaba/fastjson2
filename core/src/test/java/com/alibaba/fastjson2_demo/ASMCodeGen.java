package com.alibaba.fastjson2_demo;

import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2_vo.Int1000;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

public class ASMCodeGen {
    @Test
    public void gen() {
        List<Method> methods = new ArrayList<>();
        BeanUtils.setters(Int1000.class, e -> methods.add(e));

        List<String> propertyNames = new ArrayList<>();
        methods.forEach(
                e -> propertyNames.add(
                        BeanUtils.setterName(e.getName(), null)));

        long[] hashCodes = new long[propertyNames.size()];
        for (int i = 0; i < propertyNames.size(); i++) {
            hashCodes[i] = Fnv.hashCode64(propertyNames.get(i));
        }
        Arrays.sort(hashCodes);

        final short[] mapping = new short[hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(hashCodes, hashCode);
            mapping[index] = (short) i;
        }
        Map<Integer, List<Long>> map = new HashMap<>();
        BeanUtils.setters(Int1000.class, e -> {
            String propertyName = BeanUtils.setterName(e.getName(), null);
            long hashCode64 = Fnv.hashCode64(propertyName);
            int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
            List<Long> relatedHashCodes = map.get(hashCode32);
            if (relatedHashCodes == null) {
                map.put(hashCode32, relatedHashCodes = new ArrayList<>());
            }
            relatedHashCodes.add(hashCode64);
        });

        System.out.println("int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));");
        System.out.println("int indx;");
        System.out.println("switch(hashCode32) {");
        map.forEach(
                (hashCode32, relatedHashCodes) -> {
                    System.out.println("\tcase " + hashCode32 + ":");
                    relatedHashCodes.forEach(
                            e -> {
                                System.out.println("\t\tif (hashCode64 == " + e + "L) {");
                                int m = Arrays.binarySearch(hashCodes, e);
                                int indx = mapping[m];
                                System.out.println("\t\t\tindx = " + indx + ";");
                                System.out.println("\t\t\tbreak;");
                                System.out.println("\t\t}");
                            });
                });
        System.out.println("\tdefault:");
        System.out.println("\t\treturn null;");
        System.out.println("}");
    }
}
