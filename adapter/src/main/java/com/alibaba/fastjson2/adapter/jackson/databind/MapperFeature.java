package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.databind.cfg.ConfigFeature;

public enum MapperFeature implements ConfigFeature {
    USE_ANNOTATIONS(true),
    USE_GETTERS_AS_SETTERS(true),
    PROPAGATE_TRANSIENT_MARKER(false),
    AUTO_DETECT_CREATORS(true),
    AUTO_DETECT_FIELDS(true),

    /**
     * Feature that determines whether regular "getter" methods are
     * automatically detected based on standard Bean naming convention
     * or not. If yes, then all public zero-argument methods that
     * start with prefix "get"
     * are considered as getters.
     * If disabled, only methods explicitly  annotated are considered getters.
     * <p>
     * Note that since version 1.3, this does <b>NOT</b> include
     * "is getters" (see {@link #AUTO_DETECT_IS_GETTERS} for details)
     * <p>
     * Note that this feature has lower precedence than per-class
     * annotations, and is only used if there isn't more granular
     * configuration available.
     * <p>
     * Feature is enabled by default.
     */
    AUTO_DETECT_GETTERS(true),

    /**
     * Feature that determines whether "is getter" methods are
     * automatically detected based on standard Bean naming convention
     * or not. If yes, then all public zero-argument methods that
     * start with prefix "is", and whose return type is boolean
     * are considered as "is getters".
     * If disabled, only methods explicitly annotated are considered getters.
     * <p>
     * Note that this feature has lower precedence than per-class
     * annotations, and is only used if there isn't more granular
     * configuration available.
     * <p>
     * Feature is enabled by default.
     */
    AUTO_DETECT_IS_GETTERS(true),

    /**
     * Feature that determines whether "setter" methods are
     * automatically detected based on standard Bean naming convention
     * or not. If yes, then all public one-argument methods that
     * start with prefix "set"
     * are considered setters. If disabled, only methods explicitly
     * annotated are considered setters.
     * <p>
     * Note that this feature has lower precedence than per-class
     * annotations, and is only used if there isn't more granular
     * configuration available.
     * <p>
     * Feature is enabled by default.
     */
    AUTO_DETECT_SETTERS(true),

    /**
     * Feature that determines whether getters (getter methods)
     * can be auto-detected if there is no matching mutator (setter,
     * constructor parameter or field) or not: if set to true,
     * only getters that match a mutator are auto-discovered; if
     * false, all auto-detectable getters can be discovered.
     * <p>
     * Feature is disabled by default.
     */
    REQUIRE_SETTERS_FOR_GETTERS(false),

    /**
     * Feature that determines whether member fields declared as 'final' may
     * be auto-detected to be used mutators (used to change value of the logical
     * property) or not. If enabled, 'final' access modifier has no effect, and
     * such fields may be detected according to usual visibility and inference
     * rules; if disabled, such fields are NOT used as mutators except if
     * explicitly annotated for such use.
     * <p>
     * Feature is enabled by default, for backwards compatibility reasons.
     *
     * @since 2.2
     */
    ALLOW_FINAL_FIELDS_AS_MUTATORS(true),

    /**
     * Feature that determines whether member mutators (fields and
     * setters) may be "pulled in" even if they are not visible,
     * as long as there is a visible accessor (getter or field) with same name.
     * For example: field "value" may be inferred as mutator,
     * if there is visible or explicitly marked getter "getValue()".
     * If enabled, inferring is enabled; otherwise (disabled) only visible and
     * explicitly annotated accessors are ever used.
     * <p>
     * Note that 'getters' are never inferred and need to be either visible (including
     * bean-style naming) or explicitly annotated.
     * <p>
     * Feature is enabled by default.
     *
     * @since 2.2
     */
    INFER_PROPERTY_MUTATORS(true),
    INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES(true),
    ALLOW_VOID_VALUED_PROPERTIES(false),
    CAN_OVERRIDE_ACCESS_MODIFIERS(true),
    OVERRIDE_PUBLIC_ACCESS_MODIFIERS(true),

    USE_STATIC_TYPING(false),
    USE_BASE_TYPE_AS_DEFAULT_IMPL(false),
    INFER_BUILDER_TYPE_BINDINGS(true),
    DEFAULT_VIEW_INCLUSION(true),
    SORT_PROPERTIES_ALPHABETICALLY(false),
    SORT_CREATOR_PROPERTIES_FIRST(true),
    ACCEPT_CASE_INSENSITIVE_PROPERTIES(false),
    ACCEPT_CASE_INSENSITIVE_ENUMS(false),
    ACCEPT_CASE_INSENSITIVE_VALUES(false),

    USE_WRAPPER_NAME_AS_PROPERTY_NAME(false),
    USE_STD_BEAN_NAMING(false),
    ALLOW_EXPLICIT_PROPERTY_RENAMING(false),
    ALLOW_COERCION_OF_SCALARS(true),

    /*
    /******************************************************
    /* Other features
    /******************************************************
     */

    IGNORE_DUPLICATE_MODULE_REGISTRATIONS(true),
    IGNORE_MERGE_FOR_UNMERGEABLE(true),

    BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES(false),
    APPLY_DEFAULT_VALUES(true);

    private final boolean defaultState;
    private final long mask;

    // @since 2.13
    public static long collectLongDefaults() {
        long flags = 0;
        for (MapperFeature value : MapperFeature.values()) {
            if (value.enabledByDefault()) {
                flags |= value.getLongMask();
            }
        }
        return flags;
    }

    private MapperFeature(boolean defaultState) {
        this.defaultState = defaultState;
        mask = (1L << ordinal());
    }

    @Override
    public boolean enabledByDefault() {
        return defaultState;
    }

    @Override
    @Deprecated // 2.13
    public int getMask() {
        // 25-Feb-2021, tatu: Not 100% sure what to do here; should not be
        //     called any more
        return (int) mask;
    }

    // @since 2.13
    public long getLongMask() {
        return mask;
    }

    @Override
    @Deprecated
    public boolean enabledIn(int flags) {
        return (flags & mask) != 0;
    }

    // @since 2.13
    public boolean enabledIn(long flags) {
        return (flags & mask) != 0;
    }
}
