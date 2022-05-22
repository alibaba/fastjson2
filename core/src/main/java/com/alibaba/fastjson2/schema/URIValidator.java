package com.alibaba.fastjson2.schema;

import java.net.URI;
import java.net.URISyntaxException;

final class URIValidator
        implements FormatValidator {
    static final URIValidator INSTANCE = new URIValidator();

    @Override
    public boolean isValid(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            new URI(url);
            return true;
        } catch (URISyntaxException ignored) {
            return false;
        }
    }
}
