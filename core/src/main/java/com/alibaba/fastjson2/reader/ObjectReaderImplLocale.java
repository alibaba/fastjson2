package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.Locale;

class ObjectReaderImplLocale
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplLocale INSTANCE = new ObjectReaderImplLocale();

    ObjectReaderImplLocale() {
        super(Locale.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readLocale(jsonReader);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readLocale(jsonReader);
    }

    private Locale readLocale(JSONReader jsonReader) {
        String strVal = jsonReader.readString();
        if (strVal == null) {
            return null;
        }
        if (strVal.isEmpty()) {
            return Locale.ROOT;
        }

        int extensionIndex = strVal.indexOf("_#");
        if (extensionIndex != -1) {
            String base = strVal.substring(0, extensionIndex);
            String suffix = strVal.substring(extensionIndex + 2);
            String[] baseItems = base.split("_", -1);
            String[] suffixItems = suffix.split("_", 2);

            String script = null;
            String extensions;
            if (isScript(suffixItems[0])) {
                script = suffixItems[0];
                extensions = suffixItems.length == 2 ? suffixItems[1] : null;
            } else {
                extensions = suffix;
            }

            StringBuilder languageTag = new StringBuilder(baseItems[0]);
            if (script != null) {
                languageTag.append('-').append(script);
            }
            if (baseItems.length > 1 && !baseItems[1].isEmpty()) {
                languageTag.append('-').append(baseItems[1]);
            }
            for (int i = 2; i < baseItems.length; i++) {
                if (!baseItems[i].isEmpty()) {
                    languageTag.append('-').append(baseItems[i]);
                }
            }
            if (extensions != null && !extensions.isEmpty()) {
                languageTag.append('-').append(extensions.replace('_', '-'));
            }
            return Locale.forLanguageTag(languageTag.toString());
        }

        String[] items = strVal.split("_");
        if (items.length == 1) {
            return new Locale(items[0]);
        }
        if (items.length == 2) {
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }

    private boolean isScript(String value) {
        if (value.length() != 4) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isLetter(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
