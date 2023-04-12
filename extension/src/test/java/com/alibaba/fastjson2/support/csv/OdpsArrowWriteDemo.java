package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.aliyun.odps.Instance;
import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.ArrowRecordWriter;
import com.aliyun.odps.task.SQLTask;
import com.aliyun.odps.tunnel.TableTunnel;
import com.aliyun.odps.tunnel.TunnelException;
import com.aliyun.odps.tunnel.io.CompressOption;
import org.apache.arrow.vector.types.pojo.Schema;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;

public class OdpsArrowWriteDemo {
    // EPA_SmartLocationDatabase_V3_Jan_2021_Final.csv X4
    static final File file = new File("/Users/wenshao/Downloads/Demographics_by_Zip_Code.csv");

    private static String accessID = "";
    private static String accessKey = "";
    private static String project = "sonar_test";

    public static void main(String[] args) throws Exception {
        Account account = new AliyunAccount(accessID, accessKey);
        Odps odps = new Odps(account);
        odps.setDefaultProject(project);
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

        TableTunnel.UploadSession uploadSession = tunnel.createUploadSession(project, tableName);
        int rowCount = CSVReader.rowCount(file) - 1;
        System.out.println("rowCount : " + rowCount);

        final Schema schema = uploadSession.getArrowSchema();

        long start = System.currentTimeMillis();
        CompressOption compressOption = new CompressOption(CompressOption.CompressAlgorithm.ODPS_ARROW_LZ4_FRAME, 0, 0);

        ArrowByteArrayConsumer consumer = new ArrowByteArrayConsumer(
                schema,
                rowCount,
                (root, blockIndex) -> {
                    try {
                        System.out.println("write block " + blockIndex + " [" + root.getRowCount() + "]");
                        ArrowRecordWriter arrowRecordWriter = uploadSession.openArrowRecordWriter(blockIndex, compressOption);
                        arrowRecordWriter.write(root);
                        arrowRecordWriter.close();
                    } catch (TunnelException | IOException e) {
                        throw new JSONException("write block error " + blockIndex, e);
                    }
                },
                blocks -> {
                    try {
                        long millis = System.currentTimeMillis() - start;
                        NumberFormat fmt = NumberFormat.getNumberInstance();
                        System.out.println(
                                "commit blocks " + Arrays.toString(blocks)
                                        + " timeMills : " + millis
                                        + ", size : " + fmt.format(file.length())
                        );
                        uploadSession.commit(blocks);
                    } catch (TunnelException | IOException e) {
                        throw new JSONException("commit error", e);
                    }
                }
        );

        CSVReader csvReader = CSVReader.of(file, consumer);
        csvReader.readHeader();
        csvReader.readAll();
    }
}
