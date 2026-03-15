package com.alibaba.fastjson3.jsonpath;

import com.alibaba.fastjson3.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written recursive descent compiler for JSONPath expressions (RFC 9535).
 * Compiles a path string into an array of {@link JSONPathSegment}s.
 *
 * <p>Supported syntax:
 * <ul>
 *   <li>{@code $} — root</li>
 *   <li>{@code .name} or {@code ['name']} — child property</li>
 *   <li>{@code [0]}, {@code [-1]} — array index</li>
 *   <li>{@code [*]} — wildcard</li>
 *   <li>{@code [0:3]}, {@code [1:5:2]} — array slice</li>
 *   <li>{@code [0,2,4]} — multi-index union</li>
 *   <li>{@code ['a','b']} — multi-name union</li>
 *   <li>{@code ..name}, {@code ..[*]} — recursive descent</li>
 *   <li>{@code [?@.x > 1]} — filter expression</li>
 * </ul>
 */
public final class JSONPathCompiler {
    private final String path;
    private int pos;
    private boolean definite = true;

    private JSONPathCompiler(String path) {
        this.path = path;
        this.pos = 0;
    }

    public static CompileResult compile(String path) {
        if (path == null || path.isEmpty()) {
            throw new JSONException("JSONPath cannot be empty");
        }
        JSONPathCompiler compiler = new JSONPathCompiler(path);
        JSONPathSegment[] segments = compiler.parsePath();
        return new CompileResult(segments, compiler.definite);
    }

    public record CompileResult(JSONPathSegment[] segments, boolean definite) {
    }

    // ==================== Path parsing ====================

    private JSONPathSegment[] parsePath() {
        if (pos >= path.length() || path.charAt(pos) != '$') {
            throw error("expected '$'");
        }
        pos++; // skip '$'

        List<JSONPathSegment> segments = new ArrayList<>();
        while (pos < path.length()) {
            char c = path.charAt(pos);
            if (c == '.') {
                pos++;
                if (pos < path.length() && path.charAt(pos) == '.') {
                    // Recursive descent: ..
                    pos++;
                    definite = false;
                    JSONPathSegment child = parseRecursiveChild();
                    segments.add(new JSONPathSegment.RecursiveDescentSegment(child));
                } else {
                    // Dot notation: .name or .*
                    segments.add(parseDotChild());
                }
            } else if (c == '[') {
                segments.add(parseBracket());
            } else {
                throw error("unexpected character '" + c + "'");
            }
        }
        return segments.toArray(new JSONPathSegment[0]);
    }

    private JSONPathSegment parseDotChild() {
        if (pos >= path.length()) {
            throw error("expected property name after '.'");
        }
        char c = path.charAt(pos);
        if (c == '*') {
            pos++;
            definite = false;
            return new JSONPathSegment.WildcardSegment();
        }
        String name = readDotName();
        return new JSONPathSegment.NameSegment(name);
    }

    private JSONPathSegment parseRecursiveChild() {
        if (pos >= path.length()) {
            throw error("expected property name or '*' after '..'");
        }
        char c = path.charAt(pos);
        if (c == '*') {
            pos++;
            return new JSONPathSegment.WildcardSegment();
        }
        if (c == '[') {
            return parseBracket();
        }
        String name = readDotName();
        return new JSONPathSegment.NameSegment(name);
    }

    // ==================== Bracket notation ====================

    private JSONPathSegment parseBracket() {
        pos++; // skip '['
        skipWhitespace();

        if (pos >= path.length()) {
            throw error("unexpected end in bracket");
        }

        char c = path.charAt(pos);

        // Wildcard: [*]
        if (c == '*') {
            pos++;
            skipWhitespace();
            expect(']');
            definite = false;
            return new JSONPathSegment.WildcardSegment();
        }

        // Filter: [?...]
        if (c == '?') {
            pos++;
            definite = false;
            JSONPathFilter filter = parseFilterExpression();
            skipWhitespace();
            expect(']');
            return new JSONPathSegment.FilterSegment(filter);
        }

        // String literal: ['name'] or ['a','b']
        if (c == '\'' || c == '"') {
            return parseBracketNames();
        }

        // Numeric: could be index, slice, or multi-index
        if (c == '-' || c == ':' || (c >= '0' && c <= '9')) {
            return parseBracketNumeric();
        }

        throw error("unexpected character in bracket: '" + c + "'");
    }

    private JSONPathSegment parseBracketNames() {
        List<String> names = new ArrayList<>();
        names.add(readQuotedString());
        skipWhitespace();
        while (pos < path.length() && path.charAt(pos) == ',') {
            pos++; // skip ','
            skipWhitespace();
            names.add(readQuotedString());
            skipWhitespace();
        }
        expect(']');
        if (names.size() == 1) {
            return new JSONPathSegment.NameSegment(names.getFirst());
        }
        definite = false;
        return new JSONPathSegment.MultiNameSegment(names.toArray(new String[0]));
    }

