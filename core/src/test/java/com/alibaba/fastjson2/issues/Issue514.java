package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

public class Issue514 {
    @Test
    public void test() {
        String jsonStr =
                "{\"reqNo\":null,\"respNo\":\"efe467602fcf4aa887d7060a7b31954e\",\"success\":true,\"code\":\"0\",\"errMsg\":\"处理成功\",\"data\":{\"pageSize\":100,\"pageNum\":1,\"total\":1,\"records\":[{\"id\":74447518,\"customerPhone\":\"xxxx\",\"channelCode\":\"0\",\"customerName\":null,\"finishStatusId\":6,\"finishStatusName\":\"BUSY\",\"finishStatusDesc\":\"占线\",\"duration\":0,\"chatRound\":0,\"calloutStatusComment\":null,\"callerPhone\":\"大屏演示专用线路\",\"calledTimes\":1,\"startTime\":\"2022-06-27 20:29:07\",\"endTime\":\"2022-06-27 20:29:51\",\"callResult\":null,\"intentionResult\":\"D\"}]},\"currentTime\":1656398489012}";
        RecordSearchResponse response = JSON.parseObject(jsonStr, RecordSearchResponse.class);
        System.out.println(
                JSON.toJSONString(response)
        );
    }

    @Data
    public static class RecordSearchResponse {
        /**
         * 数据集
         */
        private InnerData data;

        @Data
        public static class InnerData {
            /**
             * 当前页面记录数
             */
            private long pageSize;
            /**
             * 当前页数
             */
            private long pageNum;
            /**
             * 记录总数
             */
            private long total;

            /**
             * 数据集
             */
            private List<InnerRecord> records;

            /**
             * 内部构造
             */
            @Data
            public static class InnerRecord {
                /**
                 * 外呼电话号码
                 */
                private String customerPhone;
                /**
                 * 通道编码
                 */
                private String channelCode;
                /**
                 * 客户姓名
                 */
                private String customerName;
                /**
                 * 外呼状态id
                 */
                private Long finishStatusId;
                /**
                 * 外呼状态名称
                 */
                private String finishStatusName;
                /**
                 * 外呼状态描述
                 */
                private String finishStatusDesc;
                /**
                 * 通话时间，单位秒，calloutStatusId 为100或者999时，值为空
                 */
                private Long duration;
                /**
                 * 通话轮次， calloutStatusId 为100或者999时，值为空
                 */
                private Long chatRound;
                /**
                 * 通话状态备注
                 */
                private String calloutStatusComment;
                /**
                 * 线路名称
                 */
                private String callerPhone;
                /**
                 * 已拨打次数
                 */
                private Long calledTimes;
                /**
                 * 呼叫开始时间戳
                 */
                private LocalDateTime startTime;
                /**
                 * 呼叫结束时间戳
                 */
                private LocalDateTime endTime;
                /**
                 * 外呼结果
                 */
                private List<CalloutResultDto> callResult;
                /**
                 * 意向结果
                 */
                private String intentionResult;

                @Data
                public static class CalloutResultDto {
                    /**
                     * 外呼结果key 1 客户关注点 2 客户意向等级
                     */
                    private String resultKey;

                    /**
                     * 外呼结果值
                     */
                    private String resultValue;

                    /**
                     * 外呼结果描述
                     */
                    private String resultDesc;
                }
            }
        }
    }
}
