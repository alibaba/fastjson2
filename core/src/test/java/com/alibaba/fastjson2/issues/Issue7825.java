package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for issue #7825 — skipComment() ArrayIndexOutOfBoundsException
 * when a multi-line comment ends with '*' as the last document character.
 *
 * @see <a href="https://github.com/alibaba/fastjson2/issues/7825">Issue #7825</a>
 */
public class Issue7825 {
    @Test
    public void testTruncatedCommentOnStar() {
        // {/** crashes with AIOOBE (should be JSONException)
        assertThrows(JSONException.class, () -> JSON.parse("{/**"));
    }

    @Test
    public void testTruncatedCommentBeforeStar() {
        // {/* already raises JSONException correctly
        assertThrows(JSONException.class, () -> JSON.parse("{/*"));
    }

    @Test
    public void testClosedCommentTruncated() {
        // {/**/ — comment closed, document truncated afterwards
        assertThrows(JSONException.class, () -> JSON.parse("{/**/"));
    }

    @Test
    public void testArrayWithTruncatedComment() {
        // [/**, array context
        assertThrows(JSONException.class, () -> JSON.parse("[/**"));
    }

    @Test
    public void testObjectWithContentBeforeComment() {
        // {"a":1,/** — object with content before truncated comment
        assertThrows(JSONException.class, () -> JSON.parse("{\"a\":1,/**"));
    }

    @Test
    public void testStarBeforeCommentEnd() {
        // {/*x* — comment '*' followed by no '/'
        assertThrows(JSONException.class, () -> JSON.parse("{/*x*"));
    }

    @Test
    public void testDoubleStarBeforeEnd() {
        // {/*** — triple star, still no closing '/'
        assertThrows(JSONException.class, () -> JSON.parse("{/***"));
    }

    @Test
    public void testUTF16TruncatedComment() {
        // UTF-16 string triggers JSONReaderUTF16 path
        assertThrows(JSONException.class, () -> JSON.parse("{\"中\":1,/**"));
    }

    @Test
    public void testSingleLineCommentUnaffected() {
        // Single-line comments (//) are not affected
        assertThrows(JSONException.class, () -> JSON.parse("{//"));
        assertThrows(JSONException.class, () -> JSON.parse("{//x"));
    }

    @Test
    public void testValidCommentStillWorks() {
        // Normal multi-line comment should still parse
        JSON.parse("{/**/}");
        JSON.parse("{/*a*/}");
        JSON.parse("{/* comment */ \"key\": 1}");
    }
}