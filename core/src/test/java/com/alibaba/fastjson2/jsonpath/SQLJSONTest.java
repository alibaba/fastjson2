package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SQLJSONTest {
    @Test
    public void test() {
        assertEquals(
                101,
                JSONObject
                        .of("id", 101)
                        .eval(
                                JSONPath.of("strict $.id")
                        )
        );
        assertEquals(
                101,
                JSONObject
                        .of("id", 101)
                        .eval(
                                JSONPath.of("lax $.id")
                        )
        );
    }

    @Test
    public void all() {
        assertEquals(
                "[123,\"wenshao\"]",
                JSONPath.extract("{\"id\":123,\"name\":\"wenshao\"}", "$.*")
                        .toString()
        );
    }

    @Test
    public void test35() {
        String str = "{ 'phones': [\n" +
                "  { 'type': \"cell\", 'number': \"abc-defg\" },\n" +
                "  {               'number': \"pqr-wxyz\" },\n" +
                "  { 'type': \"home\", 'number': \"hij-klmn\" } ] }";
        assertEquals("{\"phones\":[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]}",
                JSONPath.extract(str, "$")
                        .toString()
        );
        assertEquals("[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]",
                JSONPath.extract(str, "$.phones")
                        .toString()
        );
        assertEquals("[\"cell\",\"home\"]",
                JSONPath.extract(str, "$.phones.type")
                        .toString()
        );
    }

    @Test
    public void test36() {
        String str = "{ \"who\": \"Fred\", \"where\": \"General Products\", \"friends\": [ { \"name\": \"Lili\", \"rank\": 5 }, {\"name\": \"Hank\", \"rank\": 7} ] }";
        assertEquals("{\"who\":\"Fred\",\"where\":\"General Products\",\"friends\":[{\"name\":\"Lili\",\"rank\":5},{\"name\":\"Hank\",\"rank\":7}]}",
                JSONPath.extract(str, "$")
                        .toString()
        );
        assertEquals("[{\"name\":\"Lili\",\"rank\":5},{\"name\":\"Hank\",\"rank\":7}]",
                JSONPath.extract(str, "$.friends")
                        .toString()
        );
        assertEquals("[{\"name\":\"Lili\",\"rank\":5},{\"name\":\"Hank\",\"rank\":7}]",
                JSONPath.extract(str, "$.friends[*]")
                        .toString()
        );
        assertEquals("[5,7]",
                JSONPath.extract(str, "$.friends[*].rank")
                        .toString()
        );
    }

    @Test
    public void test37() {
        String str = "{ 'phones': [\n" +
                "  { 'type': \"cell\", 'number': \"abc-defg\" },\n" +
                "  {               'number': \"pqr-wxyz\" },\n" +
                "  { 'type': \"home\", 'number': \"hij-klmn\" } ] }";
        assertEquals("{\"phones\":[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]}",
                JSONPath.extract(str, "$")
                        .toString()
        );
        assertEquals("[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]",
                JSONPath.extract(str, "$.phones")
                        .toString()
        );
        assertEquals("[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]",
                JSONPath.extract(str, "$.phones[*]")
                        .toString()
        );
        assertEquals("[\"cell\",\"home\"]",
                JSONPath.extract(str, "$.phones[*].type")
                        .toString()
        );
    }

    @Test
    public void test38() {
        String str = "{ 'phones': [\n" +
                "  { 'type': \"cell\", 'number': \"abc-defg\" },\n" +
                "  {               'number': \"pqr-wxyz\" },\n" +
                "  { 'type': \"home\", 'number': \"hij-klmn\" } ] }";

        assertEquals("{\"phones\":[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]}",
                JSONPath.extract(str, "$")
                        .toString()
        );
        assertEquals("[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]",
                JSONPath.extract(str, "$.phones")
                        .toString()
        );
        assertEquals("[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]",
                JSONPath.extract(str, "$.phones[*]? (exists(@.type))")
                        .toString()
        );

        assertEquals("[\"cell\",\"home\"]",
                JSONPath.extract(str, "$.phones[*]? (exists(@.type)).type")
                        .toString()
        );
    }

    @Test
    public void test39() {
        String str = "{ 'phones': [\n" +
                "  { 'type': \"cell\", 'number': \"abc-defg\" },\n" +
                "  {               'number': \"pqr-wxyz\" },\n" +
                "  { 'type': \"home\", 'number': \"hij-klmn\" } ] }";
        assertEquals("[\"cell\",\"abc-defg\",\"pqr-wxyz\",\"home\",\"hij-klmn\"]",
                JSONPath.eval(
                        JSON.parseObject(str),
                                "$.phones.*"
                        ).toString()
        );

        assertEquals("[\"cell\",\"abc-defg\",\"pqr-wxyz\",\"home\",\"hij-klmn\"]",
                JSONPath.extract(str, "$.phones.*")
                        .toString()
        );
    }

    @Test
    public void test40() {
        String str = "{ 'phones': [\n" +
                "  { 'type': \"cell\", 'number': \"abc-defg\" },\n" +
                "  {               'number': \"pqr-wxyz\" },\n" +
                "  { 'type': \"home\", 'number': \"hij-klmn\" } ] }";

        assertEquals("[{\"type\":\"cell\",\"number\":\"abc-defg\"},{\"number\":\"pqr-wxyz\"},{\"type\":\"home\",\"number\":\"hij-klmn\"}]",
                JSONPath.extract(str, "$.phones[*]")
                        .toString()
        );
        assertEquals("[\"cell\",\"abc-defg\",\"pqr-wxyz\",\"home\",\"hij-klmn\"]",
                JSONPath.extract(str, "$.phones[*].*")
                        .toString()
        );
    }

    @Test
    public void test41() {
        String str = "{ 'sensors':\n" +
                "  { 'SF': [10,11,12,13,15,16,17],\n" +
                "    'FC': [20,22,24],\n" +
                "'SJ': [30,33] }}";

        assertEquals("{\"SF\":[10,11,12,13,15,16,17],\"FC\":[20,22,24],\"SJ\":[30,33]}",
                JSONPath.extract(str, "$.sensors")
                        .toString()
        );
        assertEquals("[[10,11,12,13,15,16,17],[20,22,24],[30,33]]",
                JSONPath.extract(str, "$.sensors.*")
                        .toString()
        );
        assertEquals("[10,17,12,20,24,24,30,33]",
                JSONPath.extract(str, "$.sensors.*[0,last,2]")
                        .toString()
        );
        assertEquals("[10,12,17,20,24,24,30,33]",
                JSONPath.extract(str, "$.sensors.*[0,2,last]")
                        .toString()
        );
    }

    @Test
    public void test44() {
        String str = "{ 'readings': [15.2, -22.3, 45.9] }";

        assertEquals("{\"readings\":[15.2,-22.3,45.9]}",
                JSONPath.extract(str, "$")
                        .toString()
        );

        assertEquals("[15.2,-22.3,45.9]",
                JSONPath.extract(str, "$.readings")
                        .toString()
        );

        assertEquals("[15,-23,45]",
                JSONPath.extract(str, "$.readings.floor()")
                        .toString()
        );

        assertEquals("[-15,23,-45]",
                JSONPath.extract(str, "-$.readings.floor()")
                        .toString()
        );
    }

    @Test
    public void test45() {
        String str = "{ 'readings': [15.2, -22.3, 45.9] }";

        assertEquals("{\"readings\":[15.2,-22.3,45.9]}",
                JSONPath.extract(str, "$")
                        .toString()
        );

        assertEquals("[15.2,-22.3,45.9]",
                JSONPath.extract(str, "$.readings")
                        .toString()
        );

        assertEquals("[-15.2,22.3,-45.9]",
                JSONPath.extract(str, "-$.readings")
                        .toString()
        );
//
//        assertEquals("[-15.2,22.3,-45.9]",
//                JSONPath.extract(str, "(-$.readings)")
//                        .toString()
//        );
    }

    @Test
    public void test47() {
        String str = "{ 'pay': 100, 'hours': \"ten\" }";

        assertEquals("{\"pay\":100,\"hours\":\"ten\"}",
                JSONPath.extract(str, "$")
                        .toString()
        );

        assertEquals("{\"pay\":100,\"hours\":\"ten\"}",
                JSONPath.extract(str, "@")
                        .toString()
        );

        assertEquals("100",
                JSONPath.extract(str, "@.pay")
                        .toString()
        );

        assertEquals("\"ten\"",
                JSON.toJSONString(
                        JSONPath.extract(str, "@.hours")
                )
        );
    }

    @Test
    public void test58() {
        String str = "{ 'name': { 'first': \"Manny\",\n" +
                "                                    'last': \"Moe\" }, 'points': 123 }";

        assertEquals("{\"name\":{\"first\":\"Manny\",\"last\":\"Moe\"},\"points\":123}",
                JSONPath.extract(str, "$")
                        .toString()
        );

        assertEquals("{\"name\":{\"first\":\"Manny\",\"last\":\"Moe\"},\"points\":123}",
                JSONPath.extract(str, "@")
                        .toString()
        );

        assertEquals("{\"first\":\"Manny\",\"last\":\"Moe\"}",
                JSONPath.extract(str, "@.name")
                        .toString()
        );

        assertEquals("true",
                JSONPath.extract(str, "exists (@.name)")
                        .toString()
        );

        assertEquals("{\"name\":{\"first\":\"Manny\",\"last\":\"Moe\"},\"points\":123}",
                JSONPath.extract(str, "$?(exists (@.name)) ")
                        .toString()
        );

        assertEquals("{\"first\":\"Manny\",\"last\":\"Moe\"}",
                JSONPath.extract(str, "$?(exists (@.name)).name ")
                        .toString()
        );
    }
}
