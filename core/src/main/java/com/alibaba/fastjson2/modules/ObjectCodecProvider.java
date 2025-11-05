package com.alibaba.fastjson2.modules;

/**
 * Provider interface for customizing codec behavior, particularly for mix-in annotations.
 * Mix-ins allow applying serialization/deserialization annotations from one class to another
 * without modifying the target class.
 *
 * <p>This is commonly used to add JSON annotations to third-party classes that cannot be modified.
 *
 * @see ObjectReaderModule
 * @see ObjectWriterModule
 * @since 2.0.0
 */
public interface ObjectCodecProvider {
    /**
     * Returns the mix-in class for the specified target class, if any.
     * The mix-in class's annotations will be applied to the target class during
     * serialization and deserialization.
     *
     * <p><b>Usage Example:</b></p>
     * <pre>{@code
     * // Define a mix-in class with annotations
     * public abstract class ThirdPartyClassMixIn {
     *     @JSONField(name = "custom_name")
     *     public abstract String getValue();
     * }
     *
     * // Register the mix-in
     * public Class getMixIn(Class target) {
     *     if (target == ThirdPartyClass.class) {
     *         return ThirdPartyClassMixIn.class;
     *     }
     *     return null;
     * }
     * }</pre>
     *
     * @param target the target class to find a mix-in for
     * @return the mix-in class, or null if no mix-in is registered for the target
     */
    Class getMixIn(Class target);
}
