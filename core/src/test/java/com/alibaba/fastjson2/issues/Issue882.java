package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue882 {
    @Test
    public void test() {
        JSON.mixIn(MarginStatus.class, BitwiseEnumMixin.class);

        Bean t = new Bean();
        MarginStatus status = new MarginStatus();
        status.setLosscut(true);
        t.setStatus(status);
        assertEquals("{\"status\":2}", JSON.toJSONString(t));
    }

    public static class Bean {
        MarginStatus status;

        public MarginStatus getStatus() {
            return status;
        }

        public void setStatus(MarginStatus status) {
            this.status = status;
        }
    }

    public interface BitwiseEnum<T> {
        T getValue();

        boolean isEmpty();
    }

    @JSONType(includes = "value")
    public interface BitwiseEnumMixin<T> {
        @JSONField(value = true)
        T getValue();
    }

    public static class MarginStatus
            implements BitwiseEnum<Byte> {
        public static final byte WARNING_MASK = (1 << 0);
        public static final byte LOSSCUT_MASK = (1 << 1);

        private byte value;

        public MarginStatus() {
            this((byte) 0);
        }

        public MarginStatus(byte value) {
            this.value = value;
        }

        @Override
        public Byte getValue() {
            return this.value;
        }

        @Override
        public boolean isEmpty() {
            return this.value == 0;
        }

        public boolean isNormal() {
            return this.value == (byte) 0;
        }

        public boolean isWarning() {
            return (this.value & WARNING_MASK) != 0;
        }

        public boolean isLosscut() {
            return (this.value & LOSSCUT_MASK) != 0;
        }

        public void setWarning(final boolean enabled) {
            if (enabled) {
                this.value |= WARNING_MASK;
            } else {
                this.value &= ~WARNING_MASK;
            }
        }

        public void setLosscut(final boolean enabled) {
            if (enabled) {
                this.value |= LOSSCUT_MASK;
            } else {
                this.value &= ~LOSSCUT_MASK;
            }
        }
    }
}
