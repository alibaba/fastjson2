package com.alibaba.fastjson2.schema;

public interface ValidateResult {
    boolean isSuccess();

    String getMessage();

    ValidateResult getCause();

    final class MinimumFail implements ValidateResult {
        final Object minimum;
        final Object value;
        final boolean exclusive;
        private String message;

        public MinimumFail(Object minimum, Object value, boolean exclusive) {
            this.minimum = minimum;
            this.exclusive = exclusive;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = ((exclusive ? "exclusiveMinimum not match, expect >= " : "minimum not match, expect >= ") + minimum + ", but " + value);
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class FormatFail implements ValidateResult {
        final String format;
        final String value;
        private String message;

        public FormatFail(String format, String value) {
            this.format = format;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "format not match, expect " + format + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MaxLengthFail implements ValidateResult {
        final int maxLength;
        final int size;
        private String message;

        public MaxLengthFail(int maxLength, int size) {
            this.maxLength = maxLength;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "maxLength not match, expect " + maxLength + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MinLengthFail implements ValidateResult {
        final int minLength;
        final int size;
        private String message;

        public MinLengthFail(int minLength, int size) {
            this.minLength = minLength;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "minLength not match, expect " + minLength + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MinPropertiesFail implements ValidateResult {
        final int minProperties;
        final int size;
        private String message;

        public MinPropertiesFail(int minProperties, int size) {
            this.minProperties = minProperties;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "minProperties not match, expect " + minProperties + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class PatternFail implements ValidateResult {
        final String pattern;
        final String value;
        private String message;

        public PatternFail(String pattern, String value) {
            this.pattern = pattern;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "pattern not match, expect " + pattern + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class TypeNotMatchFail implements ValidateResult {
        final JSONSchema.Type expectType;
        final Class inputType;
        private String message;

        public TypeNotMatchFail(JSONSchema.Type expectType, Class inputType) {
            this.expectType = expectType;
            this.inputType = inputType;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "type " + expectType + " not match : " + inputType.getName();
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class Success implements ValidateResult {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            return "success";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class SuccessNull implements ValidateResult {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            return "success";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class SuccessTypeNotMatch implements ValidateResult {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            return "success";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MaximumFail implements ValidateResult {
        final Object maximum;
        final Object value;
        final boolean exclusive;
        private String message;


        public MaximumFail(Object maximum, Object value, boolean exclusive) {
            this.maximum = maximum;
            this.value = value;
            this.exclusive = exclusive;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = ((exclusive ? "exclusiveMaximum not match, expect >= " : "maximum not match, expect >= ") + maximum + ", but " + value);
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MultipleOfFail implements ValidateResult {
        final Number multipleOf;
        final Number value;
        private String message;

        public MultipleOfFail(Number multipleOf, Number value) {
            this.multipleOf = multipleOf;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "multipleOf not match, expect multipleOf " + multipleOf + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MaxPropertiesFail implements ValidateResult {
        final int maxProperties;
        final int size;
        private String message;

        public MaxPropertiesFail(int maxProperties, int size) {
            this.maxProperties = maxProperties;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "minProperties not match, expect " + maxProperties + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MinContainsFail implements ValidateResult {
        final int minContains;
        final int size;
        private String message;

        public MinContainsFail(int minContains, int size) {
            this.minContains = minContains;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "maxContains not match, expect " + minContains + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class MaxContainsFail implements ValidateResult {
        final int maxContains;
        final int size;
        private String message;

        public MaxContainsFail(int maxContains, int size) {
            this.maxContains = maxContains;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "maxContains not match, expect " + maxContains + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class AdditionalItemsFail implements ValidateResult {
        final int maxSize;
        final int size;
        private String message;

        public AdditionalItemsFail(int maxSize, int size) {
            this.maxSize = maxSize;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "additional items not match, max size " + maxSize + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class ConstFail implements ValidateResult {
        final Object constValue;
        final Object value;
        private String message;

        public ConstFail(Object constValue, Object value) {
            this.constValue = constValue;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "const not match, expect " + constValue + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class PropertyPatternFail implements ValidateResult {
        final String propertyPattern;
        final Object property;
        private String message;

        public PropertyPatternFail(String propertyPattern, Object property) {
            this.propertyPattern = propertyPattern;
            this.property = property;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "propertyNames pattern not match, expect '" + propertyPattern + ", but " + property;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class AdditionalPropertiesFail implements ValidateResult {
        final Object property;
        private String message;

        public AdditionalPropertiesFail(Object property) {
            this.property = property;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "add additionalProperties '" + property + "'";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class RequiredFail implements ValidateResult {
        final String property;
        private String message;

        public RequiredFail(String property) {
            this.property = property;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "required property '" + property + "'";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class DependentRequiredPropertyFail implements ValidateResult {
        final String property;
        final String dependentRequiredProperty;
        private String message;

        public DependentRequiredPropertyFail(String property, String dependentRequiredProperty) {
            this.property = property;
            this.dependentRequiredProperty = dependentRequiredProperty;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "property '" + property + "', dependentRequired property '" + dependentRequiredProperty + "'";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final class Fail implements ValidateResult {
        final String message;
        final ValidateResult cause;

        public Fail(String message) {
            this.message = message;
            this.cause = null;
        }

        public Fail(String message, ValidateResult cause) {
            this.message = message;
            this.cause = cause;
        }

        public String getMessage() {
            return message;
        }

        public ValidateResult getCause() {
            return cause;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}
