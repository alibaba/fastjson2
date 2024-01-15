package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.dubbo.common.serialize.fastjson2.FastJson2ObjectInput;
import org.apache.dubbo.common.serialize.fastjson2.FastJson2ObjectOutput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 张治保
 * @since 2024/1/15
 */
public class Issue2181 {
    @ToString
    @Getter
    @Setter
    public static class CommonException extends RuntimeException {
        Integer code;
        String message;
        List params;

        public CommonException(Integer code, String message) {
//            super(message);
            this.message = message;
            this.code = code;
        }

    }

    /**
     * @see FastJson2ObjectOutput#writeObject(Object)
     * @see FastJson2ObjectInput#readObject(Class)
     */
    @Test
    void test() {
        //writeObject
        CommonException error = new CommonException(1, "error");
        byte[] bytes = JSONB.toBytes(
                error,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        // readObject
        Object result = JSONB.parseObject(
                bytes,
                Exception.class,
                JSONReader.autoTypeFilter(true, CommonException.class),
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertNotNull(result);
        assertSame(result.getClass(), CommonException.class);
    }
}
