package com.alibaba.fastjson2.eishay.vo;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.util.Arrays;

public class Media_ObjectReader implements ObjectReader {

    private FieldReader[] fieldReaders;

    private FieldReader fieldReader0; // bitrate
    private FieldReader fieldReader1; // copyright
    private FieldReader fieldReader2; // duration
    private FieldReader fieldReader3; // format
    private FieldReader fieldReader4; // height
    private FieldReader fieldReader5; // persons
    private FieldReader fieldReader6; // player
    private FieldReader fieldReader7; // size
    private FieldReader fieldReader8; // title
    private FieldReader fieldReader9; // uri
    private FieldReader fieldReader10; // width
    private ObjectReader fieldObjectReader0; // bitrate
    private ObjectReader fieldObjectReader1; // copyright
    private ObjectReader fieldObjectReader2; // duration
    private ObjectReader fieldObjectReader3; // format
    private ObjectReader fieldObjectReader4; // height
    private ObjectReader fieldObjectReader5; // persons
    private ObjectReader fieldObjectReader7; // size
    private ObjectReader fieldObjectReader8; // title
    private ObjectReader fieldObjectReader9; // uri
    private ObjectReader fieldObjectReader10; // width
    private ObjectReader fieldListItemReader5; // persons.item

    public Media_ObjectReader(FieldReader[] fieldReaders) {
        this.fieldReaders = Arrays.copyOf(fieldReaders, fieldReaders.length);
        Arrays.sort(this.fieldReaders);

        this.fieldReader0 = this.fieldReaders[0];
        this.fieldReader1 = this.fieldReaders[1];
        this.fieldReader2 = this.fieldReaders[2];
        this.fieldReader3 = this.fieldReaders[3];
        this.fieldReader4 = this.fieldReaders[4];
        this.fieldReader5 = this.fieldReaders[5];
        this.fieldReader6 = this.fieldReaders[6];
        this.fieldReader7 = this.fieldReaders[7];
        this.fieldReader8 = this.fieldReaders[8];
        this.fieldReader9 = this.fieldReaders[9];
        this.fieldReader10 = this.fieldReaders[10];
    }

