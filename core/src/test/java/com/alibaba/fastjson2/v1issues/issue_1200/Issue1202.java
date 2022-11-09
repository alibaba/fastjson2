package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.date.LocaleSetter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 16/05/2017.
 */
public class Issue1202 {
    private final String dateText = "{\"date\":\"Apr 27, 2017 5:02:17 PM\"}";

    @BeforeAll
    static void setup() {
        LocaleSetter.setLocaleToEnglish();
    }

    @Nested
    class Model1Test {
        @Test
        public void test_for_issue_0() {
            Model1 model = JSON.parseObject(dateText, Model1.class);
            assertNotNull(model.getDate());
        }

        @Test
        public void test_for_issue_0_creators() {
            ObjectReaderCreator[] creators = TestUtils.readerCreators2();

            for (ObjectReaderCreator creator : creators) {
                ObjectReader<Model1> objectReader = creator.createObjectReader(Model1.class);
                Model1 model = objectReader.readObject(JSONReader.of(dateText));
                assertNotNull(model.getDate());
            }
        }

        public class Model1 {
            @JSONField(format = "MMM dd, yyyy h:mm:ss aa")
            private Date date;

            public Date getDate() {
                return date;
            }

            public void setDate(Date date) {
                this.date = date;
            }
        }
    }

    @Nested
    class Model2Test {
        @Test
        public void test_for_issue_2() throws Exception {
            Model2 model = JSON.parseObject(dateText, Model2.class);
            assertNotNull(model.date);
        }

        @Test
        public void test_for_issue_2_creators() {
            ObjectReaderCreator[] creators = TestUtils.readerCreators();

            for (ObjectReaderCreator creator : creators) {
                ObjectReader<Model2> objectReader = creator.createObjectReader(Model2.class);
                Model2 model = objectReader.readObject(JSONReader.of(dateText));
                assertNotNull(model.date);
            }
        }

        public class Model2 {
            @JSONField(format = "MMM dd, yyyy h:mm:ss aa")
            public Date date;
        }
    }

    @Nested
    class Model3Test {
        @Test
        public void test_for_issue_3() throws Exception {
            Model3 model = JSON.parseObject(dateText, Model3.class);
            assertNotNull(model.date);
        }

        @Test
        public void test_for_issue_3_creators() {
            ObjectReaderCreator[] creators = TestUtils.readerCreators2();

            for (ObjectReaderCreator creator : creators) {
                ObjectReader<Model3> objectReader = creator.createObjectReader(Model3.class);
                Model3 model = objectReader.readObject(JSONReader.of(dateText));
                assertNotNull(model.date);
            }
        }

        private class Model3 {
            @JSONField(format = "MMM dd, yyyy h:mm:ss aa")
            private Date date;

            public Date getDate() {
                return date;
            }

            public void setDate(Date date) {
                this.date = date;
            }
        }
    }
}
