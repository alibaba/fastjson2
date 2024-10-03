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

/**
 * @author noear 2023/8/16 created
 */
public class OrderDo {
    long orderId = 2;

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }
}
