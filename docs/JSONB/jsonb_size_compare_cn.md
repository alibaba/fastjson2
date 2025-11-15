
# 带类型信息 KeyValue映射
|                | size |                             |
|----------------|------|-----------------------------|
| jsonb          | 409  | 使用WriteClassName            |
| hessian        | 644  |                             |
| fury           | 670  | 配置CompatibleMode.COMPATIBLE |
| java serialize | 1123 |                             |


# 不带类型信息 KeyValue映射
对象使用KeyValue的方式映射，这种方式的好处是增加和删除字段不影响兼容性

|            | size |               |
|------------|------|---------------|
| json       | 451  |               |
| jsonb      | 348  |               |

# 不带类型信息 Array映射
对象使用数组的方式映射，这种方式对增加/删除字段会影响兼容性

|            | size |               |
|------------|------|---------------|
| json       | 276  | 配置BeanToArray |
| jsonb      | 223  | 配置BeanToArray |
| protobuf-3 | 235  |               |

