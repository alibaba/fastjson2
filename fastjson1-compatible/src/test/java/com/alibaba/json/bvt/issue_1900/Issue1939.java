package com.alibaba.json.bvt.issue_1900;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1939 {
    @XmlRootElement(name = "Container")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "any"
    })
    public static class Container
            implements Serializable {
        @XmlAnyElement(lax = true)
        public List<Object> any;
    }

    private static final String MESSAGE = "<Container>" +
            "<WeightMajor measurementSystem=\"English\" unit=\"lbs\">0</WeightMajor>" +
            "</Container>";

    @Test
    public void test_for_issue() throws Exception {
        JAXBContext context = JAXBContext.newInstance(Container.class, Issue1939.class);
        Container con = (Container) context.createUnmarshaller().unmarshal(new StringReader(MESSAGE));
        assertEquals("{\"any\":[\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?><WeightMajor measurementSystem=\\\"English\\\" unit=\\\"lbs\\\">0</WeightMajor>\"]}",
                JSON.toJSONString(con));
    }

    @Test
    public void test_for_issue_1() throws Exception {
        JAXBContext context = JAXBContext.newInstance(Container.class, Issue1939.class);
        Container con = (Container) context.createUnmarshaller().unmarshal(new StringReader(MESSAGE));
//        assertEquals("{\"any\":[\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?><WeightMajor measurementSystem=\\\"English\\\" unit=\\\"lbs\\\">0</WeightMajor>\"]}",
//                JSON.toJSON(con).toString());
    }
}
