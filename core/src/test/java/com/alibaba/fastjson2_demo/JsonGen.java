package com.alibaba.fastjson2_demo;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class JsonGen {
    private Random r = new Random();

    @Test
    public void gen() {
        System.out.println("{");
        final int COUNT = 20;
        for (int i = 0; i < COUNT; ++i) {
//            System.out.println("private int v" + i + ";");
            String field;
            if (i < 10) {
                field = "v000" + i;
            } else if (i < 100) {
                field = "v00" + i;
            } else if (i < 1000) {
                field = "v0" + i;
            } else {
                field = "v" + i;
            }
            char[] chars = new char[10];
            for (int j = chars.length - 1; j >= 0; j--) {
                int r = this.r.nextInt(26);
                chars[j] = (char) ('a' + r);
            }
            String value = new String(chars);
            System.out.print("\t\"" + field + "\" : \"" + value + "\"");
            if (i != COUNT - 1) {
                System.out.print(',');
            }
            System.out.println();
        }
        System.out.println("}");
    }
    @Test
    public void gen_int() {
        System.out.println("{");
        final int COUNT = 20;
        for (int i = 0; i < COUNT; ++i) {
//            System.out.println("private int v" + i + ";");
            String field;
            if (i < 10) {
                field = "v000" + i;
            } else if (i < 100) {
                field = "v00" + i;
            } else if (i < 1000) {
                field = "v0" + i;
            } else {
                field = "v" + i;
            }
            int intVal = r.nextInt(999999999);
            System.out.print("\t\"" + field + "\" : " + intVal + "");
            if (i != COUNT - 1) {
                System.out.print(',');
            }
            System.out.println();
        }
        System.out.println("}");
    }
    @Test
    public void gen_bool() {
        System.out.println("{");
        final int COUNT = 20;
        for (int i = 0; i < COUNT; ++i) {
//            System.out.println("private int v" + i + ";");
            String field;
            if (i < 10) {
                field = "v000" + i;
            } else if (i < 100) {
                field = "v00" + i;
            } else if (i < 1000) {
                field = "v0" + i;
            } else {
                field = "v" + i;
            }
            boolean val = (r.nextInt() % 2) == 0;
            System.out.print("\t\"" + field + "\" : " + val + "");
            if (i != COUNT - 1) {
                System.out.print(',');
            }
            System.out.println();
        }
        System.out.println("}");
    }

    @Test
    public void gen_long() {
        System.out.println("{");
        final int COUNT = 20;
        for (int i = 0; i < COUNT; ++i) {
//            System.out.println("private int v" + i + ";");
            String field;
            if (i < 10) {
                field = "v000" + i;
            } else if (i < 100) {
                field = "v00" + i;
            } else if (i < 1000) {
                field = "v0" + i;
            } else {
                field = "v" + i;
            }
            System.out.print("\t\"" + field + "\" : " + r.nextLong() + "");
            if (i != COUNT - 1) {
                System.out.print(',');
            }
            System.out.println();
        }
        System.out.println("}");
    }
}
