package com.alibaba.fastjson2;

import java.util.ArrayList;
import java.util.List;

/**
 * JSONPObject is used to represent JSONP (JSON with Padding) data structure.
 *
 * <p>JSONP wraps JSON values in a JavaScript callback invocation. The callback name is
 * emitted as supplied, without validation or JSON string escaping.</p>
 * <details><summary>中文</summary>
 * JSONP 将 JSON 值包装为 JavaScript 回调调用；回调名称按原样输出，不进行校验或 JSON 字符串转义。
 * </details>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create a JSONP object
 * JSONPObject jsonp = new JSONPObject("callback");
 * jsonp.addParameter(JSONObject.of("id", 1, "name", "test"));
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
     * Sets the callback name, which is emitted verbatim during serialization.
     * <details><summary>中文</summary>设置回调名称；序列化时按原样输出。</details>
     *
     * @param function the function name to set
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     * Gets the mutable parameters list of this JSONP object.
     * Changes to this list affect subsequent serialization.
     * <details><summary>中文</summary>返回可修改的参数列表；修改会影响后续序列化结果。</details>
     *
     * @return the parameters list
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Appends a parameter, including null, to the ordered callback argument list.
     * <details><summary>中文</summary>将参数追加到有序的回调参数列表，允许 null。</details>
     *
     * @param parameter the parameter to add
     */
    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Serializes this object as a JavaScript callback invocation, rather than a JSON document.
     * <details><summary>中文</summary>序列化为 JavaScript 回调调用，而非独立的 JSON 文档。</details>
     *
     * @return the JSONP string representation
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
