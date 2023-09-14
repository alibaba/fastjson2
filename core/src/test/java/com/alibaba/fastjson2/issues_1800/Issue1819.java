package com.alibaba.fastjson2.issues_1800;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.Serialization;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1819 {
    @Test
    public void testWriteReadMap() throws Exception {
        FrameworkModel frameworkModel = new FrameworkModel();
        Serialization serialization = frameworkModel.getExtensionLoader(Serialization.class).getExtension("fastjson2");
        URL url = URL.valueOf("").setScopeModel(frameworkModel);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = serialization.serialize(url, outputStream);
        Map<String, Object> map = new HashMap<>();
        List<Integer> workBysOrigin = new ArrayList<>();
        workBysOrigin.add(120731003);
        workBysOrigin.add(140707005);
        map.put("work_by", workBysOrigin);
        objectOutput.writeObject(map);
        objectOutput.flushBuffer();

        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = serialization.deserialize(url, inputStream);
        map = objectInput.readObject(Map.class);
        List<Integer> workBys = (List) map.get("work_by");
        for (int i=0; i<workBys.size(); i++) {
            assertEquals(workBysOrigin.get(i), workBys.get(i));
        }
    }
}
