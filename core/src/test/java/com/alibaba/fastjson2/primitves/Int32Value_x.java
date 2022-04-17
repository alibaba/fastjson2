package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2_vo.Int10;
import org.junit.jupiter.api.Test;

public class Int32Value_x {
    @Test
    public void test_0() throws Exception {
        Int10 vo = new Int10();
        vo.setV0000(0);
        vo.setV0001(1);
        vo.setV0002(2);
        vo.setV0003(3);
        vo.setV0004(4);
        vo.setV0005(5);
        vo.setV0006(6);
        vo.setV0007(7);
        vo.setV0008(8);
        vo.setV0009(9);

        ObjectWriter ow = ObjectWriters.objectWriter(Int10.class);

        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        System.out.println(json);
    }

    @Test
    public void test_1() throws Exception {
        Int10 vo = new Int10();
        vo.setV0000(0);
        vo.setV0001(-1);
        vo.setV0002(-2);
        vo.setV0003(-3);
        vo.setV0004(-4);
        vo.setV0005(-5);
        vo.setV0006(-6);
        vo.setV0007(-7);
        vo.setV0008(-8);
        vo.setV0009(-9);

        ObjectWriter ow = ObjectWriters.objectWriter(Int10.class);

        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        System.out.println(json);
    }
}
