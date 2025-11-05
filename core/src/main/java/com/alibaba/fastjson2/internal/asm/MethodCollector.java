package com.alibaba.fastjson2.internal.asm;

/**
 * Collects local variable information from method bytecode to extract parameter names.
 * This class is used during class file parsing to gather debug information about method parameters.
 *
 * @see TypeCollector
 * @see ClassReader
 */
public class MethodCollector {
    private final int paramCount;

    private final int ignoreCount;

    private int currentParameter;

    private final StringBuilder result;

    protected boolean debugInfoPresent;

    protected MethodCollector(int ignoreCount, int paramCount) {
        this.ignoreCount = ignoreCount;
        this.paramCount = paramCount;
        this.result = new StringBuilder();
        this.currentParameter = 0;
        // if there are 0 parameters, there is no need for debug info
        this.debugInfoPresent = paramCount == 0;
    }

    /**
     * Visits a local variable entry in the method's LocalVariableTable.
     * Collects parameter names from the debug information.
     *
     * @param name the name of the local variable from debug info
     * @param index the local variable index in the method's local variable array
     */
    protected void visitLocalVariable(String name, int index) {
        if (index >= ignoreCount && index < ignoreCount + paramCount) {
            if (!("arg" + currentParameter).equals(name)) {
                debugInfoPresent = true;
            }
            result.append(',');
            result.append(name);
            currentParameter++;
        }
    }

    /**
     * Returns the collected parameter names as a comma-separated string.
     *
     * @return comma-separated parameter names, or empty string if no parameters
     */
    protected String getResult() {
        return result.length() != 0 ? result.substring(1) : "";
    }
}
