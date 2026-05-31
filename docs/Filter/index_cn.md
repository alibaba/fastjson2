### 仅在序列化（将 Java 对象转换为 JSON 字符串）时生效：

*   **AfterFilter**: 在序列化输出的末尾添加额外内容。
*   **BeforeFilter**: 在序列化输出的开头添加额外内容。
*   **NameFilter**: 用于在序列化时修改属性的名称。
*   **ContextNameFilter**: 功能同 `NameFilter` ，但它可以结合上下文信息修改属性名称。
*   **ValueFilter**: 用于在序列化时修改属性的值。
*   **ContextValueFilter**: 功能同 `ValueFilter` ，但它可以结合上下文信息修改属性值。
*   **LabelFilter**: 允许根据不同的场景标签（Label）来定制序列化输出不同的字段。
*   **PascalNameFilter**: 这是一个 `NameFilter` 的具体实现，它会将属性名称的首字母大写，实现驼峰命名到帕斯卡命名（Pascal）的转换。
*   **PropertyPreFilter**: 根据属性名进行快速过滤，决定某个属性是否需要被序列化。
*   **PropertyFilter**: 根据属性名和属性值来判断某个属性是否需要被序列化。
*   **SimplePropertyPreFilter**: 一个简便的属性预过滤器，通过指定包含（includes）或排除（excludes）的属性列表来控制哪些字段参与序列化。

### 仅在反序列化（将 JSON 字符串转换为 Java 对象）时生效：

*   **ExtraProcessor**: 用于处理在反序列化时，JSON 字符串中存在但 Java 对象中不存在的多余字段。
*   **AutoTypeBeforeHandler**: 在 Fastjson 2.x 中，用于在反序列化时提供类似 `AutoType` 功能，通过白名单机制来控制允许反序列化的类，以增强安全性。
*   **ContextAutoTypeBeforeHandler**: 同样用于处理 `AutoType`，并提供了上下文信息，内置了一些常用的类型支持。
