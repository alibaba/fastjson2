package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class Issue577 {
    @Test
    public void test() {
        OffsetSerializeWrapper w = new OffsetSerializeWrapper();
        w.getOffsetTable().put(new MessageQueue("topic_1", "broker-a", 0), new AtomicLong(123));
        w.getOffsetTable().put(new MessageQueue("topic_1", "broker-a", 1), new AtomicLong(124));
        String json = w.toJson(true);
        System.out.println(json);
        OffsetSerializeWrapper w2 = OffsetSerializeWrapper.fromJson(json, OffsetSerializeWrapper.class);
        System.out.println(w2.toJson(true));
    }

    public static class MessageQueue
            implements Comparable<MessageQueue>, Serializable {
        private static final long serialVersionUID = 6191200464116433425L;
        private String topic;
        private String brokerName;
        private int queueId;

        public MessageQueue() {
        }

        public MessageQueue(String topic, String brokerName, int queueId) {
            this.topic = topic;
            this.brokerName = brokerName;
            this.queueId = queueId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public void setBrokerName(String brokerName) {
            this.brokerName = brokerName;
        }

        public int getQueueId() {
            return queueId;
        }

        public void setQueueId(int queueId) {
            this.queueId = queueId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((brokerName == null) ? 0 : brokerName.hashCode());
            result = prime * result + queueId;
            result = prime * result + ((topic == null) ? 0 : topic.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MessageQueue other = (MessageQueue) obj;
            if (brokerName == null) {
                if (other.brokerName != null) {
                    return false;
                }
            } else if (!brokerName.equals(other.brokerName)) {
                return false;
            }
            if (queueId != other.queueId) {
                return false;
            }
            if (topic == null) {
                if (other.topic != null) {
                    return false;
                }
            } else if (!topic.equals(other.topic)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "MessageQueue [topic=" + topic + ", brokerName=" + brokerName + ", queueId=" + queueId + "]";
        }

        @Override
        public int compareTo(MessageQueue o) {
            {
                int result = this.topic.compareTo(o.topic);
                if (result != 0) {
                    return result;
                }
            }

            {
                int result = this.brokerName.compareTo(o.brokerName);
                if (result != 0) {
                    return result;
                }
            }

            return this.queueId - o.queueId;
        }
    }

    public static class OffsetSerializeWrapper {
        private ConcurrentMap<MessageQueue, AtomicLong> offsetTable =
                new ConcurrentHashMap<MessageQueue, AtomicLong>();

        public ConcurrentMap<MessageQueue, AtomicLong> getOffsetTable() {
            return offsetTable;
        }

        public void setOffsetTable(ConcurrentMap<MessageQueue, AtomicLong> offsetTable) {
            this.offsetTable = offsetTable;
        }

        public String toJson(boolean prettyFormat) {
            return JSON.toJSONString(this, prettyFormat);
        }

        public static <T> T fromJson(String json, Class<T> classOfT) {
            return JSON.parseObject(json, classOfT);
        }
    }
}
