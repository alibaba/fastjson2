package com.alibaba.fastjson2.writer;

import static com.alibaba.fastjson2.internal.asm.ASMUtils.*;
import static com.alibaba.fastjson2.internal.asm.ASMUtils.TYPE_JSON_UTF16_IO;
import static com.alibaba.fastjson2.internal.asm.ASMUtils.TYPE_JSON_UTF8_IO;
import static com.alibaba.fastjson2.writer.ObjectWriterCreatorASM.*;

public enum JSONWriterType {
    JSONB,
    JSON_UTF8,
    JSON_UTF16;

    public String jsonWriterType() {
        switch (this) {
            case JSONB:
                return TYPE_JSONB_WRITER;
            case JSON_UTF8:
                return TYPE_JSON_WRITER_UTF8;
            default:
                return TYPE_JSON_WRITER_UTF16;
        }
    }

    public String writeArrayMappingMethodName() {
        switch (this) {
            case JSONB:
                return "writeArrayMappingJSONB";
            case JSON_UTF8:
                return "writeArrayMappingUTF8";
            default:
                return "writeArrayMappingUTF16";
        }
    }

    public String writeArrayMappingMethodDesc() {
        switch (this) {
            case JSONB:
                return METHOD_DESC_WRITE_JSONB;
            case JSON_UTF8:
                return METHOD_DESC_WRITE_UTF8;
            default:
                return METHOD_DESC_WRITE_UTF16;
        }
    }

    public String writeMethodName() {
        switch (this) {
            case JSONB:
                return "writeJSONB";
            case JSON_UTF8:
                return "writeUTF8";
            default:
                return "writeUTF16";
        }
    }

    public String writeMethodDesc() {
        switch (this) {
            case JSONB:
                return METHOD_DESC_WRITE_JSONB;
            case JSON_UTF8:
                return METHOD_DESC_WRITE_UTF8;
            default:
                return METHOD_DESC_WRITE_UTF16;
        }
    }

    public String fieldWriteMethodName() {
        switch (this) {
            case JSONB:
                return "writeJSONB";
            case JSON_UTF8:
                return "writeUTF8";
            default:
                return "writeUTF16";
        }
    }

    public String fieldWriteMethodDesc() {
        switch (this) {
            case JSONB:
                return METHOD_DESC_FIELD_WRITE_OBJECT_JSONB;
            case JSON_UTF8:
                return METHOD_DESC_FIELD_WRITE_OBJECT_UTF8;
            default:
                return METHOD_DESC_FIELD_WRITE_OBJECT_UTF16;
        }
    }

    public String writeListValueMethodName() {
        switch (this) {
            case JSONB:
                return "writeListValueJSONB";
            case JSON_UTF8:
                return "writeListValueUTF8";
            default:
                return "writeListValueUTF16";
        }
    }

    public String writeListValueMethodDesc() {
        switch (this) {
            case JSONB:
                return METHOD_DESC_WRITE_LIST_JSONB;
            case JSON_UTF8:
                return METHOD_DESC_WRITE_LIST_UTF8;
            default:
                return METHOD_DESC_WRITE_LIST_UTF16;
        }
    }

    public String writEnumMethodName() {
        switch (this) {
            case JSONB:
                return "writeEnumJSONB";
            case JSON_UTF8:
                return "writeEnumUTF8";
            default:
                return "writeEnumUTF16";
        }
    }

    public String writeEnumMethodDesc() {
        switch (this) {
            case JSONB:
                return METHOD_DESC_WRITE_ENUM_JSONB;
            case JSON_UTF8:
                return METHOD_DESC_WRITE_ENUM_UTF8;
            default:
                return METHOD_DESC_WRITE_ENUM_UTF16;
        }
    }

    public String writeFieldName() {
        switch (this) {
            case JSONB:
                return "writeFieldNameJSONB";
            case JSON_UTF8:
                return "writeFieldNameUTF8";
            default:
                return "writeFieldNameUTF16";
        }
    }

    public String writeNameBeforeMethodDesc() {
        return this == JSON_UTF16 ? "(I)[C" : "(I)[B";
    }

    public String bufType() {
        return this == JSON_UTF16 ? "[C" : "[B";
    }

    public String ioType() {
        switch (this) {
            case JSONB:
                return TYPE_JSONB_IO;
            case JSON_UTF8:
                return TYPE_JSON_UTF8_IO;
            default:
                return TYPE_JSON_UTF16_IO;
        }
    }

    public String fieldNameMethodName() {
        switch (this) {
            case JSONB:
                return "fieldNameJSONB";
            case JSON_UTF8:
                return "fieldNameUTF8Quote";
            default:
                return "fieldNameUTF16Quote";
        }
    }

    public String fieldNameMethodDesc() {
        return this == JSON_UTF16 ? "(J)[C" : "(J)[B";
    }

    public String fieldNameUnquote() {
        switch (this) {
            case JSONB:
                return "nameJSONB";
            case JSON_UTF8:
                return "nameWithColonUTF8Unquote";
            default:
                return "nameWithColonUTF16Unquote";
        }
    }
}
