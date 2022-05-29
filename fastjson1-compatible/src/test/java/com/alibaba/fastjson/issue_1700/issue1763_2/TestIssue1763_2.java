package com.alibaba.fastjson.issue_1700.issue1763_2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.issue_1700.issue1763_2.bean.BaseResult;
import com.alibaba.fastjson.issue_1700.issue1763_2.bean.CouponResult;
import com.alibaba.fastjson.issue_1700.issue1763_2.bean.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Issue 1763_2
 * 如果有多层泛型且前面泛型已经实现的情况下，判断下一级泛型
 *
 * @author cnlyml
 */
public class TestIssue1763_2<T> {
    private String jsonStr;
    private Class<T> clazz;

    @BeforeEach
    public void init() {
        jsonStr = "{\"code\":0, \"message\":\"Success\", \"content\":{\"pageNum\":1, \"pageSize\":2, \"size\":2, \"startRow\":1, \"endRow\":1, \"total\":2, \"pages\":1, \"list\":[{\"id\":10000001, \"couponName\":\"Test1\"}, {\"id\":10000002, \"couponName\": \"Test2\"}]}}";
        clazz = (Class<T>) CouponResult.class;
    }

    // 修复test
    @Test
    public void testFixBug1763_2() {
        BaseResult<PageResult<CouponResult>> data = JSON.parseObject(jsonStr, new TypeReference<BaseResult<PageResult<T>>>(clazz) {
        }.getType());

        assertTrue(data.isSuccess());
        assertTrue(data.getContent().getList().size() == 2);
        assertTrue(data.getContent().getList().get(0).getId().equals(10000001L));
        assertEquals(CouponResult.class, data.getContent().getList().get(0).getClass());
    }

    // 复现BUG
    @Test
    public void testBug1763_2() {
        BaseResult<PageResult<CouponResult>> data = JSON.parseObject(jsonStr, new TypeReferenceBug1763_2<BaseResult<PageResult<T>>>(clazz) {
        }.getType());

        assertTrue(data.isSuccess());
        assertTrue(data.getContent().getList().size() == 2);
        try {
            data.getContent().getList().get(0).getId();
        } catch (Throwable ex) {
            assertEquals(ex.getCause() instanceof ClassCastException, false);
        }
    }
}
