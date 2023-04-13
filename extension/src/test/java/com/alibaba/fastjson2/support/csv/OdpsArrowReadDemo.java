package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.ArrowRecordReader;
import com.aliyun.odps.tunnel.TableTunnel;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

import java.io.File;
import java.util.List;

public class OdpsArrowReadDemo {
    private static String accessID = "";
    private static String accessKey = "";
    private static String project = "sonar_test";

    public static void main(String[] args) throws Exception {
        Account account = new AliyunAccount(accessID, accessKey);
        Odps odps = new Odps(account);
        odps.setDefaultProject(project);
        String tableName = "x7";

        TableTunnel tunnel = new TableTunnel(odps);
        TableTunnel.DownloadSession downloadSession = tunnel.createDownloadSession(project, tableName);
        long recordCount = downloadSession.getRecordCount();
        System.out.println("recordCount : " + recordCount);

        if (recordCount >= 1000000) {
            throw new JSONException("too large : " + recordCount);
        }

        File file = new File("/Users/wenshao/Downloads/odps_download_" + tableName + ".csv");
        CSVWriter writer = CSVWriter.of(file);

        Schema arrowSchema = downloadSession.getArrowSchema();
        List<Field> fields = arrowSchema.getFields();
        writer.writeLine(
                fields.size(),
                column -> fields.get(column).getName()
        );

        long start = System.currentTimeMillis();
        int off = 0;
        ArrowRecordReader arrowRecordReader = downloadSession.openArrowRecordReader(off, recordCount);

        while (true) {
            VectorSchemaRoot root = arrowRecordReader.read();
            int rowCount = root.getRowCount();
            if (rowCount == 0) {
                break;
            }

            ArrowUtils.write(writer, root);
//            System.out.println(new java.util.Date() + " rowCount : " + rowCount + ", " + off + "/" + recordCount);
            off += rowCount;
            if (off >= recordCount) {
                break;
            }
        }
        arrowRecordReader.close();

        long millis = System.currentTimeMillis() - start;
        System.out.println("millis " + millis);

        writer.close();
    }
}
