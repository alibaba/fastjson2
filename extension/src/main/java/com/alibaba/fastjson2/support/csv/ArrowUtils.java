package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.vector.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteOrder;
import java.util.List;

import static com.alibaba.fastjson2.util.JDKUtils.FIELD_DECIMAL_INT_COMPACT_OFFSET;

public class ArrowUtils {
    static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    static final byte DECIMAL_TYPE_WIDTH = 16;

    public static void write(CSVWriter writer, VectorSchemaRoot root) throws IOException {
        List<FieldVector> fieldVectors = root.getFieldVectors();

        int rowCount = root.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < fieldVectors.size(); j++) {
                if (j != 0) {
                    writer.writeComma();
                }

                FieldVector fieldVector = fieldVectors.get(j);
                if (fieldVector.isNull(i)) {
                    continue;
                }

                if (fieldVector instanceof IntVector) {
                    int value = ((IntVector) fieldVector).get(i);
                    writer.writeInt32(value);
                } else if (fieldVector instanceof BigIntVector) {
                    long value = ((BigIntVector) fieldVector).get(i);
                    writer.writeInt64(value);
                } else if (fieldVector instanceof VarCharVector) {
                    byte[] value = ((VarCharVector) fieldVector).get(i);
                    writer.writeString(value);
                } else if (fieldVector instanceof DecimalVector) {
                    DecimalVector decimalVector = (DecimalVector) fieldVector;
                    writeDecimal(writer, i, decimalVector);
                } else if (fieldVector instanceof DateMilliVector) {
                    long millis = ((DateMilliVector) fieldVector).get(i);
                    writer.writeDate(millis);
                } else if (fieldVector instanceof Float8Vector) {
                    double value = ((Float8Vector) fieldVector).get(i);
                    writer.writeDouble(value);
                } else if (fieldVector instanceof Float4Vector) {
                    float value = ((Float4Vector) fieldVector).get(i);
                    writer.writeFloat(value);
                } else if (fieldVector instanceof SmallIntVector) {
                    short value = ((SmallIntVector) fieldVector).get(i);
                    writer.writeInt32(value);
                } else if (fieldVector instanceof TinyIntVector) {
                    short value = ((TinyIntVector) fieldVector).get(i);
                    writer.writeInt32(value);
                } else if (fieldVector instanceof BitVector) {
                    int value = ((BitVector) fieldVector).get(i);
                    writer.writeInt32(value);
                } else if (fieldVector instanceof Decimal256Vector) {
                    Object object = fieldVector.getObject(i);
                    writer.writeString(object.toString());
                } else {
                    throw new JSONException("TODO : " + fieldVector.getClass().getName());
                }
            }

