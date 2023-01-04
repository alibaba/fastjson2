package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.core.FormatSchema;

import java.util.ArrayList;
import java.util.List;

public class CsvSchema
        extends FormatSchema {
    protected static final int ENCODING_FEATURE_USE_HEADER = 0x0001;
    protected static final int ENCODING_FEATURE_SKIP_FIRST_DATA_ROW = 0x0002;
    protected static final int ENCODING_FEATURE_ALLOW_COMMENTS = 0x0004;
    protected static final int ENCODING_FEATURE_REORDER_COLUMNS = 0x0008;
    protected static final int ENCODING_FEATURE_STRICT_HEADERS = 0x0010;

    private int quoteChar = '"';
    private char columnSeparator = ',';
    private char[] lineSeparator;
    private char[] nullValue;
    private char escapeChar;
    private String arrayElementSeparator;
    private int features;
    private List<Column> columns = new ArrayList<>();

    public CsvSchema() {
    }

    public static CsvSchema emptySchema() {
        return new CsvSchema();
    }

    public int size() {
        return columns.size();
    }

    public CsvSchema withHeader() {
        return this;
    }

    public String columnName(int index) {
        return columns.get(index).name;
    }

    public int getQuoteChar() {
        return quoteChar;
    }

    public char getColumnSeparator() {
        return columnSeparator;
    }

    public char[] getLineSeparator() {
        return lineSeparator;
    }

    public CsvSchema withLineSeparator(String sep) {
        return this;
    }

    public String getArrayElementSeparator() {
        return arrayElementSeparator;
    }

    public int getEscapeChar() {
        return escapeChar;
    }

    public char[] getNullValue() {
        return nullValue;
    }

    public boolean allowsComments() {
        return (features & ENCODING_FEATURE_ALLOW_COMMENTS) != 0;
    }

    public CsvSchema withoutQuoteChar() {
        throw new JSONException("TODO");
    }

    public boolean usesEscapeChar() { return escapeChar >= 0; }

    public enum ColumnType {
        STRING,
        NUMBER,
        BOOLEAN,
        ARRAY
    }

    public Builder rebuild() {
        return new Builder(this);
    }

    public static class Builder {
        CsvSchema src;

        public Builder() {
            src = new CsvSchema();
        }

        public Builder(CsvSchema src) {
            this.src = src;
        }

        public Builder setQuoteChar(char c) {
            src.quoteChar = c;
            return this;
        }

        public Builder setColumnSeparator(char c) {
            src.columnSeparator = c;
            return this;
        }

        public Builder setLineSeparator(char lf) {
            src.lineSeparator = new char[]{lf};
            return this;
        }

        public Builder setLineSeparator(String lf) {
            src.lineSeparator = lf.toCharArray();
            return this;
        }

        public Builder setEscapeChar(char c) {
            src.escapeChar = c;
            return this;
        }

        public Builder setNullValue(String nvl) {
            return setNullValue((nvl == null) ? null : nvl.toCharArray());
        }

        public Builder setNullValue(char[] nvl) {
            src.nullValue = nvl;
            return this;
        }

        public Builder setArrayElementSeparator(String separator) {
            if (separator == null) {
                separator = "";
            }
            src.arrayElementSeparator = separator;
            return this;
        }

        public Builder setAllowComments(boolean b) {
            if (b) {
                src.features |= ENCODING_FEATURE_ALLOW_COMMENTS;
            } else {
                src.features &= ~ENCODING_FEATURE_ALLOW_COMMENTS;
            }
            return this;
        }

        public Builder addColumn(Column c) {
            src.columns.add(c);
            return this;
        }

        public Builder disableQuoteChar() {
            src.quoteChar = -1;
            return this;
        }

        public CsvSchema build() {
            return src;
        }
    }

    public static class Column
            implements java.io.Serializable {
        int index;
        String name;
        ColumnType type;

        public Column(int index, String name, ColumnType type) {
            this.index = index;
            this.name = name;
            this.type = type;
        }
    }
}