    @Override
    public Object createInstance(long features) {
        return new Media();
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        jsonReader.nextIfObjectStart();
        Media object = new Media();
        for (;;) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hashCode64 = jsonReader.readFieldNameHashCode();
            if (hashCode64 == 0) {
                continue;
            }

            int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
            switch(hashCode32) {
                case 388047649:
                    if (hashCode64 == -8434675112194400115L) { // duration
                        object.setDuration(jsonReader.readInt64Value());
                        continue;
                    }
                    break;
                case -450945852:
                    if (hashCode64 == -3158236125056422850L) { // copyright
                        object.setCopyright(jsonReader.readString());
                        continue;
                    }
                    break;
                case -701871803:
                    if (hashCode64 == -2724350755546111959L) { // title
                        object.setTitle(jsonReader.readString());
                        continue;
                    }
                    break;
                case -200596436:
                    if (hashCode64 == -2604543402434238017L) { // width
                        object.setWidth(jsonReader.readInt32Value());
                        continue;
                    }
                    break;
                case 1961915977:
                    if (hashCode64 == 1560139381482114249L) { // persons
                        int listItemCnt = jsonReader.startArray();
                        java.util.List list = new java.util.ArrayList(listItemCnt);
                        for (int j = 0; j < listItemCnt; ++j) {
                            list.add(
                                    jsonReader.readString());

                        }
                        object.setPersons(list);
                        continue;
                    }
                    break;
                case 1783003604:
                    if (hashCode64 == 1689425963507806754L) { // height
                        object.setHeight(jsonReader.readInt32Value());
                        continue;
                    }
                    break;
                case 2130592624:
                    if (hashCode64 == 5008278420455340480L) { // player
                        Enum fieldValue;
                        if (jsonReader.isInt()) {
                            fieldValue
                                    = this.fieldReader6.getEnumByOrdinal(jsonReader.readInt32Value());
                        } else {
                            fieldValue
                                    = this.fieldReader6.getEnumByHashCode(jsonReader.readValueHashCode());
                        }
                        object.setPlayer((Media.Player) fieldValue);
                        continue;
                    }
                    break;
                case 1904629476:
                    if (hashCode64 == 5498490633151104765L) { // uri
                        object.setUri(jsonReader.readString());
                        continue;
                    }
                    break;
                case -1410189276:
                    if (hashCode64 == 5614464919154503228L) { // size
                        object.setSize(jsonReader.readInt64Value());
                        continue;
                    }
                    break;
                case -1960559364:
                    if (hashCode64 == 7267508590720539762L) { // format
                        object.setFormat(jsonReader.readString());
                        continue;
                    }
                    break;
                case -4434978:
                    if (hashCode64 == 9205194826872731698L) { // bitrate
                        object.setBitrate(jsonReader.readInt32Value());
                        continue;
                    }
                    break;
                default:
                    break;
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
        Media object = new Media();
        for_:
        for (;;) {
            if (jsonReader.current() == '}') {
                jsonReader.next();
                break;
            }

            long hashCode64 = jsonReader.readFieldNameHashCode();
            int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
            switch(hashCode32) {
                case 388047649:
                    if (hashCode64 == -8434675112194400115L) { // duration
                        object.setDuration(jsonReader.readInt64Value());
                        continue;
                    }
                    break;
                case -450945852:
                    if (hashCode64 == -3158236125056422850L) { // copyright
                        object.setCopyright(jsonReader.readString());
                        continue;
                    }
                    break;
                case -701871803:
                    if (hashCode64 == -2724350755546111959L) { // title
                        object.setTitle(jsonReader.readString());
                        continue;
                    }
                    break;
                case -200596436:
                    if (hashCode64 == -2604543402434238017L) { // width
                        object.setWidth(jsonReader.readInt32Value());
                        continue;
                    }
                    break;
                case 1961915977:
                    if (hashCode64 == 1560139381482114249L) { // persons
                        if (jsonReader.current() == '[') {
                            java.util.List list = new java.util.ArrayList();
                            jsonReader.next();
                            for (;;) {
                                if (jsonReader.current() == ']') {
                                    jsonReader.next();
                                    break;
                                }

                                list.add(jsonReader.readString());

                                if (jsonReader.current() == ',') {
                                    jsonReader.next();
                                    continue;
                                }
                            }
                            object.setPersons(list);
                            if (jsonReader.current() == ',') {
                                jsonReader.next();
                            }
                        }
                        continue;
                    }
                    break;
                case 1783003604:
                    if (hashCode64 == 1689425963507806754L) { // height
                        object.setHeight(jsonReader.readInt32Value());
                        continue;
                    }
                    break;
                case 2130592624:
                    if (hashCode64 == 5008278420455340480L) { // player
                        char ch = jsonReader.current();
                        if (ch == '"') {
                            Enum fieldValue;
                            if (jsonReader.isInt()) {
                                fieldValue = this.fieldReader6.getEnumByOrdinal(jsonReader.readInt32Value());
                            } else {
                                fieldValue = this.fieldReader6.getEnumByHashCode(jsonReader.readValueHashCode());
                            }
                            object.setPlayer((Media.Player) fieldValue);
                        } else {
                            throw new JSONException("TODO");
                        }
                        continue;
                    }
                    break;
                case 1904629476:
                    if (hashCode64 == 5498490633151104765L) { // uri
                        object.setUri(jsonReader.readString());
                        continue;
                    }
                    break;
                case -1410189276:
                    if (hashCode64 == 5614464919154503228L) { // size
                        object.setSize(jsonReader.readInt64Value());
                        continue;
                    }
                    break;
                case -1960559364:
                    if (hashCode64 == 7267508590720539762L) { // format
                        object.setFormat(jsonReader.readString());
                        continue;
                    }
                    break;
                case -4434978:
                    if (hashCode64 == 9205194826872731698L) { // bitrate
                        object.setBitrate(jsonReader.readInt32Value());
                        continue;
                    }
                    break;
                default:
                    break;
            }
            String fieldName = jsonReader.getFieldName();
            throw new JSONException("fieldReader not found, fieldName " + fieldName);
        }

        return object;
    }

    @Override
    public FieldReader getFieldReader(long hashCode64) {
        int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
        switch(hashCode32) {
            case 388047649:
                if (hashCode64 == -8434675112194400115L) { // duration
                    return this.fieldReader2;
                }
                break;
            case -450945852:
                if (hashCode64 == -3158236125056422850L) { // copyright
                    return this.fieldReader1;
                }
                break;
            case -701871803:
                if (hashCode64 == -2724350755546111959L) { // title
                    return this.fieldReader8;
                }
                break;
            case -200596436:
                if (hashCode64 == -2604543402434238017L) { // width
                    return this.fieldReader10;
                }
                break;
            case 1961915977:
                if (hashCode64 == 1560139381482114249L) { // persons
                    return this.fieldReader5;
                }
                break;
            case 1783003604:
                if (hashCode64 == 1689425963507806754L) { // height
                    return this.fieldReader4;
                }
                break;
            case 2130592624:
                if (hashCode64 == 5008278420455340480L) { // player
                    return this.fieldReader6;
                }
                break;
            case 1904629476:
                if (hashCode64 == 5498490633151104765L) { // uri
                    return this.fieldReader9;
                }
                break;
            case -1410189276:
                if (hashCode64 == 5614464919154503228L) { // size
                    return this.fieldReader7;
                }
                break;
            case -1960559364:
                if (hashCode64 == 7267508590720539762L) { // format
                    return this.fieldReader3;
                }
                break;
            case -4434978:
                if (hashCode64 == 9205194826872731698L) { // bitrate
                    return this.fieldReader0;
                }
                break;
            default:
                break;
        }
        return null;
    }
}