            writer.writeLine();
        }
    }

    private static void writeDecimal(
            CSVWriter writer,
            int row,
            DecimalVector decimalVector
    ) {
        int precision = decimalVector.getPrecision();

        decimalVector.getObject(row);
        if (precision < 20) {
            final long startIndex = (long) row * DecimalVector.TYPE_WIDTH;
            int scale = decimalVector.getScale();
            long unscaleValue;
            ArrowBuf dataBuffer = decimalVector.getDataBuffer();
            if (LITTLE_ENDIAN) {
                unscaleValue = dataBuffer.getLong(startIndex);
            } else {
                long littleEndianValue = dataBuffer.getLong(startIndex + 8);
                unscaleValue = Long.reverseBytes(littleEndianValue);
            }
            writer.writeDecimal(unscaleValue, scale);
        } else {
            BigDecimal decimal = decimalVector.getObject(row);
            writer.writeDecimal(decimal);
        }
    }

    public static void setDecimal(DecimalVector vector, int row, byte[] bytes, int off, int len) {
        boolean negative = false;
        int j = off;
        if (bytes[off] == '-') {
            negative = true;
            j++;
        }

        if (len <= 20 || (negative && len == 21)) {
            int end = off + len;
            int dot = 0;
            int dotIndex = -1;
            long unscaleValue = 0;
            for (; j < end; j++) {
                byte b = bytes[j];
                if (b == '.') {
                    dot++;
                    if (dot > 1) {
                        break;
                    }
                    dotIndex = j;
                } else if (b >= '0' && b <= '9') {
                    unscaleValue = unscaleValue * 10 + (b - '0');
                } else {
                    unscaleValue = -1;
                    break;
                }
            }
            int scale = 0;
            if (unscaleValue >= 0 && dot <= 1) {
                if (dotIndex != -1) {
                    scale = len - (dotIndex - off) - 1;
                }

                boolean overflow = false;
                long unscaleValueV = unscaleValue;
                int scaleV = vector.getScale();
                if (scaleV > scale) {
                    for (int i = scale; i < scaleV; i++) {
                        unscaleValueV *= 10;
                        if (unscaleValueV < 0) {
                            overflow = true;
                            break;
                        }
                    }
                } else if (scaleV < scale) {
                    overflow = true;
                }

                if (!overflow) {
                    if (negative) {
                        unscaleValueV = -unscaleValueV;
                    }
                    BitVectorHelper.setBit(vector.getValidityBuffer(), row);

                    ArrowBuf dataBuffer = vector.getDataBuffer();
                    final long startIndex = (long) row * DECIMAL_TYPE_WIDTH;
                    if (LITTLE_ENDIAN) {
                        // Decimal stored as native-endian, need to swap data bytes before writing to ArrowBuf if LE
                        // Write LE data
                        dataBuffer.setLong(startIndex, unscaleValueV);
                    } else {
                        // Write BE data
                        dataBuffer.setLong(startIndex, 0);
                        long littleEndianValue = Long.reverseBytes(unscaleValueV);
                        dataBuffer.setLong(startIndex + 8, littleEndianValue);
                    }
                    return;
                }

                if (negative) {
                    unscaleValue = -unscaleValue;
                }

                BigDecimal decimal = BigDecimal.valueOf(unscaleValue, scale);
                if (vector.getScale() != decimal.scale()) {
                    decimal = decimal.setScale(vector.getScale(), BigDecimal.ROUND_CEILING);
                }
                vector.set(row, decimal);
                return;
            }
        }

        BigDecimal decimal = TypeUtils.parseBigDecimal(bytes, off, len);
        if (vector.getScale() != decimal.scale()) {
            decimal = decimal.setScale(vector.getScale(), BigDecimal.ROUND_CEILING);
        }
        vector.set(row, decimal);
    }

    public static void setDecimal(DecimalVector vector, int row, BigDecimal decimal) {
        int scale = vector.getScale();
        if (decimal.scale() != scale) {
            decimal = decimal.setScale(scale, BigDecimal.ROUND_CEILING);
        }
        int precision = decimal.precision();
        if (precision < 20 && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1) {
            long unscaleValue = UnsafeUtils.getLong(decimal, FIELD_DECIMAL_INT_COMPACT_OFFSET);
            if (unscaleValue != Long.MIN_VALUE) {
                BitVectorHelper.setBit(vector.getValidityBuffer(), row);
                ArrowBuf dataBuffer = vector.getDataBuffer();
                final long startIndex = (long) row * DECIMAL_TYPE_WIDTH;
                if (LITTLE_ENDIAN) {
                    // Decimal stored as native-endian, need to swap data bytes before writing to ArrowBuf if LE
                    // Write LE data
                    dataBuffer.setLong(startIndex, unscaleValue);
                } else {
                    // Write BE data
                    dataBuffer.setLong(startIndex, 0);
                    long littleEndianValue = Long.reverseBytes(unscaleValue);
                    dataBuffer.setLong(startIndex + 8, littleEndianValue);
                }
                return;
            }
        }

        vector.set(row, decimal);
    }
}
