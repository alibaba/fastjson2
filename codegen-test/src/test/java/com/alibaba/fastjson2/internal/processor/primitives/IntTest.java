package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntTest {
    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.value = 1001;
        String str = JSON.toJSONString(bean);

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.value, bean1.value);
    }

    @JSONCompiled
    public static class Bean1 {
        public Integer value;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.v1 = 1001;
        bean.v2 = 1002;
        String str = JSON.toJSONString(bean);

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
    }

    @JSONCompiled
    public static class Bean2 {
        public Integer v1;
        public Integer v2;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        String str = JSON.toJSONString(bean);

        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
    }

    @JSONCompiled
    public static class Bean3 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        String str = JSON.toJSONString(bean);

        Bean4 bean1 = JSON.parseObject(str, Bean4.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
    }

    @JSONCompiled
    public static class Bean4 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        String str = JSON.toJSONString(bean);

        Bean5 bean1 = JSON.parseObject(str, Bean5.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
    }

    @JSONCompiled
    public static class Bean5 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
    }

    @Test
    public void test6() {
        Bean6 bean = new Bean6();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        String str = JSON.toJSONString(bean);

        Bean6 bean1 = JSON.parseObject(str, Bean6.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
    }

    @JSONCompiled
    public static class Bean6 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
    }

    @Test
    public void test7() {
        Bean7 bean = new Bean7();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        String str = JSON.toJSONString(bean);

        Bean7 bean1 = JSON.parseObject(str, Bean7.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
    }

    @JSONCompiled
    public static class Bean7 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
    }

    @Test
    public void test8() {
        Bean8 bean = new Bean8();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        String str = JSON.toJSONString(bean);

        Bean8 bean1 = JSON.parseObject(str, Bean8.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
    }

    @JSONCompiled
    public static class Bean8 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
    }

    @Test
    public void test9() {
        Bean9 bean = new Bean9();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        String str = JSON.toJSONString(bean);

        Bean9 bean1 = JSON.parseObject(str, Bean9.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
    }

    @JSONCompiled
    public static class Bean9 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
    }

    @Test
    public void test10() {
        Bean10 bean = new Bean10();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        String str = JSON.toJSONString(bean);

        Bean10 bean1 = JSON.parseObject(str, Bean10.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
    }

    @JSONCompiled
    public static class Bean10 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
    }

    @Test
    public void test11() {
        Bean11 bean = new Bean11();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        String str = JSON.toJSONString(bean);

        Bean11 bean1 = JSON.parseObject(str, Bean11.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
    }

    @JSONCompiled
    public static class Bean11 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
    }

    @Test
    public void test12() {
        Bean12 bean = new Bean12();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        String str = JSON.toJSONString(bean);

        Bean12 bean1 = JSON.parseObject(str, Bean12.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
    }

    @JSONCompiled
    public static class Bean12 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
    }

    @Test
    public void test13() {
        Bean13 bean = new Bean13();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        String str = JSON.toJSONString(bean);

        Bean13 bean1 = JSON.parseObject(str, Bean13.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
    }

    @JSONCompiled
    public static class Bean13 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
    }

    @Test
    public void test14() {
        Bean14 bean = new Bean14();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        String str = JSON.toJSONString(bean);

        Bean14 bean1 = JSON.parseObject(str, Bean14.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
    }

    @JSONCompiled
    public static class Bean14 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
    }

    @Test
    public void test15() {
        Bean15 bean = new Bean15();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        bean.v15 = 1015;
        String str = JSON.toJSONString(bean);

        Bean15 bean1 = JSON.parseObject(str, Bean15.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
    }

    @JSONCompiled
    public static class Bean15 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
        public Integer v15;
    }

    @Test
    public void test16() {
        Bean16 bean = new Bean16();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        bean.v15 = 1015;
        bean.v16 = 1016;
        String str = JSON.toJSONString(bean);

        Bean16 bean1 = JSON.parseObject(str, Bean16.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
        assertEquals(bean.v16, bean1.v16);
    }

    @JSONCompiled
    public static class Bean16 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
        public Integer v15;
        public Integer v16;
    }

    @Test
    public void test17() {
        Bean17 bean = new Bean17();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        bean.v15 = 1015;
        bean.v16 = 1016;
        bean.v17 = 1017;
        String str = JSON.toJSONString(bean);

        Bean17 bean1 = JSON.parseObject(str, Bean17.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
        assertEquals(bean.v16, bean1.v16);
        assertEquals(bean.v17, bean1.v17);
    }

    @JSONCompiled
    public static class Bean17 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
        public Integer v15;
        public Integer v16;
        public Integer v17;
    }

    @Test
    public void test18() {
        Bean18 bean = new Bean18();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        bean.v15 = 1015;
        bean.v16 = 1016;
        bean.v17 = 1017;
        bean.v18 = 1018;
        String str = JSON.toJSONString(bean);

        Bean18 bean1 = JSON.parseObject(str, Bean18.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
        assertEquals(bean.v16, bean1.v16);
        assertEquals(bean.v17, bean1.v17);
        assertEquals(bean.v18, bean1.v18);
    }

    @JSONCompiled
    public static class Bean18 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
        public Integer v15;
        public Integer v16;
        public Integer v17;
        public Integer v18;
    }

    @Test
    public void test19() {
        Bean19 bean = new Bean19();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        bean.v15 = 1015;
        bean.v16 = 1016;
        bean.v17 = 1017;
        bean.v18 = 1018;
        bean.v19 = 1019;
        String str = JSON.toJSONString(bean);

        Bean19 bean1 = JSON.parseObject(str, Bean19.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
        assertEquals(bean.v16, bean1.v16);
        assertEquals(bean.v17, bean1.v17);
        assertEquals(bean.v18, bean1.v18);
        assertEquals(bean.v19, bean1.v19);
    }

    @JSONCompiled
    public static class Bean19 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
        public Integer v15;
        public Integer v16;
        public Integer v17;
        public Integer v18;
        public Integer v19;
    }

    @Test
    public void test20() {
        Bean20 bean = new Bean20();
        bean.v1 = 1001;
        bean.v2 = 1002;
        bean.v3 = 1003;
        bean.v4 = 1004;
        bean.v5 = 1005;
        bean.v6 = 1006;
        bean.v7 = 1007;
        bean.v8 = 1008;
        bean.v9 = 1009;
        bean.v10 = 1010;
        bean.v11 = 1011;
        bean.v12 = 1012;
        bean.v13 = 1013;
        bean.v14 = 1014;
        bean.v15 = 1015;
        bean.v16 = 1016;
        bean.v17 = 1017;
        bean.v18 = 1018;
        bean.v19 = 1019;
        bean.v20 = 1020;
        String str = JSON.toJSONString(bean);

        Bean20 bean1 = JSON.parseObject(str, Bean20.class);
        assertEquals(bean.v1, bean1.v1);
        assertEquals(bean.v2, bean1.v2);
        assertEquals(bean.v3, bean1.v3);
        assertEquals(bean.v4, bean1.v4);
        assertEquals(bean.v5, bean1.v5);
        assertEquals(bean.v6, bean1.v6);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v7, bean1.v7);
        assertEquals(bean.v8, bean1.v8);
        assertEquals(bean.v9, bean1.v9);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
        assertEquals(bean.v16, bean1.v16);
        assertEquals(bean.v17, bean1.v17);
        assertEquals(bean.v18, bean1.v18);
        assertEquals(bean.v19, bean1.v19);
        assertEquals(bean.v20, bean1.v20);
    }

    @JSONCompiled
    public static class Bean20 {
        public Integer v1;
        public Integer v2;
        public Integer v3;
        public Integer v4;
        public Integer v5;
        public Integer v6;
        public Integer v7;
        public Integer v8;
        public Integer v9;
        public Integer v10;
        public Integer v11;
        public Integer v12;
        public Integer v13;
        public Integer v14;
        public Integer v15;
        public Integer v16;
        public Integer v17;
        public Integer v18;
        public Integer v19;
        public Integer v20;
    }
}
