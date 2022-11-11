package com.alibaba.fastjson;

/**
 * An enumeration that defines a few standard naming conventions for JSON field names.
 * @since 1.2.15
 */
public enum PropertyNamingStrategy {
    CamelCase,
    /**
     * Using this naming policy with FASTJSON will ensure that the first "letter" of the Java field name is capitalized when serialized to its JSON form.
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; SomeFieldName
     * _someFieldName ---&gt; _SomeFieldName
     */
    PascalCase,
    SnakeCase,
    UpperCase,
    /**
     * Using this naming policy with FASTJSON will ensure that the first "letter" of the Java field name is capitalized when serialized to its JSON form and the words will be separated by a space.
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; Some Field Name
     * _someFieldName ---&gt; _Some Field Name
     * @since 2.0.7
     */
    UpperCamelCaseWithSpaces,
    /**
     * Using this naming policy with FASTJSON will ensure that the first "letter" of the Java field name is capitalized when serialized to its JSON form and the words will be separated by an underscore (_).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; Some_Field_Name
     * _someFieldName ---&gt; _Some_Field_Name
     * @since 2.0.7
     */
    UpperCamelCaseWithUnderScores,
    /**
     * Using this naming policy with FASTJSON will ensure that the first "letter" of the Java field name is capitalized when serialized to its JSON form and the words will be separated by a dash (-).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; Some-Field-Name
     * _someFieldName ---&gt; _Some-Field-Name
     * @since 2.0.7
     */
    UpperCamelCaseWithDashes,
    /**
     * Using this naming policy with FASTJSON will ensure that the first "letter" of the Java field name is capitalized when serialized to its JSON form and the words will be separated by a dash (-).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; Some-Field-Name
     * _someFieldName ---&gt; _Some-Field-Name
     * @since 2.0.7
     */
    UpperCamelCaseWithDots,
    /**
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to a lower case field name where each word is separated by a dash (-).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; some-field-name
     * _someFieldName ---&gt; _some-field-name
     * aStringField ---&gt; a-string-field
     * aURL ---&gt; a-u-r-l
     * Using dashes in JavaScript is not recommended since dash is also used for a minus sign in expressions. This requires that a field named with dashes is always accessed as a quoted property like myobject['my-field']. Accessing it as an object field myobject.my-field will result in an unintended javascript expression.
     */
    KebabCase,
    /**
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to an upper case field name where each word is separated by an underscore (_).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; SOME_FIELD_NAME
     * _someFieldName ---&gt; _SOME_FIELD_NAME
     * aStringField ---&gt; A_STRING_FIELD
     * aURL ---&gt; A_U_R_L
     * @since 2.0.7
     */
    UpperCaseWithUnderScores,
    /**
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to a lower case field name where each word is separated by an underscore (_).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; some_field_name
     * _someFieldName ---&gt; _some_field_name
     * aStringField ---&gt; a_string_field
     * aURL ---&gt; a_u_r_l
     * @since 2.0.7
     */
    LowerCaseWithUnderScores,
    /**
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to a lower case field name where each word is separated by a dash (-).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; some-field-name
     * _someFieldName ---&gt; _some-field-name
     * aStringField ---&gt; a-string-field
     * aURL ---&gt; a-u-r-l
     * Using dashes in JavaScript is not recommended since dash is also used for a minus sign in expressions. This requires that a field named with dashes is always accessed as a quoted property like myobject['my-field']. Accessing it as an object field myobject.my-field will result in an unintended javascript expression.
     * @since 2.0.7
     */
    LowerCaseWithDashes,
    /**
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to a lower case field name where each word is separated by a dot (.).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; some.field.name
     * _someFieldName ---&gt; _some.field.name
     * aStringField ---&gt; a.string.field
     * aURL ---&gt; a.u.r.l
     * Using dots in JavaScript is not recommended since dot is also used for a member sign in expressions. This requires that a field named with dots is always accessed as a quoted property like myobject['my.field']. Accessing it as an object field myobject.my.field will result in an unintended javascript expression.
     * @since 2.0.7
     */
    LowerCaseWithDots,
    NoChange,
    NeverUseThisValueExceptDefaultValue;

    public String translate(String propertyName) {
        switch (this) {
            case SnakeCase: {
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < propertyName.length(); ++i) {
                    char ch = propertyName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        char ch_ucase = (char) (ch + 32);
                        if (i > 0) {
                            buf.append('_');
                        }
                        buf.append(ch_ucase);
                    } else {
                        buf.append(ch);
                    }
                }
                return buf.toString();
            }
            case KebabCase: {
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < propertyName.length(); ++i) {
                    char ch = propertyName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        char ch_ucase = (char) (ch + 32);
                        if (i > 0) {
                            buf.append('-');
                        }
                        buf.append(ch_ucase);
                    } else {
                        buf.append(ch);
                    }
                }
                return buf.toString();
            }
            case PascalCase: {
                char ch = propertyName.charAt(0);
                if (ch >= 'a' && ch <= 'z') {
                    char[] chars = propertyName.toCharArray();
                    chars[0] -= 32;
                    return new String(chars);
                }

                return propertyName;
            }
            case CamelCase: {
                char ch = propertyName.charAt(0);
                if (ch >= 'A' && ch <= 'Z') {
                    char[] chars = propertyName.toCharArray();
                    chars[0] += 32;
                    return new String(chars);
                }

                return propertyName;
            }
            case NoChange:
            case NeverUseThisValueExceptDefaultValue:
            default:
                return propertyName;
        }
    }
}
