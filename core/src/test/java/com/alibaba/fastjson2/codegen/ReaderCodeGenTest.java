package com.alibaba.fastjson2.codegen;

import com.alibaba.fastjson2.eishay.vo.Image;
import org.junit.jupiter.api.Test;

public class ReaderCodeGenTest {
    @Test
    public void test_gen() {
        ObjectReaderGen gen = new ObjectReaderGen(Image.class);
        gen.gen();
    }
}
