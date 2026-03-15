package com.alibaba.fastjson3.filter;

/**
 * Filter for label-based field visibility (similar to Jackson's @JsonView).
 * Fields annotated with {@code @JSONField(label = "xxx")} are only serialized
 * when a matching LabelFilter returns true for that label.
 *
 * <pre>
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .addLabelFilter(label -> "admin".equals(label))
 *     .build();
 * </pre>
 */
@FunctionalInterface
public interface LabelFilter {
    /**
     * @param label the label string from @JSONField(label=)
     * @return true to include the field, false to exclude
     */
    boolean apply(String label);
}
