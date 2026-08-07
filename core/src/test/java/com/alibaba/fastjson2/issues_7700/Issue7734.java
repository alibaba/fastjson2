package com.alibaba.fastjson2.issues_7700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression for #7734: a {@code BigDecimal} field annotated with Jackson's
 * {@code @JsonFormat(shape = STRING)} produced invalid JSON such as
 * {@code {"amount":string200}} instead of {@code {"amount":"200"}}.
 *
 * <p>Root cause: the {@code "string"} sentinel (set for shape=STRING) was fed
 * to {@code new DecimalFormat(...)} as a pattern, so {@code writeDecimal} took
 * the formatted-string branch and emitted the raw pattern text unquoted. The
 * fix keeps {@code WriteNonStringValueAsString} (set for shape=STRING) in charge
 * and never builds a {@code DecimalFormat} from the sentinel, in every place a
 * {@code DecimalFormat} is constructed from a field format.
 *
 * <p>Covers the directly-declared {@code BigDecimal} case plus the sibling
 * paths that share the same sentinel handling: polymorphic {@code Object} and
 * {@code Number} fields whose runtime value is a {@code BigDecimal} (runtime
 * {@code getObjectWriter}), and the {@code float}/{@code double} BeanToArray
 * paths (reflective {@code writeValue} and ASM array mapping), which previously
 * ignored {@code WriteNonStringValueAsString}.
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
        byte[] jsonb = JSON.toJSONBytes(bean);
        FloatBean parsed = JSON.parseObject(jsonb, FloatBean.class);
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

    @Test
    public void bigDecimalShapeStringJsonbRoundTrip() {
        // JSONB is binary; verify the value survives a round-trip
        Bean bean = new Bean();
        bean.amount = new BigDecimal("200");
        byte[] jsonb = JSON.toJSONBytes(bean);
        Bean parsed = JSON.parseObject(jsonb, Bean.class);
        assertEquals(new BigDecimal("200"), parsed.amount);
    }
}
