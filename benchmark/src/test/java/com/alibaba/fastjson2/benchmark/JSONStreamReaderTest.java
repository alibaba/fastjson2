package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.stream.JSONStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class JSONStreamReaderTest {
    public static void main(String[] args) throws Exception {
        File tempFile = new File("/Users/wenshao/Downloads/2023-04-02-0.json");
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();

            int rowCount = 0;
            try (
                    InputStream fis = new FileInputStream(tempFile)
            ) {
                JSONStreamReader streamReader = JSONStreamReader.of(fis);

                Object object;
                while ((object = streamReader.readLineObject()) != null) {
                    JSONObject jsonObject = (JSONObject) object;
                    jsonObject.size();
                    rowCount++;
                }
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Stream-readLoneObject millis : " + millis);
        }
    }
}
