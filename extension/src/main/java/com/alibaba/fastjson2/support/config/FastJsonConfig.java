package com.alibaba.fastjson2.support.config;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;
import com.alibaba.fastjson2.filter.Filter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Configuration for FastJson.
 *
 * @author Victor.Zxy
 * @see JSONReader.Feature
 * @see JSONWriter.Feature
 * @see Filter
 * @since 2.0.2
 */
public class FastJsonConfig {
    /**
     * default charset
     */
    private Charset charset;

    /**
     * format date type
     */
    private String dateFormat;

    /**
     * JSONReader Features
     */
    private JSONReader.Feature[] readerFeatures;

    /**
     * JSONWriter Features
     */
    private JSONWriter.Feature[] writerFeatures;

    /**
     * JSONReader Filters
     */
    private Filter[] readerFilters;

    /**
     * JSONWriter Filters
     */
    private Filter[] writerFilters;

    /**
     * The Write content length.
     */
    private boolean writeContentLength;

    /**
     * JSONB flag.
     */
    private boolean jsonb;

    /**
     * JSONB symbol table.
     */
    private SymbolTable symbolTable;

    /** internal cache for JSONReader.Context, avoid repeatedly constructing new objects */
    private transient JSONReader.Context readerContext;
    /** internal cache for JSONWriter.Context, avoid repeatedly constructing new objects */
    private transient JSONWriter.Context writerContext;

    /**
     * init param.
     */
    public FastJsonConfig() {
        this.dateFormat = "yyyy-MM-dd HH:mm:ss";
        this.charset = StandardCharsets.UTF_8;
        this.readerFeatures = new JSONReader.Feature[0];
        this.writerFeatures = new JSONWriter.Feature[] {
                JSONWriter.Feature.WriteByteArrayAsBase64,
                JSONWriter.Feature.BrowserSecure
        };
        this.readerFilters = new Filter[0];
        this.writerFilters = new Filter[0];
        this.writeContentLength = true;
    }

    /**
     * Gets charset.
     *
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets charset.
     *
     * @param charset the charset
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
        this.clearContext();
    }

    /**
     * Gets date format.
     *
     * @return the date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets date format.
     *
     * @param dateFormat the date format
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        this.clearContext();
    }

    /**
     * Get reader features json reader . feature [ ].
     *
     * @return the json reader . feature [ ]
     */
    public JSONReader.Feature[] getReaderFeatures() {
        return readerFeatures;
    }

    /**
     * Sets reader features.
     *
     * @param readerFeatures the reader features
     */
    public void setReaderFeatures(JSONReader.Feature... readerFeatures) {
        this.readerFeatures = readerFeatures;
        this.readerContext = null;
    }

    /**
     * Get writer features json writer . feature [ ].
     *
     * @return the json writer . feature [ ]
     */
    public JSONWriter.Feature[] getWriterFeatures() {
        return writerFeatures;
    }

    /**
     * Sets writer features.
     *
     * @param writerFeatures the writer features
     */
    public void setWriterFeatures(JSONWriter.Feature... writerFeatures) {
        this.writerFeatures = writerFeatures;
        this.writerContext = null;
    }

    /**
     * Get reader filters filter [ ].
     *
     * @return the filter [ ]
     */
    public Filter[] getReaderFilters() {
        return readerFilters;
    }

    /**
     * Sets reader filters.
     *
     * @param readerFilters the reader filters
     */
    public void setReaderFilters(Filter... readerFilters) {
        this.readerFilters = readerFilters;
        this.readerContext = null;
    }

    /**
     * Get writer filters filter [ ].
     *
     * @return the filter [ ]
     */
    public Filter[] getWriterFilters() {
        return writerFilters;
    }

    /**
     * Sets writer filters.
     *
     * @param writerFilters the writer filters
     */
    public void setWriterFilters(Filter... writerFilters) {
        this.writerFilters = writerFilters;
        this.writerContext = null;
    }

    /**
     * Is write content length boolean.
     *
     * @return the boolean
     */
    public boolean isWriteContentLength() {
        return writeContentLength;
    }

    /**
     * Sets write content length.
     *
     * @param writeContentLength the write content length
     */
    public void setWriteContentLength(boolean writeContentLength) {
        this.writeContentLength = writeContentLength;
    }

    /**
     * Is jsonb boolean.
     *
     * @return the boolean
     * @since 2.0.5
     */
    public boolean isJSONB() {
        return jsonb;
    }

    /**
     * Sets jsonb flag.
     *
     * @param jsonb the jsonb
     * @since 2.0.5
     */
    public void setJSONB(boolean jsonb) {
        this.jsonb = jsonb;
        this.clearContext();
    }

    /**
     * Gets symbol table.
     *
     * @return the symbol table
     * @since 2.0.5
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Sets symbol table.
     *
     * @param names the names
     * @since 2.0.5
     */
    public void setSymbolTable(String... names) {
        this.symbolTable = JSONB.symbolTable(names);
        this.clearContext();
    }

    /**
     * Clear internal caches of JSONReader.Context and JSONWriter.Context
     */
    public void clearContext() {
        readerContext = null;
        writerContext = null;
    }

    public JSONReader.Context readerContext() {
        JSONReader.Context context = readerContext;
        if (context == null) { // Concurrency may occur, but it will not cause any problems
            context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider(), jsonb ? symbolTable : null, readerFilters, readerFeatures);
            context.setDateFormat(dateFormat);
            this.readerContext = context;
        }
        return context;
    }

    public JSONWriter.Context writerContext() {
        JSONWriter.Context context = writerContext;
        if (context == null) {
            context = new JSONWriter.Context(dateFormat, writerFeatures);
            if (dateFormat != null && !dateFormat.isEmpty()) {
                context.setDateFormat(dateFormat);
            }
            // symbolTable is only in JSONWriter class
            if (writerFilters != null && writerFilters.length > 0) {
                context.configFilter(writerFilters);
            }
            this.writerContext = context;
        }
        return context;
    }
}
