package com.alibaba.fastjson2;

import java.util.ArrayList;
import java.util.List;

/**
 * JSONPObject is used to represent JSONP (JSON with Padding) data structure.
 *
 * <p>JSONP is a technique for safely requesting data from another domain.
 * It wraps JSON data in a function call to bypass the same-origin policy
 * that prevents direct access to resources from different domains.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create a JSONP object
 * JSONPObject jsonp = new JSONPObject("callback");
 * jsonp.addParameter(new JSONObject().fluentPut("id", 1).fluentPut("name", "test"));
 *
 * // Serialize to JSONP string
 * String jsonpString = jsonp.toString(); // "callback({\"id\":1,\"name\":\"test\"})"
 *
 * // Parse from JSONP string
 * JSONPObject parsed = JSON.parseObject(jsonpString, JSONPObject.class);
 * }</pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/JSONP">JSONP Wikipedia</a>
 */
public class JSONPObject {
    /**
     * The function name for JSONP callback
     */
    private String function;

    /**
     * The parameters for JSONP function call
     */
    private final List<Object> parameters = new ArrayList<>();

    /**
     * Default constructor for JSONPObject
     */
    public JSONPObject() {
    }

    /**
     * Constructor with function name
     *
     * @param function the JSONP callback function name
     */
    public JSONPObject(String function) {
        this.function = function;
    }

    /**
     * Gets the function name of this JSONP object
     *
     * @return the function name
     */
    public String getFunction() {
        return function;
    }

    /**
     * Sets the function name of this JSONP object
     *
     * @param function the function name to set
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     * Gets the parameters list of this JSONP object
     *
     * @return the parameters list
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Adds a parameter to this JSONP object
     *
     * @param parameter the parameter to add
     */
    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Serializes this JSONP object to JSONP string format
     *
     * @return the JSONP string representation
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
