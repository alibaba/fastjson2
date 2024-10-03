/*
 * Copyright 2017-2024 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
