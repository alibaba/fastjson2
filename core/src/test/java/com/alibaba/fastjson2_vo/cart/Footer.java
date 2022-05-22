package com.alibaba.fastjson2_vo.cart;

public class Footer
        extends CartObject<Footer.FooterFields> {
    public static class FooterFields {
        public Submit submit;
        public Pay pay;
        public CheckAll checkAll;
        public Quantity quantity;
    }

    public static class Pay {
        public String postTitle;
        public int price;
        public String priceTitle;
    }

    public static class Submit {
        public String title;
        public String status;
    }

    public static class CheckAll {
        public boolean checked;
        public String title;
        public boolean editable;
    }

    public static class Quantity {
        public int value;
    }
}
