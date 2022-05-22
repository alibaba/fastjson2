package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Fastjson for Spring MVC View.
 * <p>
 * Compatible fastjson 1.2.x
 *
 * @author libinsong
 * @author VictorZeng
 * @see AbstractView
 * @since 2.0.2
 */

public class FastJsonJsonView
        extends AbstractView {
    /**
     * renderedAttributes
     */
    private Set<String> renderedAttributes;

    /**
     * disableCaching
     */
    private boolean disableCaching = true;

    /**
     * extractValueFromSingleKeyModel
     */
    private boolean extractValueFromSingleKeyModel;

    /**
     * with fastJson config
     */
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    /**
     * Set default param.
     */
    public FastJsonJsonView() {
        setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        setExposePathVariables(false);
    }

    /**
     * @return the fastJsonConfig.
     */
    public FastJsonConfig getFastJsonConfig() {
        return fastJsonConfig;
    }

    /**
     * @param fastJsonConfig the fastJsonConfig to set.
     */
    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }

    /**
     * Set renderedAttributes.
     *
     * @param renderedAttributes renderedAttributes
     */
    public void setRenderedAttributes(Set<String> renderedAttributes) {
        this.renderedAttributes = renderedAttributes;
    }

    /**
     * Check extractValueFromSingleKeyModel.
     *
     * @return extractValueFromSingleKeyModel
     */
    public boolean isExtractValueFromSingleKeyModel() {
        return extractValueFromSingleKeyModel;
    }

    /**
     * Set extractValueFromSingleKeyModel.
     */
    public void setExtractValueFromSingleKeyModel(boolean extractValueFromSingleKeyModel) {
        this.extractValueFromSingleKeyModel = extractValueFromSingleKeyModel;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, //
                                           HttpServletRequest request, //
                                           HttpServletResponse response) throws Exception {
        Object value = filterModel(model);

        ByteArrayOutputStream outnew = new ByteArrayOutputStream();

        int len = JSON.writeJSONString(outnew, value, fastJsonConfig.getSerializeFilters(), fastJsonConfig.getSerializerFeatures());

        if (fastJsonConfig.isWriteContentLength()) {
            // Write content length (determined via byte array).
            response.setContentLength(len);
        }

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        outnew.writeTo(out);
        outnew.close();
        out.flush();
    }

    @Override
    protected void prepareResponse(HttpServletRequest request, //
                                   HttpServletResponse response) {
        setResponseContentType(request, response);
        response.setCharacterEncoding(fastJsonConfig.getCharset().name());
        if (this.disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }

    /**
     * Disables caching of the generated JSON.
     * <p>
     * Default is {@code true}, which will prevent the client from caching the
     * generated JSON.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    /**
     * Whether to update the 'Content-Length' header of the response. When set
     * to {@code true}, the response is buffered in order to determine the
     * content length and set the 'Content-Length' header of the response.
     * <p>
     * The default setting is {@code false}.
     */
    public void setUpdateContentLength(boolean updateContentLength) {
        this.fastJsonConfig.setWriteContentLength(updateContentLength);
    }

    /**
     * Filters out undesired attributes from the given model. The return value
     * can be either another {@link Map}, or a single value object.
     * <p>
     * Default implementation removes {@link BindingResult} instances and
     * entries not included in the {@link #setRenderedAttributes(Set)
     * renderedAttributes} property.
     *
     * @param model the model, as passed on to {@link #renderMergedOutputModel}
     * @return the object to be rendered
     */
    protected Object filterModel(Map<String, Object> model) {
        Map<String, Object> result = new HashMap<String, Object>(model.size());
        Set<String> renderedAttributes = !CollectionUtils.isEmpty(this.renderedAttributes) ? //
                this.renderedAttributes //
                : model.keySet();

        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (!(entry.getValue() instanceof BindingResult)
                    && renderedAttributes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        if (extractValueFromSingleKeyModel) {
            if (result.size() == 1) {
                for (Map.Entry<String, Object> entry : result.entrySet()) {
                    return entry.getValue();
                }
            }
        }
        return result;
    }

    @Override
    protected void setResponseContentType(HttpServletRequest request, HttpServletResponse response) {
        super.setResponseContentType(request, response);
    }
}
