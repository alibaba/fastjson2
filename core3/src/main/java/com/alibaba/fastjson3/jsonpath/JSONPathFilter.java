package com.alibaba.fastjson3.jsonpath;

import java.util.Map;

/**
 * Filter expression for JSONPath predicates: {@code [?@.price < 10]}.
 * Supports comparison operators, logical operators, and existence tests.
 */
public sealed interface JSONPathFilter {
    /**
     * Test if the given node matches this filter.
     *
     * @param current the current node (@)
     * @param root    the root node ($)
     * @return true if the node matches
     */
    boolean test(Object current, Object root);

    // ==================== Comparison: @.price < 10 ====================

    record ComparisonFilter(PathExpression left, Operator op, ValueExpression right) implements JSONPathFilter {
        @Override
        public boolean test(Object current, Object root) {
            Object lval = left.resolve(current, root);
            Object rval = right.resolve(current, root);
            return op.compare(lval, rval);
        }
    }

    // ==================== Existence: @.isbn ====================

    record ExistsFilter(PathExpression path) implements JSONPathFilter {
        @Override
        public boolean test(Object current, Object root) {
            return path.exists(current, root);
        }
    }

    // ==================== Logical AND: expr1 && expr2 ====================

    record AndFilter(JSONPathFilter left, JSONPathFilter right) implements JSONPathFilter {
        @Override
        public boolean test(Object current, Object root) {
            return left.test(current, root) && right.test(current, root);
        }
    }

    // ==================== Logical OR: expr1 || expr2 ====================

    record OrFilter(JSONPathFilter left, JSONPathFilter right) implements JSONPathFilter {
        @Override
        public boolean test(Object current, Object root) {
            return left.test(current, root) || right.test(current, root);
        }
    }

    // ==================== Logical NOT: !expr ====================

    record NotFilter(JSONPathFilter inner) implements JSONPathFilter {
        @Override
        public boolean test(Object current, Object root) {
            return !inner.test(current, root);
        }
    }

    // ==================== Path expression: @.name, @.a.b, $ ====================

    record PathExpression(boolean isRoot, String[] names) {
        Object resolve(Object current, Object root) {
            Object node = isRoot ? root : current;
            for (String name : names) {
                if (node instanceof Map<?, ?> map) {
                    node = map.get(name);
                } else {
                    return null;
                }
            }
            return node;
        }

        boolean exists(Object current, Object root) {
            Object node = isRoot ? root : current;
            for (int i = 0; i < names.length; i++) {
                if (node instanceof Map<?, ?> map) {
                    if (i == names.length - 1) {
                        return map.containsKey(names[i]);
                    }
                    node = map.get(names[i]);
                } else {
                    return false;
                }
            }
            return node != null;
        }
    }

    // ==================== Value expression (literal or path) ====================

    sealed interface ValueExpression {
        Object resolve(Object current, Object root);
    }

    record LiteralValue(Object value) implements ValueExpression {
        @Override
        public Object resolve(Object current, Object root) {
            return value;
        }
    }

    record PathValue(PathExpression path) implements ValueExpression {
        @Override
        public Object resolve(Object current, Object root) {
            return path.resolve(current, root);
        }
    }

    // ==================== Comparison operators ====================

    enum Operator {
        EQ, NE, LT, LE, GT, GE;

        boolean compare(Object left, Object right) {
            if (left == null && right == null) {
                return this == EQ;
            }
            if (left == null || right == null) {
                return this == NE;
            }

            // String comparison
            if (left instanceof String ls && right instanceof String rs) {
                int cmp = ls.compareTo(rs);
                return switch (this) {
                    case EQ -> cmp == 0;
                    case NE -> cmp != 0;
                    case LT -> cmp < 0;
                    case LE -> cmp <= 0;
                    case GT -> cmp > 0;
                    case GE -> cmp >= 0;
                };
            }

            // Boolean equality
            if (left instanceof Boolean lb && right instanceof Boolean rb) {
                return switch (this) {
                    case EQ -> lb.equals(rb);
                    case NE -> !lb.equals(rb);
                    default -> false;
                };
            }

            // Numeric comparison
            double ld = toDouble(left);
            double rd = toDouble(right);
            if (!Double.isNaN(ld) && !Double.isNaN(rd)) {
                int cmp = Double.compare(ld, rd);
                return switch (this) {
                    case EQ -> cmp == 0;
                    case NE -> cmp != 0;
                    case LT -> cmp < 0;
                    case LE -> cmp <= 0;
                    case GT -> cmp > 0;
                    case GE -> cmp >= 0;
                };
            }

            // Fallback: equals
            return switch (this) {
                case EQ -> left.equals(right);
                case NE -> !left.equals(right);
                default -> false;
            };
        }

        private static double toDouble(Object value) {
            if (value instanceof Number n) {
                return n.doubleValue();
            }
            return Double.NaN;
        }
    }
}
