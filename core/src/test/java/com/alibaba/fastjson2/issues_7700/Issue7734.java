package com.alibaba.fastjson2.issues_7700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for #7734: a {@code BigDecimal} field annotated with Jackson's
 * {@code @JsonFormat(shape = STRING)} produced invalid JSON such as
 * {@code {"amount":string200}} instead of {@code {"amount":"200"}}.
 *
 * <p>Root cause: the {@code "string"} sentinel (set for shape=STRING) was fed
 * to {@code new DecimalFormat(...)} as a pattern, so {@code writeDecimal} took
 * the formatted-string branch and emitted the raw pattern text unquoted. The
 * fix keeps {@code WriteNonStringValueAsString} (set for shape=STRING) in charge
 * and never builds a {@code DecimalFormat} from a sentinel, in every place a
 * {@code DecimalFormat} is constructed from a field format: the
 * {@code FieldWriter} constructor, the static {@code FieldWriter.getObjectWriter}
 * and {@code ObjectWriterProvider.getObjectWriter} (reached for collection
 * items via {@code FieldWriterList.getItemWriter}).
 *
 * <p>For shape=NUMBER the sibling sentinel {@code "millis"} is assigned only to
 * non-numeric fields (it selects the date epoch-millis mode), the DecimalFormat
 * sites exclude it the same way as {@code "string"}, and the bean-level format
 * is not inherited by numeric fields ({@code BeanUtils.inheritBeanFormat}), so
 * shape=NUMBER on a numeric field, declared or inherited from a class-level
 * annotation, serializes as a plain number instead of pattern text such as
 * {@code millis200} or {@code "millis"}.
 *
 * <p>Covers the directly-declared {@code BigDecimal} case plus the sibling
 * paths that share the same sentinel handling: collection items, polymorphic
 * {@code Object} and {@code Number} fields whose runtime value is a
 * {@code BigDecimal} or {@code BigDecimal[]} (runtime {@code getObjectWriter}),
 * and the {@code float}/{@code double} BeanToArray paths (reflective
 * {@code writeValue} and ASM array mapping), which previously ignored
 * {@code WriteNonStringValueAsString}.
 */
