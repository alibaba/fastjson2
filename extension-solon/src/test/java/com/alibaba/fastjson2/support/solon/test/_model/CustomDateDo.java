package com.alibaba.fastjson2.support.solon.test._model;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author noear 2024/9/4 created
 */
@Setter
@Getter
public class CustomDateDo {
    private Date date;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date date2;
}
