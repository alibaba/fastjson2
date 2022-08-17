package com.alibaba.fastjson2;

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
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to an upper case field name where each word is separated by a dash (-).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; SOME-FIELD-NAME
     * _someFieldName ---&gt; _SOME-FIELD-NAME
     * aStringField ---&gt; A-STRING-FIELD
     * aURL ---&gt; A-U-R-L
     * @since 2.0.8
     */
    UpperCaseWithDashes,
    /**
     * Using this naming policy with FASTJSON will modify the Java Field name from its camel cased form to an upper case field name where each word is separated by a dot (.).
     * Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":
     * someFieldName ---&gt; SOME.FIELD.NAME
     * _someFieldName ---&gt; _SOME.FIELD.NAME
     * aStringField ---&gt; A.STRING.FIELD
     * aURL ---&gt; A.U.R.L
     * @since 2.0.8
     */
    UpperCaseWithDots,
    LowerCase,
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
    NeverUseThisValueExceptDefaultValue;

    public static String snakeToCamel(String name) {
        if (name == null || name.indexOf('_') == -1) {
            return name;
        }

        int underscoreCount = 0;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_') {
                underscoreCount++;
            }
        }

        char[] chars = new char[name.length() - underscoreCount];
        for (int i = 0, j = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_') {
                continue;
            }
            if (i > 0 && name.charAt(i - 1) == '_') {
                if (ch >= 'a' && ch <= 'z') {
                    ch -= 32;
                }
            }
            chars[j++] = ch;
        }

        return new String(chars);
    }
}
