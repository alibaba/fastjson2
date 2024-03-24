package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author 张治保
 * @since 2024/2/27
 */
public class Issue2296 {
    @Test
    void test() {
        final String jsonConfig = "{\n" +
                "  \"streamConfigs\": {\n" +
                "    \"CHAT\": {\n" +
                "      \"batchingConfig\": {\n" +
                "        \"enableBatching\": false,\n" +
                "        \"enableEmptyFraming\": false,\n" +
                "        \"maxBatchInterval\": 1000,\n" +
                "        \"maxBatchSize\": 1024,\n" +
                "        \"maxMsgCount\": 10,\n" +
                "        \"maxMsgCountPerSubscriber\": 15\n" +
                "      },\n" +
                "      \"deliveryConfig\": {\n" +
                "        \"accsDeliveryStrategy\": \"RELIABILITY\",\n" +
                "        \"distinctInMillis\": 5000,\n" +
                "        \"enableDistinction\": true,\n" +
                "        \"enableMessageOrdering\": true,\n" +
                "        \"redundance\": 1,\n" +
                "        \"sendReliably\": true\n" +
                "      },\n" +
                "      \"flowControlConfig\": {\n" +
                "        \"enableThrottling\": true,\n" +
                "        \"overFlowStrategy\": \"Fail\",\n" +
                "        \"perUserLimitPerSecond\": 10,\n" +
                "        \"receiveQueueSize\": 1024,\n" +
                "        \"throttle\": 1\n" +
                "      },\n" +
                "      \"messageTarget\": {\n" +
                "        \"messageTargetType\": \"TOPIC\",\n" +
                "        \"topicId\": {\n" +
                "          \"businessId\": {\n" +
                "            \"namespace\": 37\n" +
                "          },\n" +
                "          \"namespaceAlias\": \"37\",\n" +
                "          \"uniqueTopicId\": \"snakeOnline-test-10087\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"notifyOrCallbackConfig\": {\n" +
                "        \"idleNotifyIntervalInMillis\": 3000,\n" +
                "        \"idleNotifyThreshold\": -1,\n" +
                "        \"inboundMessageNotifyFilters\": null,\n" +
                "        \"outboundFrameCallbackFilters\": null,\n" +
                "        \"outboundMessageCallbackFilters\": null,\n" +
                "        \"unreachableNotifyIntervalInMillis\": 3000,\n" +
                "        \"unreachableNotifyThreshold\": -1\n" +
                "      },\n" +
                "      \"sequencerType\": \"PERSISTENT\"\n" +
                "    },\n" +
                "    \"MAIN\": {\n" +
                "      \"batchingConfig\": {\n" +
                "        \"enableBatching\": true,\n" +
                "        \"enableEmptyFraming\": true,\n" +
                "        \"maxBatchInterval\": 1000,\n" +
                "        \"maxBatchSize\": 2048,\n" +
                "        \"maxMsgCount\": 2,\n" +
                "        \"maxMsgCountPerSubscriber\": 15\n" +
                "      },\n" +
                "      \"deliveryConfig\": {\n" +
                "        \"accsDeliveryStrategy\": \"LOW_DELAY\",\n" +
                "        \"distinctInMillis\": 5000,\n" +
                "        \"enableDistinction\": true,\n" +
                "        \"enableMessageOrdering\": true,\n" +
                "        \"redundance\": 1,\n" +
                "        \"sendReliably\": true\n" +
                "      },\n" +
                "      \"flowControlConfig\": {\n" +
                "        \"enableThrottling\": false,\n" +
                "        \"overFlowStrategy\": \"Fail\",\n" +
                "        \"perUserLimitPerSecond\": 10,\n" +
                "        \"receiveQueueSize\": 1024,\n" +
                "        \"throttle\": 1\n" +
                "      },\n" +
                "      \"messageTarget\": {\n" +
                "        \"messageTargetType\": \"TOPIC\",\n" +
                "        \"topicId\": {\n" +
                "          \"$ref\": \"$.streamConfigs.CHAT.messageTarget.topicId\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"notifyOrCallbackConfig\": {\n" +
                "        \"idleNotifyIntervalInMillis\": 3000,\n" +
                "        \"idleNotifyThreshold\": 30,\n" +
                "        \"inboundMessageNotifyFilters\": null,\n" +
                "        \"outboundFrameCallbackFilters\": null,\n" +
                "        \"outboundMessageCallbackFilters\": null,\n" +
                "        \"unreachableNotifyIntervalInMillis\": 3000,\n" +
                "        \"unreachableNotifyThreshold\": 5\n" +
                "      },\n" +
                "      \"sequencerType\": \"IN_MEMORY\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"subscriberIdType\": \"USER\",\n" +
                "  \"ttl\": 94867200\n" +
                "}";
        JSONObject jsonObject = JSON.parseObject(jsonConfig);
        PmTopicConfig pmTopicConfig = jsonObject.to(PmTopicConfig.class);
        Assertions.assertNotNull(pmTopicConfig);

        Map<String, StreamConfig> streamConfigs = pmTopicConfig.getStreamConfigs();
        Assertions.assertFalse(streamConfigs == null || streamConfigs.isEmpty());

        StreamConfig chatConf = streamConfigs.get("CHAT");
        Assertions.assertNotNull(chatConf);
        MessageTarget chatConfMessageTarget = chatConf.getMessageTarget();
        Assertions.assertNotNull(chatConfMessageTarget);
        TopicId chatConfMessageTargetTopicId = chatConfMessageTarget.getTopicId();
        Assertions.assertNotNull(chatConfMessageTargetTopicId);

        StreamConfig mainConf = streamConfigs.get("MAIN");
        Assertions.assertNotNull(mainConf);
        MessageTarget mainConfMessageTarget = mainConf.getMessageTarget();
        Assertions.assertNotNull(mainConfMessageTarget);

        Assertions.assertEquals(chatConfMessageTargetTopicId, mainConfMessageTarget.getTopicId());
    }

