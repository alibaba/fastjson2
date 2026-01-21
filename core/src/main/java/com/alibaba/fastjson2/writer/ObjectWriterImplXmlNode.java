package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.lang.reflect.Type;

public class ObjectWriterImplXmlNode
        implements ObjectWriter {
    static final ObjectWriterImplXmlNode INSTANCE = new ObjectWriterImplXmlNode();

    static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        try {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
        } catch (Exception e) {
            throw new JSONException("init xml TransformerFactory error", e);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        try {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            DOMSource domSource = new DOMSource((Node) object);

            StringWriter writer = new StringWriter();
            transformer.transform(domSource, new StreamResult(writer));
            jsonWriter.writeString(writer.toString());
        } catch (Exception e) {
            throw new JSONException("write xml node error", e);
        }
    }
}
