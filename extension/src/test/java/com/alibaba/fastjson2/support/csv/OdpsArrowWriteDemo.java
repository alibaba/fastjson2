package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.ArrowRecordWriter;
import com.aliyun.odps.tunnel.TableTunnel;
import com.aliyun.odps.tunnel.TunnelException;
import com.aliyun.odps.tunnel.io.CompressOption;
import org.apache.arrow.vector.types.pojo.Schema;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class OdpsArrowWriteDemo {
    // EPA_SmartLocationDatabase_V3_Jan_2021_Final.csv X4
    static final File file = new File("/Users/wenshao/Downloads/AH_Provisional_COVID-19_Deaths_by_Race_and_Educational_Attainment.csv");

    private static String accessID = "";
    private static String accessKey = "";
    private static String project = "sonar_test";

    public static void main(String[] args) throws Exception {
        Account account = new AliyunAccount(accessID, accessKey);
        Odps odps = new Odps(account);
        odps.setDefaultProject(project);
        String tableName = "x6";

        TableTunnel tunnel = new TableTunnel(odps);

        CSVReader csvReader = CSVReader.of(file);
        csvReader.readHeader();

        TableTunnel.UploadSession uploadSession = tunnel.createUploadSession(project, tableName);
        int rowCount = CSVReader.rowCount(file) - 1;
        System.out.println("rowCount : " + rowCount);

        final Schema schema = uploadSession.getArrowSchema();

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
                        System.out.println("commit blocks " + Arrays.toString(blocks));
                        uploadSession.commit(blocks);
                    } catch (TunnelException | IOException e) {
                        throw new JSONException("commit error", e);
                    }
                }
        );

        csvReader.readAll(consumer);
    }
}
