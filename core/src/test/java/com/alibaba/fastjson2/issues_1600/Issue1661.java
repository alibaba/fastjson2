package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.BeanContext;
import com.alibaba.fastjson2.filter.ContextValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1661 {
    @Test
    public void test() throws Exception {
        Info info = new Info();
        info.getCard();
        String str = JSON.toJSONString(info, JSONWriter.Feature.FieldBased);
        assertEquals("{\"card\":{\"cardNo\":\"98765\"}}", str);

        byte[] jsonbBytes = JSONB.toBytes(info, JSONWriter.Feature.FieldBased);
        assertEquals("{\n" +
                "\t\"card\":{\n" +
                "\t\t\"cardNo\":\"98765\"\n" +
                "\t}\n" +
                "}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void test1() throws Exception {
        Info info = new Info();
        info.getCard();
        String str = JSON.toJSONString(info, new Myfilter(), JSONWriter.Feature.FieldBased);
        assertEquals("{\"card\":{\"cardNo\":\"98765\"}}", str);
    }

    public class Myfilter
            implements ContextValueFilter {
        @Override
        public Object process(BeanContext context, Object object, String name, Object value) {
            return value;
        }
    }

    public class Info {
        private Card card;

        public Card getCard() {
            card = new Card();
            card.setCardNo("98765");
            return card;
        }

        public void setCard(Card card) {
            this.card = card;
        }

        public class Card {
            private String cardNo;

            public String getCardNo() {
                return cardNo;
            }

            public void setCardNo(String cardNo) {
                this.cardNo = cardNo;
            }
        }
    }
}
