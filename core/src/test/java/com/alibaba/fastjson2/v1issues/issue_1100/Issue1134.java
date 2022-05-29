package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 09/04/2017.
 */
public class Issue1134 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.blockpos = new BlockPos();
        model.blockpos.x = 526;
        model.blockpos.y = 65;
        model.blockpos.z = 554;
        model.passCode = "010";

        String text = JSON.toJSONString(model);
        assertEquals("{\"Dimension\":0,\"PassCode\":\"010\",\"BlockPos\":{\"x\":526,\"y\":65,\"z\":554}}", text);
    }

    public static class Model {
        @JSONField(ordinal = 1, name = "Dimension")
        private int dimension;
        @JSONField(ordinal = 2, name = "PassCode")
        private String passCode;
        @JSONField(ordinal = 3, name = "BlockPos")
        private BlockPos blockpos;

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public String getPassCode() {
            return passCode;
        }

        public void setPassCode(String passCode) {
            this.passCode = passCode;
        }

        public BlockPos getBlockpos() {
            return blockpos;
        }

        public void setBlockpos(BlockPos blockpos) {
            this.blockpos = blockpos;
        }
    }

    public static class BlockPos {
        public int x;
        public int y;
        public int z;
    }
}
