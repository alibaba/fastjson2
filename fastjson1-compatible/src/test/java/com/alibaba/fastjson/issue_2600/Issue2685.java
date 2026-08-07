package com.alibaba.fastjson.issue_2600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.StringCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.MsgId;
import org.junit.jupiter.api.Test;
import org.marre.sms.SmsMessage;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2685 {
    @Test
    public void test_field() {
        SMGPSubmitMessage smgpSubmitMessage = new SMGPSubmitMessage();
        smgpSubmitMessage.setSequenceNo(1);
        smgpSubmitMessage.setServiceId("hell");
        smgpSubmitMessage.setMsgContent("hello"); // 注释掉可以正常
        smgpSubmitMessage.setChargeTermId("123555");
        smgpSubmitMessage.setSrcTermId("10086");
        CmppSubmitResponseMessage submitResponseMessage = new CmppSubmitResponseMessage(1);
        submitResponseMessage.setResult(0);
        submitResponseMessage.setMsgId(new MsgId());

        String smsMsg = JSON.toJSONString(smgpSubmitMessage);
        // System.out.println(smsMsg);

        System.out.println(smsMsg);

        com.alibaba.fastjson2.JSON.mixIn(SMGPSubmitMessage.class, Mixin.class);
        SMGPSubmitMessage smgpSubmitMessage2 = JSON.parseObject(smsMsg, SMGPSubmitMessage.class);
        assertEquals("hello", smgpSubmitMessage2.getMsgContent());
    }

    public interface Mixin {
        @JSONField(deserializeUsing = MyDeserializer.class)
        void setMsgContent(SmsMessage msg);
    }

    public static class MyDeserializer
            implements ObjectDeserializer {
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            String msg = StringCodec.deserialze(parser);
            return (T) CMPPCommonUtil.buildTextMessage(msg);
        }

        public int getFastMatchToken() {
            return JSONToken.LITERAL_STRING;
        }
    }
}
