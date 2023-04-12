package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public class ArrowByteArrayConsumer
        implements ByteArrayValueConsumer {
    static final int CHUNK_SIZE = 10000;
    final Schema schema;
    final int rowCount;
    final int varcharValueSize = 2048;
    final ObjIntConsumer<VectorSchemaRoot> rootConsumer;
    final Consumer<Long[]> committer;
    BufferAllocator allocator;

    VectorSchemaRoot root;
    int blockSize;
    int blockRowIndex;
    int blockIndex = -1;

    public ArrowByteArrayConsumer(
            Schema schema,
            int rowCount,
            ObjIntConsumer<VectorSchemaRoot> rootConsumer,
            Consumer<Long[]> committer
    ) {
        allocator = new RootAllocator();
        this.schema = schema;
        this.rowCount = rowCount;
        this.rootConsumer = rootConsumer;
        this.committer = committer;
        int blockSize = Math.min(CHUNK_SIZE, rowCount);
        allocateNew(blockSize);
    }

    public void afterRow(int row) {
        blockRowIndex++;

        if (blockRowIndex == blockSize) {
            List<Field> fields = root.getSchema().getFields();
            for (int i = 0; i < fields.size(); i++) {
                FieldVector vector = root.getVector(i);
                vector.setValueCount(blockSize);
            }

            rootConsumer.accept(root, blockIndex);
            root.close();

            if (row + 1 == rowCount) {
                if (committer != null) {
                    Long[] blocks = new Long[blockIndex + 1];
                    for (int i = 0; i <= blockIndex; i++) {
                        blocks[i] = Long.valueOf(i);
                    }
                    committer.accept(blocks);
                }
            } else if (row < rowCount) {
                int rest = rowCount - row - 1;
                int blockSize = Math.min(rest, CHUNK_SIZE);
                allocateNew(blockSize);
            }
        }
    }

    public void allocateNew(int blockSize) {
        root = VectorSchemaRoot.create(schema, allocator);

        this.blockSize = blockSize;
        this.blockRowIndex = 0;
        root.setRowCount(blockSize);

        List<Field> fields = root.getSchema().getFields();
        for (int i = 0; i < fields.size(); i++) {
            FieldVector vector = root.getVector(i);
            if (vector instanceof FixedWidthVector) {
                ((FixedWidthVector) vector).allocateNew(blockSize);
            } else if (vector instanceof VariableWidthVector) {
                VariableWidthVector variableWidthVector = (VariableWidthVector) vector;
                variableWidthVector.allocateNew(varcharValueSize * blockSize, blockSize);
            } else {
                throw new JSONException("TODO");
            }
        }

        blockIndex++;
    }

    @Override
    public void accept(int row, int column, byte[] bytes, int off, int len, Charset charset) {
        if (column >= root.getSchema().getFields().size()) {
            return;
        }

        FieldVector vector = root.getVector(column);
        if (len == 0) {
            return;
        }

        row = blockRowIndex;

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
            VarCharVector charVector = (VarCharVector) vector;
            charVector.set(row, bytes, off, len);
            return;
        }

        if (vector instanceof DecimalVector) {
            DecimalVector decimalVector = (DecimalVector) vector;
            ArrowUtils.setDecimal(decimalVector, row, bytes, off, len);
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

        if (vector instanceof BitVector) {
            Boolean value = TypeUtils.parseBoolean(bytes, off, len);
            if (value != null) {
                int intValue = value.booleanValue() ? 1 : 0;
                ((BitVector) vector).set(row, intValue);
            }
            return;
        }

        throw new JSONException("TODO : " + vector.getClass().getName());
    }
}
