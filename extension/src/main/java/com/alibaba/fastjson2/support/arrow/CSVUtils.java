package com.alibaba.fastjson2.support.arrow;

import com.alibaba.fastjson2.stream.StreamReader;
import com.alibaba.fastjson2.support.csv.CSVReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CSVUtils {
    public static String genMaxComputeCreateTable(File file, String tableName) throws IOException {
        CSVReader csvReader = CSVReader.of(file);
        csvReader.readHeader();
        csvReader.statAll();

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

        List<StreamReader.ColumnStat> columns = csvReader.getColumnStats();
        for (int i = 0; i < columns.size(); i++) {
            StreamReader.ColumnStat columnStat = columns.get(i);

            String columnName;
            {
                StringBuffer buf = new StringBuffer();
                for (int j = 0; j < columnStat.name.length(); j++) {
                    char ch = columnStat.name.charAt(j);
                    if (ch == 0xFFFD) {
                        continue;
                    }

                    if (ch == ' ' || ch == '-' || ch == '+' || ch == '.') {
                        buf.append('_');
                    } else {
                        buf.append(ch);
                    }
                }
                columnName = buf.toString();
            }

            boolean special = false;
            for (int j = 0; j < columnName.length(); j++) {
                char ch = columnName.charAt(j);
                boolean firstIdent = (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';

                if (j == 0) {
                    if (!firstIdent) {
                        special = true;
                        break;
                    }
                }

                if (!firstIdent && !(ch >= '0' && ch <= '9')) {
                    special = true;
                    break;
                }
            }
            if (!special && columnName.length() > 30) {
                special = true;
            }

            sql.append('\t');

            if (special) {
                sql.append("COL_" + i);
            } else {
                sql.append(columnName);
            }

            sql.append(' ');
            sql.append(
                    columnStat.getInferSQLType()
            );

            if (special) {
                sql.append(" COMMENT '");
                for (int j = 0; j < columnStat.name.length(); j++) {
                    char ch = columnStat.name.charAt(j);
                    if (ch == 0xFFFD) {
                        continue;
                    }

                    if (ch == '\'') {
                        sql.append(ch);
                    }
                    sql.append(ch);
                }
                sql.append('\'');
            }

            if (i != columns.size() - 1) {
                sql.append(',');
            }
            sql.append("\n");
        }

        sql.append(");");

        return sql.toString();
    }
}