    private JSONPathSegment parseBracketNumeric() {
        // Try to detect: index, slice, or multi-index
        // Peek ahead to determine type
        int saved = pos;
        Integer first = readOptionalInt();

        skipWhitespace();
        if (pos < path.length()) {
            char c = path.charAt(pos);

            // Slice: [start:end] or [start:end:step]
            if (c == ':') {
                definite = false;
                pos++;
                skipWhitespace();
                Integer end = readOptionalInt();
                skipWhitespace();
                int step = 1;
                if (pos < path.length() && path.charAt(pos) == ':') {
                    pos++;
                    skipWhitespace();
                    Integer s = readOptionalInt();
                    step = (s != null) ? s : 1;
                    if (step == 0) {
                        throw error("slice step cannot be 0");
                    }
                    skipWhitespace();
                }
                expect(']');
                return new JSONPathSegment.SliceSegment(first, end, step);
            }

            // Multi-index: [0,2,4]
            if (c == ',') {
                definite = false;
                List<Integer> indices = new ArrayList<>();
                if (first != null) {
                    indices.add(first);
                }
                while (pos < path.length() && path.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                    indices.add(readInt());
                    skipWhitespace();
                }
                expect(']');
                return new JSONPathSegment.MultiIndexSegment(indices.stream().mapToInt(i -> i).toArray());
            }

            // Single index: [0] or [-1]
            if (c == ']') {
                pos++;
                if (first == null) {
                    throw error("expected index");
                }
                return new JSONPathSegment.IndexSegment(first);
            }
        }

        throw error("unexpected token in bracket numeric expression");
    }

    // ==================== Filter expression parsing ====================

    private JSONPathFilter parseFilterExpression() {
        skipWhitespace();
        return parseLogicalOr();
    }

    private JSONPathFilter parseLogicalOr() {
        JSONPathFilter left = parseLogicalAnd();
        while (match("||")) {
            JSONPathFilter right = parseLogicalAnd();
            left = new JSONPathFilter.OrFilter(left, right);
        }
        return left;
    }

    private JSONPathFilter parseLogicalAnd() {
        JSONPathFilter left = parseFilterPrimary();
        while (match("&&")) {
            JSONPathFilter right = parseFilterPrimary();
            left = new JSONPathFilter.AndFilter(left, right);
        }
        return left;
    }

    private JSONPathFilter parseFilterPrimary() {
        skipWhitespace();
        if (pos < path.length() && path.charAt(pos) == '!') {
            pos++;
            return new JSONPathFilter.NotFilter(parseFilterPrimary());
        }
        if (pos < path.length() && path.charAt(pos) == '(') {
            pos++;
            JSONPathFilter inner = parseLogicalOr();
            skipWhitespace();
            expect(')');
            return inner;
        }
        return parseComparison();
    }

    private JSONPathFilter parseComparison() {
        skipWhitespace();
        JSONPathFilter.ValueExpression left = parseFilterValue();
        skipWhitespace();

        // Check for comparison operator
        JSONPathFilter.Operator op = readOperator();
        if (op == null) {
            // Existence test: just a path expression
            if (left instanceof JSONPathFilter.PathValue pv) {
                return new JSONPathFilter.ExistsFilter(pv.path());
            }
            throw error("expected comparison operator or logical operator");
        }

        skipWhitespace();
        JSONPathFilter.ValueExpression right = parseFilterValue();

        if (left instanceof JSONPathFilter.PathValue pv) {
            return new JSONPathFilter.ComparisonFilter(pv.path(), op, right);
        }
        // left is literal — wrap in a path-like comparison
        if (right instanceof JSONPathFilter.PathValue rpv) {
            // Reverse: literal op path → path reverseOp literal
            JSONPathFilter.Operator reversed = switch (op) {
                case LT -> JSONPathFilter.Operator.GT;
                case LE -> JSONPathFilter.Operator.GE;
                case GT -> JSONPathFilter.Operator.LT;
                case GE -> JSONPathFilter.Operator.LE;
                default -> op;
            };
            return new JSONPathFilter.ComparisonFilter(rpv.path(), reversed, left);
        }
        throw error("at least one side of comparison must be a path expression");
    }

