package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.arrow.CSVUtils;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.aliyun.odps.Column;
import com.aliyun.odps.Instance;
import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsType;
import com.aliyun.odps.data.ArrayRecord;
import com.aliyun.odps.data.RecordWriter;
import com.aliyun.odps.task.SQLTask;
import com.aliyun.odps.tunnel.TableTunnel;

import java.io.File;
import java.util.List;

public class OdpsWriteDemo {
    static final File file = new File("/Users/wenshao/Downloads/Public_School_Characteristics_2020-21.csv");

    public static void main(String[] args) throws Exception {
        Odps odps = OdpsTestUtils.odps();
        String tableName = "x7";

        TableTunnel tunnel = new TableTunnel(odps);

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
        TableTunnel.UploadSession uploadSession = tunnel.createUploadSession(odps.getDefaultProject(), tableName);
        List<Column> columnList = uploadSession.getSchema().getColumns();
        Column[] columns = columnList.toArray(new Column[columnList.size()]);
        RecordWriter recordWriter = uploadSession.openRecordWriter(0);
        CSVReader csvReader = CSVReader.of(file);
        csvReader.readHeader();
        while (true) {
            String[] line = csvReader.readLine();
            if (line == null) {
                break;
            }

            ArrayRecord record = new ArrayRecord(columns);
            for (int i = 0; i < line.length; i++) {
                String value = line[i];
                if (value == null || value.isEmpty()) {
                    continue;
                }

                if (i >= columns.length) {
                    break;
                }
                Column column = columns[i];
                OdpsType odpsType = column.getTypeInfo().getOdpsType();
                switch (odpsType) {
                    case INT:
                        record.setInt(i, Integer.parseInt(value));
                        break;
                    case BIGINT:
                        record.setBigint(i, Long.parseLong(value));
                        break;
                    case STRING:
                        record.set(i, value);
                        break;
                    case DATETIME:
                        record.set(i, DateUtils.parseDate(value));
                        break;
                    case DECIMAL:
                        record.set(i, TypeUtils.toBigDecimal(value));
                        break;
                    case DOUBLE:
                        record.set(i, Double.parseDouble(value));
                        break;
                    default:
                        throw new JSONException("TODO " + odpsType);
                }
            }

            recordWriter.write(record);
        }
        recordWriter.close();
        uploadSession.commit(new Long[] {0L});

        long millis = System.currentTimeMillis() - start;
        System.out.println("millis " + millis);
    }
}
