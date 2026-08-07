package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.arrow.ArrowUtils;
import com.alibaba.fastjson2.support.arrow.CSVUtils;
import com.aliyun.odps.Instance;
import com.aliyun.odps.Odps;
import com.aliyun.odps.data.ArrowRecordWriter;
import com.aliyun.odps.task.SQLTask;
import com.aliyun.odps.tunnel.TableTunnel;
import com.aliyun.odps.tunnel.io.CompressOption;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class OdpsArrowWriteDemo1 {
    static final File file = new File("/Users/wenshao/Downloads/Public_School_Characteristics_2020-21.csv");

    public static void main(String[] args) throws Exception {
        Odps odps = OdpsTestUtils.odps();
        String tableName = "x7";

        {
            String dropTable = "drop table if exists " + tableName + ";";
            System.out.println(dropTable);
            Instance dropTableTask = SQLTask.run(odps, dropTable);
            dropTableTask.waitForSuccess();

            String ddl = CSVUtils.genMaxComputeCreateTable(file, tableName);
            System.out.println(ddl);

            Instance createTableTask = SQLTask.run(odps, ddl);
            createTableTask.waitForSuccess();
        }

        long start = System.currentTimeMillis();

        TableTunnel tunnel = new TableTunnel(odps);
        writeData(odps.getDefaultProject(), tableName, tunnel);

        long millis = System.currentTimeMillis() - start;
        System.out.println("millis " + millis);
    }

    private static void writeData(
            String project,
            String tableName,
            TableTunnel tunnel
    ) throws Exception {
        TableTunnel.UploadSession uploadSession = tunnel.createUploadSession(project, tableName);
        CompressOption compressOption = new CompressOption(CompressOption.CompressAlgorithm.ODPS_ARROW_LZ4_FRAME, 0, 0);
        ArrowRecordWriter arrowRecordWriter = uploadSession.openArrowRecordWriter(0, compressOption);
        Schema arrowSchema = uploadSession.getArrowSchema();
        List<Field> fields = arrowSchema.getFields();
        BufferAllocator allocator = new RootAllocator();
        VectorSchemaRoot root = createRoot(arrowSchema, fields, allocator);

        CSVReader csvReader = CSVReader.of(file);
        csvReader.readHeader();
        int row = 0;
        for (; ; row++) {
            String[] line = csvReader.readLine();
            if (line == null) {
                break;
            }

            for (int i = 0; i < line.length; i++) {
                String value = line[i];
                if (value == null || value.isEmpty()) {
                    continue;
                }

                if (i >= fields.size()) {
                    break;
                }

                FieldVector vector = root.getVector(i);
                ArrowUtils.setValue(vector, row, value);
            }
        }
        root.setRowCount(row);
        arrowRecordWriter.write(root);
        arrowRecordWriter.close();

        uploadSession.commit(new Long[]{0L});
    }

    @NotNull
    private static VectorSchemaRoot createRoot(
            Schema arrowSchema,
            List<Field> fields,
            BufferAllocator allocator
    ) throws Exception {
        VectorSchemaRoot root = VectorSchemaRoot.create(arrowSchema, allocator);

        int rowCount = CSVReader.rowCount(file);
        System.out.println("rowCount : " + rowCount);

        int blockSize = rowCount;
        final int varcharValueSize = 128;
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
        return root;
    }
}
