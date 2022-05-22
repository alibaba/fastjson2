package com.alibaba.fastjson2.v1issues.issue_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 20/03/2017.
 */
public class Issue1085 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = (Model) JSON.parseObject("{\"id\":123}", AbstractModel.class);
        assertEquals(123, model.id);
    }

    @Test
    public void test_for_issue_1() throws Exception {
        ObjectReader<AbstractModel> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(AbstractModel.class);
        Model model = (Model) objectReader.readObject(JSONReader.of("{\"id\":123}"));
        assertEquals(123, model.id);
    }

    public abstract static class AbstractModel {
        public int id;

        @JSONCreator
        public static Model createInstance() {
            return new Model();
        }
    }

    public static class Model
            extends AbstractModel {
    }
}
