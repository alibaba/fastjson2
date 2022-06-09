/*
 * Copyright 1999-2018 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson2.util.BeanUtils;

public interface NameFilter
        extends SerializeFilter, com.alibaba.fastjson2.filter.NameFilter {
    static NameFilter of(PropertyNamingStrategy namingStrategy) {
        return (object, name, value) -> BeanUtils.fieldName(name, namingStrategy.name());
    }

    static NameFilter compose(NameFilter before, NameFilter after) {
        return (object, name, value) ->
                after.process(
                        object,
                        before.process(object, name, value),
                        value
                );
    }
}