    @Data
    private static class StreamConfig {
        private BatchingConfig batchingConfig;
        private DeliveryConfig deliveryConfig;
        private FlowControlConfig flowControlConfig;
        private MessageTarget messageTarget;
        private NotifyOrCallbackConfig notifyOrCallbackConfig;
        private String sequencerType;
    }

    @Data
    private static class BatchingConfig {
        private boolean enableBatching;
        private boolean enableEmptyFraming;
        private int maxBatchInterval;
        private int maxBatchSize;
        private int maxMsgCount;
        private int maxMsgCountPerSubscriber;
    }

    @Data
    private static class DeliveryConfig {
        private String accsDeliveryStrategy;
        private int distinctInMillis;
        private boolean enableDistinction;
        private boolean enableMessageOrdering;
        private int redundance;
        private boolean sendReliably;
    }

    @Data
    private static class FlowControlConfig {
        private boolean enableThrottling;
        private String overFlowStrategy;
        private int perUserLimitPerSecond;
        private int receiveQueueSize;
        private int throttle;
    }

    @Data
    private static class MessageTarget {
        private String messageTargetType;
        private TopicId topicId;
    }

    @Data
    private static class TopicId {
        private BusinessId businessId;
        private String namespaceAlias;
        private String uniqueTopicId;
    }

    @Data
    private static class BusinessId {
        private int namespace;
    }

    @Data
    private static class NotifyOrCallbackConfig {
        private int idleNotifyIntervalInMillis;
        private int idleNotifyThreshold;
        private Object inboundMessageNotifyFilters;
        private Object outboundFrameCallbackFilters;
        private Object outboundMessageCallbackFilters;
        private int unreachableNotifyIntervalInMillis;
        private int unreachableNotifyThreshold;
    }

    @Data
    private static class PmTopicConfig {
        private Map<String, StreamConfig> streamConfigs;
        private String subscriberIdType;
        private int ttl;
    }
}