@Tag("regression")
public class Issue7734 {
    public static class Bean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal amount;
    }

    @Test
    public void bigDecimalStringShapeSerializesAsQuotedString() {
        Bean bean = new Bean();
        bean.amount = new BigDecimal("6.56000000001");
        String json = JSON.toJSONString(bean);
        // must be valid JSON with the value quoted as a string
        assertEquals("{\"amount\":\"6.56000000001\"}", json);
        Bean parsed = JSON.parseObject(json, Bean.class);
        assertEquals(new BigDecimal("6.56000000001"), parsed.amount);
    }

    @Test
    public void bigDecimalStringShapeNullRoundTripsWithoutCorruption() {
        Bean bean = new Bean();
        // amount is null; a null BigDecimal must round-trip cleanly
        String json = JSON.toJSONString(bean);
        Bean parsed = JSON.parseObject(json, Bean.class);
        assertNull(parsed.amount);
    }

    public static class PatternBean {
        // a real DecimalFormat pattern (no shape=STRING) must still be applied,
        // proving the fix only skips the "string" sentinel, not genuine patterns
        @JsonFormat(pattern = "0.00")
        public BigDecimal amount;
    }

    @Test
    public void realDecimalFormatPatternStillApplied() {
        PatternBean bean = new PatternBean();
        bean.amount = new BigDecimal("7");
        String json = JSON.toJSONString(bean);
        // exact match: a plain substring check would also pass for the wrong value
        assertEquals("{\"amount\":7.00}", json);
    }

    // shape=STRING on a polymorphic Object field whose runtime value is BigDecimal
    public static class PolyObjectBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Object amount;
    }

    @Test
    public void polymorphicObjectBigDecimalShapeString() {
        PolyObjectBean bean = new PolyObjectBean();
        bean.amount = new BigDecimal("200");
        assertEquals("{\"amount\":\"200\"}", JSON.toJSONString(bean));
    }

    @Test
    public void polymorphicObjectBigDecimalArrayShapeString() {
        PolyObjectBean bean = new PolyObjectBean();
        bean.amount = new BigDecimal[]{new BigDecimal("1.23"), new BigDecimal("4.56")};
        assertEquals("{\"amount\":[\"1.23\",\"4.56\"]}", JSON.toJSONString(bean));
    }

    // shape=NUMBER on a polymorphic Object field whose runtime value is BigDecimal
    public static class PolyNumberShapeBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Object amount;
    }

    @Test
    public void polymorphicObjectBigDecimalShapeNumber() {
        PolyNumberShapeBean bean = new PolyNumberShapeBean();
        bean.amount = new BigDecimal("200");
        assertEquals("{\"amount\":200}", JSON.toJSONString(bean));
    }

    @Test
    public void polymorphicObjectBigDecimalArrayShapeNumber() {
        // exercises the "millis" clause of the BigDecimal[] branch in the
        // runtime getObjectWriter, previously only tested for scalars
        PolyNumberShapeBean bean = new PolyNumberShapeBean();
        bean.amount = new BigDecimal[]{new BigDecimal("200")};
        assertEquals("{\"amount\":[200]}", JSON.toJSONString(bean));
    }

    // shape=STRING on a polymorphic Number field whose runtime value is BigDecimal
    public static class PolyNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Number amount;
    }

    @Test
    public void polymorphicNumberBigDecimalShapeString() {
        PolyNumberBean bean = new PolyNumberBean();
        bean.amount = new BigDecimal("200");
        assertEquals("{\"amount\":\"200\"}", JSON.toJSONString(bean));
    }

    // shape=STRING on collection fields: the sentinel reaches
    // ObjectWriterProvider.getObjectWriter via FieldWriterList.getItemWriter
    public static class ListBigDecimalBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public List<BigDecimal> amounts;
    }

    public static class ListDoubleBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public List<Double> values;
    }

    public static class ListFloatBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public List<Float> values;
    }

    @Test
    public void listBigDecimalShapeStringQuoted() {
        ListBigDecimalBean bean = new ListBigDecimalBean();
        bean.amounts = Arrays.asList(new BigDecimal("200"), new BigDecimal("7"));
        assertEquals("{\"amounts\":[\"200\",\"7\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void listDoubleShapeStringQuoted() {
        ListDoubleBean bean = new ListDoubleBean();
        bean.values = Arrays.asList(2.0D, 2.0D);
        assertEquals("{\"values\":[\"2.0\",\"2.0\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void listFloatShapeStringQuoted() {
        ListFloatBean bean = new ListFloatBean();
        bean.values = Arrays.asList(2.0F, 2.0F);
        assertEquals("{\"values\":[\"2.0\",\"2.0\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void listBigDecimalShapeStringJsonbRoundTrip() {
        ListBigDecimalBean bean = new ListBigDecimalBean();
        bean.amounts = Arrays.asList(new BigDecimal("200"), new BigDecimal("7"));
        byte[] jsonb = JSONB.toBytes(bean);
        ListBigDecimalBean parsed = JSONB.parseObject(jsonb, ListBigDecimalBean.class);
        assertEquals(bean.amounts, parsed.amounts);
    }

    // shape=NUMBER on numeric fields: the "millis" sentinel must not reach
    // new DecimalFormat either, the values serialize as plain numbers
    public static class NumberShapeBigDecimalBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public BigDecimal amount;
    }

    public static class NumberShapeDoubleBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Double value;
    }

    public static class NumberShapeFloatBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Float value;
    }

    public static class NumberShapeListBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public List<BigDecimal> amounts;
    }

    @Test
    public void numberShapeBigDecimalPlain() {
        NumberShapeBigDecimalBean bean = new NumberShapeBigDecimalBean();
        bean.amount = new BigDecimal("200");
        assertEquals("{\"amount\":200}", JSON.toJSONString(bean));
    }

    @Test
    public void numberShapeDoublePlain() {
        NumberShapeDoubleBean bean = new NumberShapeDoubleBean();
        bean.value = 2.0D;
        assertEquals("{\"value\":2.0}", JSON.toJSONString(bean));
    }

    @Test
    public void numberShapeFloatPlain() {
        NumberShapeFloatBean bean = new NumberShapeFloatBean();
        bean.value = 2.0F;
        assertEquals("{\"value\":2.0}", JSON.toJSONString(bean));
    }

    @Test
    public void numberShapeListPlain() {
        NumberShapeListBean bean = new NumberShapeListBean();
        bean.amounts = Arrays.asList(new BigDecimal("200"), new BigDecimal("7"));
        assertEquals("{\"amounts\":[200,7]}", JSON.toJSONString(bean));
    }

    public static class IntShapeNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int value = 5;
    }

    @Test
    public void intShapeNumberPlain() {
        // FieldWriterInt32 passes the format to writeInt32(int, String), where
        // String.format("millis", value) silently emits the sentinel text
        assertEquals("{\"value\":5}", JSON.toJSONString(new IntShapeNumberBean()));
    }

    public static class DateShapeNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date = new Date(1690000000000L);
    }

    @Test
    public void dateShapeNumberEpochMillis() {
        // the "millis" sentinel is semantic for date fields: epoch millis as number
        assertEquals("{\"date\":1690000000000}", JSON.toJSONString(new DateShapeNumberBean()));
    }

    public static class GetterShapeNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int getValue() {
            return 5;
        }
    }

    @Test
    public void getterShapeNumberPlain() {
        // annotation on the accessor is processed through the method call site
        // of the type-aware producer
        assertEquals("{\"value\":5}", JSON.toJSONString(new GetterShapeNumberBean()));
    }

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public static class ClassLevelShapeNumberBean {
        public int f = 5;
        public long l = 200L;
        public Date date = new Date(1690000000000L);
    }

    @Test
    public void classLevelShapeNumberPlain() {
        // class-level shape=NUMBER is inherited as the "millis" sentinel, which
        // must not reach the numeric fields (int previously emitted "millis")
        // while the Date field keeps the epoch-millis mode; reading is unchanged
        String json = "{\"date\":1690000000000,\"f\":5,\"l\":200}";
        assertEquals(json, JSON.toJSONString(new ClassLevelShapeNumberBean()));
        ClassLevelShapeNumberBean parsed = JSON.parseObject(json, ClassLevelShapeNumberBean.class);
        assertEquals(5, parsed.f);
        assertEquals(200L, parsed.l);
        assertEquals(new Date(1690000000000L), parsed.date);
    }

    public static class LongShapeNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public long value = 1690000000000L;
    }

    @Test
    public void longShapeNumberPlainAndJsonb() {
        // text is unchanged; in JSONB the field is an int64 (a plain long field
        // never had date semantics), and the encoding round-trips
        assertEquals("{\"value\":1690000000000}", JSON.toJSONString(new LongShapeNumberBean()));
        byte[] jsonb = JSONB.toBytes(new LongShapeNumberBean());
        assertArrayEquals(new byte[]{-90, 78, 118, 97, 108, 117, 101, -66, 0, 0, 1, -119, 123, -39, -124, 0, -91}, jsonb);
        assertEquals(1690000000000L, JSONB.parseObject(jsonb, LongShapeNumberBean.class).value);
    }

    public static class FloatBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Float value;
    }

    @Test
    public void floatScalarShapeStringQuoted() {
        FloatBean bean = new FloatBean();
        bean.value = 1.5F;
        assertEquals("{\"value\":\"1.5\"}", JSON.toJSONString(bean));
    }

    @Test
    public void floatBeanToArrayShapeStringQuoted() {
        // BeanToArray uses writeValue / ASM array mapping, which previously
        // ignored WriteNonStringValueAsString; exercise both creators.
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            FloatBean bean = new FloatBean();
            bean.value = 1.5F;
            ObjectWriter<FloatBean> writer = creator.createObjectWriter(FloatBean.class);
            JSONWriter w = JSONWriter.of(JSONWriter.Feature.BeanToArray);
            writer.write(w, bean, null, null, 0);
            assertEquals("[\"1.5\"]", w.toString(),
                    "Float shape=STRING BeanToArray via " + creator.getClass().getSimpleName());
        }
    }

    @Test
    public void floatShapeStringJsonbRoundTrip() {
        FloatBean bean = new FloatBean();
        bean.value = 1.5F;
        byte[] jsonb = JSONB.toBytes(bean);
        FloatBean parsed = JSONB.parseObject(jsonb, FloatBean.class);
        assertEquals(1.5F, parsed.value);
    }

    public static class DoubleBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Double value;
    }

    @Test
    public void doubleScalarShapeStringQuoted() {
        DoubleBean bean = new DoubleBean();
        bean.value = 1.5;
        assertEquals("{\"value\":\"1.5\"}", JSON.toJSONString(bean));
    }

    @Test
    public void doubleBeanToArrayShapeStringQuoted() {
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            DoubleBean bean = new DoubleBean();
            bean.value = 1.5;
            ObjectWriter<DoubleBean> writer = creator.createObjectWriter(DoubleBean.class);
            JSONWriter w = JSONWriter.of(JSONWriter.Feature.BeanToArray);
            writer.write(w, bean, null, null, 0);
            assertEquals("[\"1.5\"]", w.toString(),
                    "Double shape=STRING BeanToArray via " + creator.getClass().getSimpleName());
        }
    }

    public static class PrimitiveFloatBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public float value;
    }

    @Test
    public void primitiveFloatBeanToArrayShapeStringQuoted() {
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            PrimitiveFloatBean bean = new PrimitiveFloatBean();
            bean.value = 1.5F;
            ObjectWriter<PrimitiveFloatBean> writer = creator.createObjectWriter(PrimitiveFloatBean.class);
            JSONWriter w = JSONWriter.of(JSONWriter.Feature.BeanToArray);
            writer.write(w, bean, null, null, 0);
            assertEquals("[\"1.5\"]", w.toString(),
                    "float shape=STRING BeanToArray via " + creator.getClass().getSimpleName());
        }
    }

    // primitive double and boxed-array fields lock the sentinel guard in the
    // FieldWriter constructor: without it a sentinel reaches new DecimalFormat
    // and the raw pattern text is emitted unquoted
    public static class PrimitiveDoubleBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double value;
    }

    public static class NumberShapePrimitiveDoubleBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public double value;
    }

    @Test
    public void primitiveDoubleShapeStringQuoted() {
        PrimitiveDoubleBean bean = new PrimitiveDoubleBean();
        bean.value = 1.5;
        assertEquals("{\"value\":\"1.5\"}", JSON.toJSONString(bean));
    }

    @Test
    public void primitiveDoubleShapeNumberPlain() {
        NumberShapePrimitiveDoubleBean bean = new NumberShapePrimitiveDoubleBean();
        bean.value = 1.5;
        assertEquals("{\"value\":1.5}", JSON.toJSONString(bean));
    }

    public static class DoubleArrayBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Double[] values;
    }

    public static class NumberShapeDoubleArrayBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Double[] values;
    }

    @Test
    public void boxedDoubleArrayShapeStringQuoted() {
        DoubleArrayBean bean = new DoubleArrayBean();
        bean.values = new Double[]{1.5, 2.0};
        assertEquals("{\"values\":[\"1.5\",\"2.0\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void boxedDoubleArrayShapeNumberPlain() {
        NumberShapeDoubleArrayBean bean = new NumberShapeDoubleArrayBean();
        bean.values = new Double[]{1.5, 2.0};
        assertEquals("{\"values\":[1.5,2.0]}", JSON.toJSONString(bean));
    }

    public static class FloatArrayBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Float[] values;
    }

    public static class NumberShapeFloatArrayBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Float[] values;
    }

    @Test
    public void boxedFloatArrayShapeStringQuoted() {
        FloatArrayBean bean = new FloatArrayBean();
        bean.values = new Float[]{1.5F, 2.0F};
        assertEquals("{\"values\":[\"1.5\",\"2.0\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void boxedFloatArrayShapeNumberPlain() {
        NumberShapeFloatArrayBean bean = new NumberShapeFloatArrayBean();
        bean.values = new Float[]{1.5F, 2.0F};
        assertEquals("{\"values\":[1.5,2.0]}", JSON.toJSONString(bean));
    }

    public static class BigDecimalArrayBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal[] values;
    }

    public static class NumberShapeBigDecimalArrayBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public BigDecimal[] values;
    }

    @Test
    public void boxedBigDecimalArrayShapeStringQuoted() {
        BigDecimalArrayBean bean = new BigDecimalArrayBean();
        bean.values = new BigDecimal[]{new BigDecimal("1.23"), new BigDecimal("4.56")};
        assertEquals("{\"values\":[\"1.23\",\"4.56\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void boxedBigDecimalArrayShapeNumberPlain() {
        NumberShapeBigDecimalArrayBean bean = new NumberShapeBigDecimalArrayBean();
        bean.values = new BigDecimal[]{new BigDecimal("1.23"), new BigDecimal("4.56")};
        assertEquals("{\"values\":[1.23,4.56]}", JSON.toJSONString(bean));
    }

    @Test
    public void bigDecimalShapeStringJsonbRoundTrip() {
        // JSONB is binary; verify the value survives a round-trip
        Bean bean = new Bean();
        bean.amount = new BigDecimal("200");
        byte[] jsonb = JSONB.toBytes(bean);
        Bean parsed = JSONB.parseObject(jsonb, Bean.class);
        assertEquals(new BigDecimal("200"), parsed.amount);
    }

    public static class PlainDoubleBean {
        public Double value;
    }

    @Test
    public void jsonbBeanToArrayWritersAgreeOnWriteAsStringFeature() {
        // JSONB + BeanToArray with WriteNonStringValueAsString set at writer level
        // only: the reflective writeValue and the ASM array mapping must produce
        // the same encoding (string elements, because the feature is honored)
        byte[] reflectionBytes = null;
        byte[] asmBytes = null;
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            PlainDoubleBean bean = new PlainDoubleBean();
            bean.value = 1.5D;
            ObjectWriter<PlainDoubleBean> writer = creator.createObjectWriter(PlainDoubleBean.class);
            JSONWriter w = JSONWriter.ofJSONB(
                    JSONWriter.Feature.BeanToArray,
                    JSONWriter.Feature.WriteNonStringValueAsString);
            writer.write(w, bean, null, null, 0);
            byte[] bytes = w.getBytes();
            if (reflectionBytes == null) {
                reflectionBytes = bytes;
            } else {
                asmBytes = bytes;
            }
        }
        assertArrayEquals(reflectionBytes, asmBytes, "reflective and ASM creators must agree");
        // string encoding embeds the ASCII text, binary double does not
        assertTrue(new String(reflectionBytes, StandardCharsets.ISO_8859_1).contains("1.5"),
                "expected string element, got " + Arrays.toString(reflectionBytes));

        // without the feature both creators must agree on the binary encoding
        reflectionBytes = null;
        asmBytes = null;
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            PlainDoubleBean bean = new PlainDoubleBean();
            bean.value = 1.5D;
            ObjectWriter<PlainDoubleBean> writer = creator.createObjectWriter(PlainDoubleBean.class);
            JSONWriter w = JSONWriter.ofJSONB(JSONWriter.Feature.BeanToArray);
            writer.write(w, bean, null, null, 0);
            byte[] bytes = w.getBytes();
            if (reflectionBytes == null) {
                reflectionBytes = bytes;
            } else {
                asmBytes = bytes;
            }
        }
        assertArrayEquals(reflectionBytes, asmBytes, "reflective and ASM creators must agree");
        assertFalse(new String(reflectionBytes, StandardCharsets.ISO_8859_1).contains("1.5"),
                "expected binary element, got " + Arrays.toString(reflectionBytes));
    }

    public static class FieldLevelShapeStringBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Double amount = 1.5D;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double prim = 2.5D;
    }

    @Test
    public void jsonbBeanToArrayFieldLevelShapeStringWritersAgree() {
        // Jackson shape=STRING sets WriteNonStringValueAsString at field level,
        // which the runtime writer-level guard cannot see: with the generated
        // array mapping the boxed Double was written as a binary double while
        // the reflective creator wrote a string
        byte[] reflectionBytes = null;
        byte[] asmBytes = null;
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            FieldLevelShapeStringBean bean = new FieldLevelShapeStringBean();
            ObjectWriter<FieldLevelShapeStringBean> writer = creator.createObjectWriter(FieldLevelShapeStringBean.class);
            JSONWriter w = JSONWriter.ofJSONB(JSONWriter.Feature.BeanToArray);
            writer.write(w, bean, null, null, 0);
            byte[] bytes = w.getBytes();
            if (reflectionBytes == null) {
                reflectionBytes = bytes;
            } else {
                asmBytes = bytes;
            }
        }
        assertArrayEquals(reflectionBytes, asmBytes, "reflective and ASM creators must agree");
        String text = new String(reflectionBytes, StandardCharsets.ISO_8859_1);
        assertTrue(text.contains("1.5") && text.contains("2.5"),
                "expected string elements, got " + Arrays.toString(reflectionBytes));
    }
}
