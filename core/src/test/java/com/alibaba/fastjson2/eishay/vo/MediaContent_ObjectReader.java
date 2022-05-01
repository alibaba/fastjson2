package com.alibaba.fastjson2.eishay.vo;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.util.Arrays;

public class MediaContent_ObjectReader implements ObjectReader {

    private FieldReader[] fieldReaders;

    private FieldReader fieldReader0; // images
    private FieldReader fieldReader1; // media
    private ObjectReader fieldObjectReader0; // images
    private ObjectReader fieldObjectReader1; // media
    private ObjectReader fieldListItemReader0; // images.item

    public MediaContent_ObjectReader(FieldReader[] fieldReaders) {
        this.fieldReaders = Arrays.copyOf(fieldReaders, fieldReaders.length);
        Arrays.sort(this.fieldReaders);

        this.fieldReader0 = this.fieldReaders[0];
        this.fieldReader1 = this.fieldReaders[1];
    }

    @Override
    public Object createInstance(long features) {
        return new MediaContent();
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        jsonReader.nextIfObjectStart();
        MediaContent object = new MediaContent();
        for (;;) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hashCode64 = jsonReader.readFieldNameHashCode();
            if (hashCode64 == 0) {
                continue;
            }

            if (hashCode64 == -5533876349178564733L) { // media
                if (this.fieldObjectReader1 == null) {
                    this.fieldObjectReader1 = jsonReader.getContext()
                            .getObjectReader(Media.class); // media
                }
                object.setMedia((Media)
                        fieldObjectReader1.readJSONBObject(jsonReader, 0));
                continue;
            }
            if (hashCode64 == -4924010017516690453L) { // images
                int listItemCnt = jsonReader.startArray();
                java.util.List list = new java.util.ArrayList(listItemCnt);
                for (int j = 0; j < listItemCnt; ++j) {
                    list.add(
                            fieldListItemReader0.readJSONBObject(jsonReader, 0));

                }
                object.setImages(list);
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
        MediaContent object = new MediaContent();
        for_:
        for (;;) {
            if (jsonReader.current() == '}') {
                jsonReader.next();
                break;
            }

            long hashCode64 = jsonReader.readFieldNameHashCode();
            if (hashCode64 == -5533876349178564733L) { // media
                if (this.fieldObjectReader1 == null) {
                    this.fieldObjectReader1 = jsonReader.getContext()
                            .getObjectReader(Media.class); // media
                }
                object.setMedia((Media)
                        fieldObjectReader1.readObject(jsonReader, 0));
                continue;
            }
            if (hashCode64 == -4924010017516690453L) { // images
                if (jsonReader.current() == '[') {
                    if (fieldListItemReader0 == null) {
                        fieldListItemReader0 = jsonReader.getContext()
                                .getObjectReader(Image.class);
                    }

                    java.util.List list = new java.util.ArrayList();
                    jsonReader.next();
                    for (;;) {
                        if (jsonReader.current() == ']') {
                            jsonReader.next();
                            break;
                        }

                        list.add(
                                fieldListItemReader0.readObject(jsonReader, 0));

                        if (jsonReader.current() == ',') {
                            jsonReader.next();
                            continue;
                        }
                    }
                    object.setImages(list);
                    if (jsonReader.current() == ',') {
                        jsonReader.next();
                    }
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
        if (hashCode64 == -5533876349178564733L) { // media
            return this.fieldReader1;
        }
        if (hashCode64 == -4924010017516690453L) { // images
            return this.fieldReader0;
        }
        return null;
    }
}
