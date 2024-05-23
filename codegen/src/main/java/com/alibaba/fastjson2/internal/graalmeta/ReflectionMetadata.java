package com.alibaba.fastjson2.internal.graalmeta;

import java.util.Objects;

public class ReflectionMetadata {
    private TypeReachableMetadata[] condition;
    /**
     * Name of the class that should be registered for reflection
     */
    private String name;
    /**
     * List of methods that should be registered for the class declared in <name>
     */
    private MethodMetadata[] methods;
    /**
     * List of methods that are queried for the class declared in <name>
     */
    private MethodMetadata[] queriedMethods;
    /**
     * List of class fields that can be looked up, read, or modified for the class declared in <name>
     */
    private FieldMetadata[] fields;
    /**
     * Register classes which would be returned by the java.lang.Class#getDeclaredClasses call
     */
    private Boolean allDeclaredClasses;
    /**
     * Register methods which would be returned by the java.lang.Class#getDeclaredMethods call
     */
    private Boolean allDeclaredMethods;
    /**
     * Register fields which would be returned by the java.lang.Class#getDeclaredFields call
     */
    private Boolean allDeclaredFields;
    /**
     * Register constructors which would be returned by the java.lang.Class#getDeclaredConstructors call
     */
    private Boolean allDeclaredConstructors;
    /**
     * Register all public classes which would be returned by the java.lang.Class#getClasses call
     */
    private Boolean allPublicClasses;
    /**
     * Register all public methods which would be returned by the java.lang.Class#getMethods call
     */
    private Boolean allPublicMethods;
    /**
     * Register all public fields which would be returned by the java.lang.Class#getFields call
     */
    private Boolean allPublicFields;
    /**
     * Register all public constructors which would be returned by the java.lang.Class#getConstructors call
     */
    private Boolean allPublicConstructors;
    /**
     * Register record components which would be returned by the java.lang.Class#getRecordComponents call
     */
    private Boolean allRecordComponents;
    /**
     * Register permitted subclasses which would be returned by the java.lang.Class#getPermittedSubclasses call
     */
    private Boolean allPermittedSubclasses;
    /**
     * Register nest members which would be returned by the java.lang.Class#getNestMembers call
     */
    private Boolean allNestMembers;
    /**
     * Register signers which would be returned by the java.lang.Class#getSigners call
     */
    private Boolean allSigners;
    /**
     * Register methods which would be returned by the java.lang.Class#getDeclaredMethods call but only for lookup
     */
    private Boolean queryAllDeclaredMethods;
    /**
     * Register constructors which would be returned by the java.lang.Class#getDeclaredConstructors call but only for lookup
     */
    private Boolean queryAllDeclaredConstructors;
    /**
     * Register all public methods which would be returned by the java.lang.Class#getMethods call but only for lookup
     */
    private Boolean queryAllPublicMethods;
    /**
     * Register all public constructors which would be returned by the java.lang.Class#getConstructors call but only for lookup
     */
    private Boolean queryAllPublicConstructors;
    /**
     * Allow objects of this class to be instantiated with a call to jdk.internal.misc.Unsafe#allocateInstance
     */
    private Boolean unsafeAllocated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeReachableMetadata[] getCondition() {
        return condition;
    }

    public void setCondition(TypeReachableMetadata[] condition) {
        this.condition = condition;
    }

    public MethodMetadata[] getMethods() {
        return methods;
    }

    public void setMethods(MethodMetadata[] methods) {
        this.methods = methods;
    }

    public MethodMetadata[] getQueriedMethods() {
        return queriedMethods;
    }

    public void setQueriedMethods(MethodMetadata[] queriedMethods) {
        this.queriedMethods = queriedMethods;
    }

    public FieldMetadata[] getFields() {
        return fields;
    }

    public void setFields(FieldMetadata[] fields) {
        this.fields = fields;
    }

    public Boolean getAllDeclaredClasses() {
        return allDeclaredClasses;
    }

