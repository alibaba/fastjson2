package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.google.common.collect.Lists;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2712 {
    @Test
    void test() {
        ClueListInfo info1 = new ClueListInfo();
        info1.setClueId(1L);
        info1.setName("Clue 01");

        ClueListInfo info2 = new ClueListInfo();
        info2.setClueId(2L);
        info2.setName("Clue 02");

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setId(1L);
        contactInfo.setName("contact");
        ArrayList<ContactInfo> contacts = Lists.newArrayList(contactInfo);

        info1.setContacts(contacts);
        info2.setContacts(contacts);
        List<ClueListInfo> infos = Lists.newArrayList(info1, info2);

        String jsonString = JSON.toJSONString(infos, JSONWriter.Feature.ReferenceDetection);
        List<ClueListInfo> clueListInfos = JSON.parseArray(jsonString, ClueListInfo.class);
        assertSame(clueListInfos.get(0).getContacts(), clueListInfos.get(1).getContacts());

        assertNotNull(JSON.register(ClueListInfo.class,
                ObjectWriterCreator.INSTANCE.createObjectWriter(ClueListInfo.class)));

        assertEquals(jsonString,
                JSON.toJSONString(infos, JSONWriter.Feature.ReferenceDetection));

        assertNotNull(JSON.register(ClueListInfo.class,
                ObjectReaderCreator.INSTANCE.createObjectReader(ClueListInfo.class)));

        List<ClueListInfo> clueListInfos1 = JSON.parseArray(jsonString, ClueListInfo.class);
        assertSame(clueListInfos1.get(0).getContacts(), clueListInfos1.get(1).getContacts());
    }

    @Data
    public static class ClueListInfo
            implements Serializable {
        private static final long serialVersionUID = 8170584149020082450L;

        private Long clueId;

        private String name;

        private List<ContactInfo> contacts;
    }

    @Data
    public static class ContactInfo
            implements Serializable {
        private static final long serialVersionUID = -5313505758293424804L;

        private Long id;

        private String name;
    }
}
