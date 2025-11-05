package com.alibaba.fastjson2.filter;

/**
 * Filter interface for selective serialization based on property labels.
 * Works with the label attribute of JSONField annotation to control which properties are serialized.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * public class User {
 *     @JSONField(label = "public")
 *     private String name;
 *
 *     @JSONField(label = "private")
 *     private String email;
 *
 *     @JSONField(label = "admin")
 *     private String ssn;
 * }
 *
 * // Include only public fields
 * LabelFilter publicOnly = Labels.includes("public");
 * String json = JSON.toJSONString(user, publicOnly);
 *
 * // Exclude admin fields
 * LabelFilter noAdmin = Labels.excludes("admin");
 * String json2 = JSON.toJSONString(user, noAdmin);
 * }</pre>
 *
 * @see Labels
 */
public interface LabelFilter
        extends Filter {
    /**
     * Determines whether properties with the given label should be included.
     *
     * @param label the label to check
     * @return true to include properties with this label, false to exclude them
     */
    boolean apply(String label);
}
