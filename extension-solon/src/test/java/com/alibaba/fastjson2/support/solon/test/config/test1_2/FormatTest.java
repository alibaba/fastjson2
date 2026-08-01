package com.alibaba.fastjson2.support.solon.test.config.test1_2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * @author noear 2024/9/4 created
 */
public class FormatTest {
    public static void main(String[] args) {
        //
        // 当有 Provider::register 类型处理后，@JSONField 注解失效了
        //
        JSONWriter.Context context = new JSONWriter.Context();
        context.getProvider().register(Date.class, new ObjectWriter() {
            @Override
            public void write(JSONWriter jsonWriter, Object o, Object o1, Type type, long l) {
                jsonWriter.writeInt64(((Date) o).getTime());
            }
        });

        CustomDateDo dateDo = new CustomDateDo();

        dateDo.setDate(new Date(1673861993477L));
        dateDo.setDate2(new Date(1673861993477L));

        String json = JSON.toJSONString(dateDo, context);
        System.out.println(json); //{"date":1673861993477,"date2":1673861993477}

        Assertions.assertEquals("{\"date\":1673861993477,\"date2\":\"2023-01-16 17:39:53\"}", json);
    }

    public static class CustomDateDo {
        private Date date;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date date2;

        public void setDate(Date date) {
            this.date = date;
        }

        public void setDate2(Date date2) {
            this.date2 = date2;
        }

        public Date getDate() {
            return date;
        }

        public Date getDate2() {
            return date2;
        }
    }
}
