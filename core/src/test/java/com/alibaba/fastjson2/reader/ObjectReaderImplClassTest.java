package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectReaderImplClassTest {
    /**
     * Verifies PR-2: ObjectReaderImplClass refuses to resolve a class whose FQCN is on the
     * {@link JDKUtils#AUTO_TYPE_DENY_FQCN} deny list, even when both SupportAutoType and
     * SupportClassForName are opted into. Without the defence-in-depth added in PR-2, this test
     * would successfully call Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl")
     * and load the TemplatesImpl class.
     */
    @Test
    public void testRefusesDangerousVal() {
        JSONReader.Feature[] features = new JSONReader.Feature[]{
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.SupportClassForName
        };
        String payload = "{\"@type\":\"java.lang.Class\",\"val\":\"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl\"}";
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(payload, Class.class, features)
        );
    }

    /**
     * Verifies PR-2 also rejects java.lang.Runtime (a 1.x-era gadget) via the same path.
     */
    @Test
    public void testRefusesRuntimeVal() {
        JSONReader.Feature[] features = new JSONReader.Feature[]{
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.SupportClassForName
        };
        String payload = "{\"@type\":\"java.lang.Class\",\"val\":\"java.lang.Runtime\"}";
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(payload, Class.class, features)
        );
    }
}