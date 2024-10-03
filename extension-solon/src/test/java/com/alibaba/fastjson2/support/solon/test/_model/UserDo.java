package com.alibaba.fastjson2.support.solon.test._model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author noear 2023/1/16 created
 */
@Getter
@Setter
public class UserDo implements Serializable {
    String s0;

    String s1 = "noear";

    Boolean b0;
    boolean b1 = true;

    Long n0;
    Long n1 = 1L;

    Double d0;
    Double d1 = 1.0D;

    Object obj0;
    List list0;
    Map map0;
    Map map1;
}