    public void setAllDeclaredClasses(Boolean allDeclaredClasses) {
        this.allDeclaredClasses = allDeclaredClasses;
    }

    public Boolean getAllDeclaredMethods() {
        return allDeclaredMethods;
    }

    public void setAllDeclaredMethods(Boolean allDeclaredMethods) {
        this.allDeclaredMethods = allDeclaredMethods;
    }

    public Boolean getAllDeclaredFields() {
        return allDeclaredFields;
    }

    public void setAllDeclaredFields(Boolean allDeclaredFields) {
        this.allDeclaredFields = allDeclaredFields;
    }

    public Boolean getAllDeclaredConstructors() {
        return allDeclaredConstructors;
    }

    public void setAllDeclaredConstructors(Boolean allDeclaredConstructors) {
        this.allDeclaredConstructors = allDeclaredConstructors;
    }

    public Boolean getAllPublicClasses() {
        return allPublicClasses;
    }

    public void setAllPublicClasses(Boolean allPublicClasses) {
        this.allPublicClasses = allPublicClasses;
    }

    public Boolean getAllPublicMethods() {
        return allPublicMethods;
    }

    public void setAllPublicMethods(Boolean allPublicMethods) {
        this.allPublicMethods = allPublicMethods;
    }

    public Boolean getAllPublicFields() {
        return allPublicFields;
    }

    public void setAllPublicFields(Boolean allPublicFields) {
        this.allPublicFields = allPublicFields;
    }

    public Boolean getAllPublicConstructors() {
        return allPublicConstructors;
    }

    public void setAllPublicConstructors(Boolean allPublicConstructors) {
        this.allPublicConstructors = allPublicConstructors;
    }

    public Boolean getAllRecordComponents() {
        return allRecordComponents;
    }

    public void setAllRecordComponents(Boolean allRecordComponents) {
        this.allRecordComponents = allRecordComponents;
    }

    public Boolean getAllPermittedSubclasses() {
        return allPermittedSubclasses;
    }

    public void setAllPermittedSubclasses(Boolean allPermittedSubclasses) {
        this.allPermittedSubclasses = allPermittedSubclasses;
    }

    public Boolean getAllNestMembers() {
        return allNestMembers;
    }

    public void setAllNestMembers(Boolean allNestMembers) {
        this.allNestMembers = allNestMembers;
    }

    public Boolean getAllSigners() {
        return allSigners;
    }

    public void setAllSigners(Boolean allSigners) {
        this.allSigners = allSigners;
    }

    public Boolean getQueryAllDeclaredMethods() {
        return queryAllDeclaredMethods;
    }

    public void setQueryAllDeclaredMethods(Boolean queryAllDeclaredMethods) {
        this.queryAllDeclaredMethods = queryAllDeclaredMethods;
    }

    public Boolean getQueryAllDeclaredConstructors() {
        return queryAllDeclaredConstructors;
    }

    public void setQueryAllDeclaredConstructors(Boolean queryAllDeclaredConstructors) {
        this.queryAllDeclaredConstructors = queryAllDeclaredConstructors;
    }

    public Boolean getQueryAllPublicMethods() {
        return queryAllPublicMethods;
    }

    public void setQueryAllPublicMethods(Boolean queryAllPublicMethods) {
        this.queryAllPublicMethods = queryAllPublicMethods;
    }

    public Boolean getQueryAllPublicConstructors() {
        return queryAllPublicConstructors;
    }

    public void setQueryAllPublicConstructors(Boolean queryAllPublicConstructors) {
        this.queryAllPublicConstructors = queryAllPublicConstructors;
    }

    public Boolean getUnsafeAllocated() {
        return unsafeAllocated;
    }

    public void setUnsafeAllocated(Boolean unsafeAllocated) {
        this.unsafeAllocated = unsafeAllocated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReflectionMetadata that = (ReflectionMetadata) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
