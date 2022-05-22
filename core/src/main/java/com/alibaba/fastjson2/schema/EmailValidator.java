package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.util.DomainValidator;
import com.alibaba.fastjson2.util.InetAddressValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class EmailValidator implements FormatValidator {
    private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
    private static final String VALID_CHARS = "(\\\\.)|[^\\s" + SPECIAL_CHARS + "]";
    private static final String QUOTED_USER = "(\"[^\"]*\")";
    private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

    private static final String EMAIL_REGEX = "^\\s*?(.+)@(.+?)\\s*$";
    private static final String IP_DOMAIN_REGEX = "^\\[(.*)\\]$";
    private static final String USER_REGEX = "^\\s*" + WORD + "(\\." + WORD + ")*$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern IP_DOMAIN_PATTERN = Pattern.compile(IP_DOMAIN_REGEX);
    private static final Pattern USER_PATTERN = Pattern.compile(USER_REGEX);

    final static EmailValidator INSTANCE = new EmailValidator();

    @Override
    public boolean isValid(String email) {
        if (email == null) {
            return false;
        }

        if (email.endsWith(".")) { // check this first - it's cheap!
            return false;
        }

        // Check the whole email address structure
        Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
        if (!emailMatcher.matches()) {
            return false;
        }

        if (!isValidUser(emailMatcher.group(1))) {
            return false;
        }

        if (!isValidDomain(emailMatcher.group(2))) {
            return false;
        }

        return true;
    }

    protected static boolean isValidDomain(String domain) {
        // see if domain is an IP address in brackets
        Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

        if (ipDomainMatcher.matches()) {
            InetAddressValidator inetAddressValidator =
                    InetAddressValidator.getInstance();
            return inetAddressValidator.isValid(ipDomainMatcher.group(1));
        }
        // Domain is symbolic name
        return DomainValidator.isValid(domain) || DomainValidator.isValidTld(domain);
    }

    protected static boolean isValidUser(String user) {
        if (user == null || user.length() > 64) {
            return false;
        }

        return USER_PATTERN.matcher(user).matches();
    }
}
