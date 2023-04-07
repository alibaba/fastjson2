package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.Field;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

public class ArrowByteArrayConsumer
        implements CSVReader.ByteArrayConsumer {
    final VectorSchemaRoot root;

    public ArrowByteArrayConsumer(VectorSchemaRoot root) {
        this.root = root;
    }

    public void allocateNew(int rowCount) {
        root.setRowCount(rowCount);

        List<Field> fields = root.getSchema().getFields();
        for (int i = 0; i < fields.size(); i++) {
            FieldVector vector = root.getVector(i);
            if (vector instanceof FixedWidthVector) {
                ((FixedWidthVector) vector).allocateNew(rowCount);
            } else if (vector instanceof VariableWidthVector) {
                ((VariableWidthVector) vector).allocateNew(rowCount);
            } else {
                throw new JSONException("TODO");
            }
        }
    }

    @Override
    public void accept(int row, int column, byte[] bytes, int off, int len, Charset charset) {
        if (column >= root.getSchema().getFields().size()) {
            return;
        }

        FieldVector vector = root.getVector(column);
        if (len == 0) {
            vector.setNull(row);
            return;
        }

        if (vector instanceof IntVector) {
            int intValue = TypeUtils.parseInt(bytes, off, len);
            ((IntVector) vector).set(row, intValue);
            return;
        }

        if (vector instanceof BigIntVector) {
            long longValue = TypeUtils.parseLong(bytes, off, len);
            ((BigIntVector) vector).set(row, longValue);
            return;
        }

        if (vector instanceof VarCharVector) {
            ((VarCharVector) vector).set(row, bytes, off, len);
            return;
        }

        if (vector instanceof DecimalVector) {
            BigDecimal decimal = TypeUtils.parseBigDecimal(bytes, off, len);
            DecimalVector decimalVector = (DecimalVector) vector;
            int scale = decimalVector.getScale();
            if (decimal.scale() != scale) {
                decimal = decimal.setScale(scale);
            }
            decimalVector.set(row, decimal);
            return;
        }

        if (vector instanceof Decimal256Vector) {
            BigDecimal decimal = TypeUtils.parseBigDecimal(bytes, off, len);
            Decimal256Vector decimalVector = (Decimal256Vector) vector;
            int scale = decimalVector.getScale();
            if (decimal.scale() != scale) {
                decimal = decimal.setScale(scale);
            }
            decimalVector.set(row, decimal);
            return;
        }

        if (vector instanceof SmallIntVector) {
            int intValue = TypeUtils.parseInt(bytes, off, len);
            ((SmallIntVector) vector).set(row, intValue);
            return;
        }

        if (vector instanceof TinyIntVector) {
            int intValue = TypeUtils.parseInt(bytes, off, len);
            ((TinyIntVector) vector).set(row, intValue);
            return;
        }

        if (vector instanceof Float4Vector) {
            float floatValue = TypeUtils.parseFloat(bytes, off, len);
            ((Float4Vector) vector).set(row, floatValue);
            return;
        }

        if (vector instanceof Float8Vector) {
            float floatValue = TypeUtils.parseFloat(bytes, off, len);
            ((Float8Vector) vector).set(row, floatValue);
            return;
        }

        if (vector instanceof DateMilliVector) {
            long millis = DateUtils.parseMillis(bytes, off, len, charset);
            ((DateMilliVector) vector).set(row, millis);
            return;
        }

        if (vector instanceof TimeStampMilliVector) {
            long millis = DateUtils.parseMillis(bytes, off, len, charset);
            ((TimeStampMilliVector) vector).set(row, millis);
            return;
        }

        throw new JSONException("TODO : " + vector.getClass().getName());
    }
}
