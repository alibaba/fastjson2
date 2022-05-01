package com.alibaba.fastjson2.eishay.vo;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.util.Arrays;

public class Image_ObjectReader implements ObjectReader {

    private FieldReader[] fieldReaders;

    private FieldReader fieldReader0; // height
    private FieldReader fieldReader1; // size
    private FieldReader fieldReader2; // title
    private FieldReader fieldReader3; // uri
    private FieldReader fieldReader4; // width
    private ObjectReader fieldObjectReader0; // height
    private ObjectReader fieldObjectReader2; // title
    private ObjectReader fieldObjectReader3; // uri
    private ObjectReader fieldObjectReader4; // width
    private ObjectReader fieldListItemReader0; // height.item
    private ObjectReader fieldListItemReader4; // width.item

    public Image_ObjectReader(FieldReader[] fieldReaders) {
        this.fieldReaders = Arrays.copyOf(fieldReaders, fieldReaders.length);
        Arrays.sort(this.fieldReaders);

        this.fieldReader0 = this.fieldReaders[0];
        this.fieldReader1 = this.fieldReaders[1];
        this.fieldReader2 = this.fieldReaders[2];
        this.fieldReader3 = this.fieldReaders[3];
        this.fieldReader4 = this.fieldReaders[4];
    }

    @Override
    public Object createInstance(long features) {
        return new Image();
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        jsonReader.nextIfObjectStart();
        Image object = new Image();
        for (;;) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hashCode64 = jsonReader.readFieldNameHashCode();
            if (hashCode64 == 0) {
                continue;
            }

            if (hashCode64 == -2724350755546111959L) { // title
                object.setTitle(jsonReader.readString());
                continue;
            }
            if (hashCode64 == -2604543402434238017L) { // width
                object.setWidth(jsonReader.readInt32Value());
                continue;
            }
            if (hashCode64 == 1689425963507806754L) { // height
                object.setHeight(jsonReader.readInt32Value());
                continue;
            }
            if (hashCode64 == 5498490633151104765L) { // uri
                object.setUri(jsonReader.readString());
                continue;
            }
            if (hashCode64 == 5614464919154503228L) { // size
                Image.Size fieldValue;
                if (jsonReader.isInt()) {
                    fieldValue = (Image.Size) this.fieldReader1.getEnumByOrdinal(jsonReader.readInt32Value());
                } else {
                    long hash = jsonReader.readValueHashCode();
                    fieldValue
                            = (Image.Size) this.fieldReader1.getEnumByHashCode(hash);
                }

                object.setSize(fieldValue);
                continue;
            }
            String fieldName = jsonReader.getFieldName();
            throw new JSONException("fieldReader not found, fieldName " + fieldName);
        }
        return object;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, 0);
        }

        jsonReader.next();
        Image object = new Image();
        for_:
        for (;;) {
            if (jsonReader.current() == '}') {
                jsonReader.next();
                break;
            }

            long hashCode64 = jsonReader.readFieldNameHashCode();
            if (hashCode64 == -2724350755546111959L) { // title
                object.setTitle(jsonReader.readString());
                continue;
            }
            if (hashCode64 == -2604543402434238017L) { // width
                object.setWidth(jsonReader.readInt32Value());
                continue;
            }
            if (hashCode64 == 1689425963507806754L) { // height
                object.setHeight(jsonReader.readInt32Value());
                continue;
            }
            if (hashCode64 == 5498490633151104765L) { // uri
                object.setUri(jsonReader.readString());
                continue;
            }
            if (hashCode64 == 5614464919154503228L) { // size
                char ch = jsonReader.current();
                if (ch == '"') {
                    long hash = jsonReader.readValueHashCode();
                    Image.Size fieldValue
                            = (Image.Size) this.fieldReader1.getEnumByHashCode(hash);
                    object.setSize(fieldValue);
                } else {
                    throw new JSONException("TODO");
                }
                continue;
            }
            String fieldName = jsonReader.getFieldName();
            throw new JSONException("fieldReader not found, fieldName " + fieldName);
        }

        return object;
    }

    @Override
    public FieldReader getFieldReader(long hashCode64) {
        if (hashCode64 == -2724350755546111959L) { // title
            return this.fieldReader2;
        }
        if (hashCode64 == -2604543402434238017L) { // width
            return this.fieldReader4;
        }
        if (hashCode64 == 1689425963507806754L) { // height
            return this.fieldReader0;
        }
        if (hashCode64 == 5498490633151104765L) { // uri
            return this.fieldReader3;
        }
        if (hashCode64 == 5614464919154503228L) { // size
            return this.fieldReader1;
        }
        return null;
    }
}
