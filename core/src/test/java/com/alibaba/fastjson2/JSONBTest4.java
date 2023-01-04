package com.alibaba.fastjson2;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBTest4 {
    String str1;
    String str2;
    String str3;

    public JSONBTest4() throws Exception {
        {
            InputStream is = JSONBTest4.class.getClassLoader().getResourceAsStream("data/path_01.json");
            str1 = IOUtils.toString(is, "UTF-8");
            is.close();
        }
        {
            InputStream is = JSONBTest4.class.getClassLoader().getResourceAsStream("data/path_02.json");
            str2 = IOUtils.toString(is, "UTF-8");
            is.close();
        }
        {
            InputStream is = JSONBTest4.class.getClassLoader().getResourceAsStream("data/path_03.json");
            str3 = IOUtils.toString(is, "UTF-8");
            is.close();
        }
    }

    @Test
    public void test_1() {
        JSONObject object = JSON.parseObject(str1);
        JSONObject object2 = JSONB
                .parseObject(
                        JSONB.fromJSONString(str1));

        assertEquals(object, object2);

        JSONObject object3 = JSONB
                .parseObject(
                        JSONB.fromJSONBytes(
                                str1.getBytes(StandardCharsets.UTF_8)
                        )
                );

        assertEquals(object, object3);
    }

    @Test
    public void test_2() {
        JSONObject object = JSON.parseObject(str2);
        JSONObject object2 = JSONB
                .parseObject(
                        JSONB.fromJSONString(str2));

        assertEquals(object, object2);

        JSONObject object3 = JSONB
                .parseObject(
                        JSONB.fromJSONBytes(
                                str2.getBytes(StandardCharsets.UTF_8)
                        )
                );

        assertEquals(object, object3);
    }

    @Test
    public void test_2_path_0() {
        JSONPath path = JSONPath.of("$.store.book[*].author");
        String expected = "[\"Nigel Rees\",\"Evelyn Waugh\",\"Herman Melville\",\"J. R. R. Tolkien\"]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(expected,
                path
                        .eval(
                                JSON.parseObject(str2))
                        .toString());
    }

    @Test
    public void test_2_path_1() {
        JSONPath path = JSONPath.of("\t$..author");
        String expected = "[\"Nigel Rees\",\"Evelyn Waugh\",\"Herman Melville\",\"J. R. R. Tolkien\"]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(expected,
                path
                        .eval(
                                JSON.parseObject(str2))
                        .toString());
    }

    @Test
    public void test_2_path_2() {
        JSONPath path = JSONPath.of("$.store.*");
        String expected = "[[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99},{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}],{\"color\":\"red\",\"price\":19.95}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_3() {
        JSONPath path = JSONPath.of("$.store..price");
        String expected = "[8.95,12.99,8.99,22.99,19.95]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_4() {
        JSONPath path = JSONPath.of("$..book[2]");
        String expected = "{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99}";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_5() {
        JSONPath path = JSONPath.of("$..book[-1:]");
        String expected = "[{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_6() {
        JSONPath path = JSONPath.of("$..book[0,1]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_7() {
        JSONPath path = JSONPath.of("$..book[:2]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_8() {
        JSONPath path = JSONPath.of("$..book[?(@.isbn)]");
        String expected = "[{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_9() {
        JSONPath path = JSONPath.of("$..book[?(@.price<10)]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_10() {
        JSONPath path = JSONPath.of("$..book[?(@.category like 'referenc%')]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_11() {
        JSONPath path = JSONPath.of("$..book[?(@.author like 'Nigel%')]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_12() {
        JSONPath path = JSONPath.of("$..book[?(@.author like '%Rees')]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_13() {
        JSONPath path = JSONPath.of("$..book[?(@.author like 'Nigel%Rees')]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_14() {
        JSONPath path = JSONPath.of("$..book[?(@.author like 'Herman Melville')]");
        String expected = "[{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_15() {
        JSONPath path = JSONPath.of("$..book[?(@.author not like 'Nigel%')]");
        String expected = "[{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99},{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_16_in() {
        JSONPath path = JSONPath.of("$..book[?(@.author in ('J. R. R. Tolkien'))]");
        String expected = "[{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_17_not_in() {
        JSONPath path = JSONPath.of("$..book[?(@.author not in ('J. R. R. Tolkien', 'Herman Melville'))]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_2_path_18_nin() {
        JSONPath path = JSONPath.of("$..book[?(@.author nin ('J. R. R. Tolkien', 'Herman Melville'))]");
        String expected = "[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str2)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str2.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(
                expected,
                path.eval(
                        JSON.parseObject(str2)
                ).toString()
        );
    }

    @Test
    public void test_3() {
        JSONObject object = JSON.parseObject(str3);
        JSONObject object2 = JSONB
                .parseObject(
                        JSONB.fromJSONString(str3));

        assertEquals(object, object2);

        JSONObject object3 = JSONB
                .parseObject(
                        JSONB.fromJSONBytes(
                                str3.getBytes(StandardCharsets.UTF_8)
                        )
                );

        assertEquals(object, object3);
    }

    @Test
    public void test_3_path() {
        assertEquals("[\"iPhone\"]",
                JSONPath
                        .of("$.phoneNumbers[:1].type")
                        .extract(JSONReader
                                .of(str3)
                        ).toString()
        );

        assertEquals("[\"iPhone\"]",
                JSONPath
                        .of("$.phoneNumbers[:1].type")
                        .extract(
                                JSONReader
                                        .ofJSONB(
                                                JSONB.fromJSONBytes(
                                                        str3.getBytes(StandardCharsets.UTF_8)
                                                )
                                        )
                        ).toString()
        );
    }

    @Test
    public void test_3_path_1() {
        JSONPath path = JSONPath
                .of("$.phoneNumbers[:-1].type");
        assertEquals("[\"iPhone\"]",
                path
                        .extract(JSONReader
                                .of(str3)
                        ).toString()
        );

        assertEquals("[\"iPhone\"]",
                path.extract(
                        JSONReader
                                .ofJSONB(
                                        JSONB.fromJSONBytes(
                                                str3.getBytes(StandardCharsets.UTF_8)
                                        )
                                )
                ).toString()
        );

        assertEquals("[\"iPhone\"]",
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }

    @Test
    public void test_3_path_2() {
        JSONPath path = JSONPath
                .of("$.phoneNumbers[:0].type");
        assertEquals("[\"iPhone\",\"home\"]",
                path
                        .extract(JSONReader
                                .of(str3)
                        ).toString()
        );

        assertEquals("[\"iPhone\",\"home\"]",
                path
                        .extract(
                                JSONReader
                                        .ofJSONB(
                                                JSONB.fromJSONBytes(
                                                        str3.getBytes(StandardCharsets.UTF_8)
                                                )
                                        )
                        ).toString()
        );

        assertEquals("[\"iPhone\",\"home\"]",
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }

    @Test
    public void test_3_path_3() {
        JSONPath path = JSONPath
                .of("$.phoneNumbers[:].type");
        assertEquals("[\"iPhone\",\"home\"]",
                path.extract(
                        JSONReader.of(str3)
                ).toString()
        );

        assertEquals("[\"iPhone\",\"home\"]",
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str3.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals("[\"iPhone\",\"home\"]",
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }

    @Test
    public void test_3_path_4() {
        assertEquals("[\"iPhone\",\"home\"]",
                JSONPath
                        .of("$.phoneNumbers[*].type")
                        .extract(JSONReader
                                .of(str3)
                        ).toString()
        );

        assertEquals("[\"iPhone\",\"home\"]",
                JSONPath
                        .of("$.phoneNumbers[*].type")
                        .extract(
                                JSONReader
                                        .ofJSONB(
                                                JSONB.fromJSONBytes(
                                                        str3.getBytes(StandardCharsets.UTF_8)
                                                )
                                        )
                        ).toString()
        );
    }

    @Test
    public void test_3_path_5() {
        JSONPath path = JSONPath.of("$.phoneNumbers[0].*");

        assertEquals("[\"iPhone\",\"0123-4567-8888\"]",
                path.extract(
                        JSONReader.of(str3)
                ).toString()
        );

        assertEquals("[\"iPhone\",\"0123-4567-8888\"]",
                path.extract(
                        JSONReader
                                .ofJSONB(
                                        JSONB.fromJSONBytes(
                                                str3.getBytes(StandardCharsets.UTF_8)
                                        )
                                )
                ).toString()
        );

        assertEquals("[\"iPhone\",\"0123-4567-8888\"]",
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }

    @Test
    public void test_3_path_6() {
        JSONPath path = JSONPath
                .of("$.phoneNumbers[-1:0].type");
        assertEquals("[\"home\"]",
                path
                        .extract(JSONReader
                                .of(str3)
                        ).toString()
        );

        assertEquals("[\"home\"]",
                path.extract(
                        JSONReader
                                .ofJSONB(
                                        JSONB.fromJSONBytes(
                                                str3.getBytes(StandardCharsets.UTF_8)
                                        )
                                )
                ).toString()
        );

        assertEquals("[\"home\"]",
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }

    @Test
    public void test_3_path_7() {
        JSONPath path = JSONPath.of("$..type");
        String expected = "[\"iPhone\",\"home\"]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str3)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str3.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(expected,
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }

    @Test
    public void test_3_path_8() {
        JSONPath path = JSONPath.of("$.phoneNumbers[0]type");
        String expected = "\"iPhone\"";

        assertEquals(expected,
                JSON.toJSONString(
                        path.extract(
                                JSONReader.of(str3)
                        )
                )
        );

        assertEquals(expected,
                JSON.toJSONString(
                        path.extract(
                                JSONReader.ofJSONB(
                                        JSONB.fromJSONBytes(
                                                str3.getBytes(StandardCharsets.UTF_8)
                                        )
                                )
                        )
                )
        );

        assertEquals(
                expected,
                JSON.toJSONString(
                        path.eval(
                                JSON.parseObject(str3)
                        )
                )
        );
    }

    @Test
    public void test_3_path_keys() {
        JSONPath path = JSONPath.of("$.keys()");
        String expected = "[\"firstName\",\"lastName\",\"age\",\"address\",\"phoneNumbers\"]";

        assertEquals(expected,
                path.extract(
                        JSONReader.of(str3)
                ).toString()
        );

        assertEquals(expected,
                path.extract(
                        JSONReader.ofJSONB(
                                JSONB.fromJSONBytes(
                                        str3.getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).toString()
        );

        assertEquals(expected,
                path
                        .eval(
                                JSON.parseObject(str3))
                        .toString());
    }
}