    private JSONPathFilter.ValueExpression parseFilterValue() {
        skipWhitespace();
        if (pos >= path.length()) {
            throw error("unexpected end in filter expression");
        }
        char c = path.charAt(pos);

        // Path: @.name or $.name
        if (c == '@' || c == '$') {
            boolean isRoot = c == '$';
            pos++;
            List<String> names = new ArrayList<>();
            while (pos < path.length() && path.charAt(pos) == '.') {
                pos++;
                names.add(readDotName());
            }
            JSONPathFilter.PathExpression pe = new JSONPathFilter.PathExpression(isRoot, names.toArray(new String[0]));
            return new JSONPathFilter.PathValue(pe);
        }

        // String literal
        if (c == '\'' || c == '"') {
            return new JSONPathFilter.LiteralValue(readQuotedString());
        }

        // Number
        if (c == '-' || (c >= '0' && c <= '9')) {
            return new JSONPathFilter.LiteralValue(readNumber());
        }

        // Boolean/null
        if (path.startsWith("true", pos)) {
            pos += 4;
            return new JSONPathFilter.LiteralValue(Boolean.TRUE);
        }
        if (path.startsWith("false", pos)) {
            pos += 5;
            return new JSONPathFilter.LiteralValue(Boolean.FALSE);
        }
        if (path.startsWith("null", pos)) {
            pos += 4;
            return new JSONPathFilter.LiteralValue(null);
        }

        throw error("unexpected token in filter value");
    }

    // ==================== Token reading helpers ====================

    private String readDotName() {
        int start = pos;
        while (pos < path.length()) {
            char c = path.charAt(pos);
            if (c == '.' || c == '[' || c == ']' || c == ' ' || c == '(' || c == ')') {
                break;
            }
            pos++;
        }
        if (pos == start) {
            throw error("expected property name");
        }
        return path.substring(start, pos);
    }

    private String readQuotedString() {
        if (pos >= path.length()) {
            throw error("expected quoted string");
        }
        char quote = path.charAt(pos);
        if (quote != '\'' && quote != '"') {
            throw error("expected quote character");
        }
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < path.length()) {
            char c = path.charAt(pos);
            if (c == '\\' && pos + 1 < path.length()) {
                pos++;
                char escaped = path.charAt(pos);
                switch (escaped) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '\\' -> sb.append('\\');
                    case 'u' -> {
                        if (pos + 4 < path.length()) {
                            String hex = path.substring(pos + 1, pos + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                    }
                    default -> {
                        sb.append(escaped);
                    }
                }
            } else if (c == quote) {
                pos++;
                return sb.toString();
            } else {
                sb.append(c);
            }
            pos++;
        }
        throw error("unterminated string");
    }

    private Number readNumber() {
        int start = pos;
        if (pos < path.length() && path.charAt(pos) == '-') {
            pos++;
        }
        while (pos < path.length() && path.charAt(pos) >= '0' && path.charAt(pos) <= '9') {
            pos++;
        }
        boolean isDouble = false;
        if (pos < path.length() && path.charAt(pos) == '.') {
            isDouble = true;
            pos++;
            while (pos < path.length() && path.charAt(pos) >= '0' && path.charAt(pos) <= '9') {
                pos++;
            }
        }
        String numStr = path.substring(start, pos);
        return isDouble ? Double.parseDouble(numStr) : Long.parseLong(numStr);
    }

    private int readInt() {
        int start = pos;
        if (pos < path.length() && path.charAt(pos) == '-') {
            pos++;
        }
        while (pos < path.length() && path.charAt(pos) >= '0' && path.charAt(pos) <= '9') {
            pos++;
        }
        if (pos == start) {
            throw error("expected integer");
        }
        return Integer.parseInt(path.substring(start, pos));
    }

    private Integer readOptionalInt() {
        if (pos >= path.length()) {
            return null;
        }
        char c = path.charAt(pos);
        if (c != '-' && (c < '0' || c > '9')) {
            return null;
        }
        return readInt();
    }

    private JSONPathFilter.Operator readOperator() {
        skipWhitespace();
        if (pos + 1 < path.length()) {
            String two = path.substring(pos, pos + 2);
            JSONPathFilter.Operator op = switch (two) {
                case "==" -> JSONPathFilter.Operator.EQ;
                case "!=" -> JSONPathFilter.Operator.NE;
                case "<=" -> JSONPathFilter.Operator.LE;
                case ">=" -> JSONPathFilter.Operator.GE;
                default -> null;
            };
            if (op != null) {
                pos += 2;
                return op;
            }
        }
        if (pos < path.length()) {
            char c = path.charAt(pos);
            if (c == '<') {
                pos++;
                return JSONPathFilter.Operator.LT;
            }
            if (c == '>') {
                pos++;
                return JSONPathFilter.Operator.GT;
            }
        }
        return null;
    }

    private boolean match(String token) {
        skipWhitespace();
        if (path.startsWith(token, pos)) {
            pos += token.length();
            return true;
        }
        return false;
    }

    private void expect(char c) {
        if (pos >= path.length() || path.charAt(pos) != c) {
            throw error("expected '" + c + "'");
        }
        pos++;
    }

    private void skipWhitespace() {
        while (pos < path.length() && path.charAt(pos) <= ' ') {
            pos++;
        }
    }

    private JSONException error(String message) {
        return new JSONException("JSONPath compile error at position " + pos + ": " + message
                + " in path: " + path);
    }
}
