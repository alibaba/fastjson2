package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import junit.framework.TestCase;
import org.junit.Assert;

public class JSONObjectTest4 extends TestCase {

    public void test_interface() throws Exception {
        VO vo = JSON.parseObject("{id:123}", VO.class);
        Assert.assertEquals(123, vo.getId());
    }

    public static interface VO {
        @JSONField()
        int getId();

        @JSONField()
        void setId(int val);
    }
}
