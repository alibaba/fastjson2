package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue251 {
    @Test
    public void test0() {
        String str = "{\"msgid\":\"CAQQvPnc4QUY0On2rYSAgAMgooLa0Q8=\",\"action\":\"send\",\"from\":\"XuJinSheng\",\"tolist\":[\"icefog\"],\"roomid\":\"\",\"msgtime\":0,\"msgtype\":\"image\",\"image\":{\"md5sum\":\"50de8e5ae8ffe4f1df7a93841f71993a\",\"filesize\":70961,\"sdkfileid\":\"CtYBMzA2OTAyMDEwMjA0NjIzMDYwMDIwMTAwMDIwNGI3ZmU0MDZlMDIwMzBmNTliMTAyMDQ1YzliNTQ3NzAyMDQ1YzM3M2NiYzA0MjQ2NjM0MzgzNTM0NjEzNTY1MmQzNDYxMzQzODJkMzQzMTYxNjEyZDM5NjEzOTM2MmQ2MTM2NjQ2NDY0NjUzMDY2NjE2NjM1MzcwMjAxMDAwMjAzMDExNTQwMDQxMDUwZGU4ZTVhZThmZmU0ZjFkZjdhOTM4NDFmNzE5OTNhMDIwMTAyMDIwMTAwMDQwMBI4TkRkZk1UWTRPRGcxTVRBek1ETXlORFF6TWw4eE9UUTVOamN6TkRZMlh6RTFORGN4TWpNNU1ERT0aIGEwNGQwYWUyM2JlYzQ3NzQ5MjZhNWZjMjk0ZTEyNTkz\"}}\n";
        JSONObject object = JSON.parseObject(str);
        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean);
        assertNotNull(bean.image);
        assertEquals(70961, bean.image.getFileSize());
        assertEquals("CtYBMzA2OTAyMDEwMjA0NjIzMDYwMDIwMTAwMDIwNGI3ZmU0MDZlMDIwMzBmNTliMTAyMDQ1YzliNTQ3NzAyMDQ1YzM3M2NiYzA0MjQ2NjM0MzgzNTM0NjEzNTY1MmQzNDYxMzQzODJkMzQzMTYxNjEyZDM5NjEzOTM2MmQ2MTM2NjQ2NDY0NjUzMDY2NjE2NjM1MzcwMjAxMDAwMjAzMDExNTQwMDQxMDUwZGU4ZTVhZThmZmU0ZjFkZjdhOTM4NDFmNzE5OTNhMDIwMTAyMDIwMTAwMDQwMBI4TkRkZk1UWTRPRGcxTVRBek1ETXlORFF6TWw4eE9UUTVOamN6TkRZMlh6RTFORGN4TWpNNU1ERT0aIGEwNGQwYWUyM2JlYzQ3NzQ5MjZhNWZjMjk0ZTEyNTkz",
                bean.image.getSdkFileId());
        assertEquals("50de8e5ae8ffe4f1df7a93841f71993a", bean.image.getMd5sum());
    }

    public static class Bean {
        public TypeImage image;
    }

    @Data
    public static class TypeImage
            extends AbstractTypeFile {
    }

    @Data
    public abstract static class AbstractTypeFile {
        /**
         * 媒体资源的id信息。String类型
         */
        @JSONField(name = "sdkfileid")
        private String sdkFileId;

        /**
         * 资源的md5值，供进行校验。String类型
         */
        private String md5sum;

        /**
         * 资源的文件大小。Uint32类型
         */
        @JSONField(name = "filesize")
        private Integer fileSize;
    }
}
