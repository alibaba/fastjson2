package com.alibaba.fastjson2;

abstract class JSONPathSegment {
    public abstract void eval(JSONPath.Context context);

    static final class SelfSegment
            extends JSONPathSegment {
        static final SelfSegment INSTANCE = new SelfSegment();

        protected SelfSegment() {
        }

        @Override
        public void eval(JSONPath.Context context) {
            context.value = context.parent == null
                    ? context.root
                    : context.parent.value;
        }
    }
}
