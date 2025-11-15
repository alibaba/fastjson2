### Effective only during serialization (converting Java objects to JSON strings):

*   **AfterFilter**: Adds extra content to the end of the serialized output.
*   **BeforeFilter**: Adds extra content at the beginning of the serialized output.
*   **NameFilter**: Used to modify property names during serialization.
*   **ContextNameFilter**: Same function as `NameFilter`, but it can modify property names using contextual information.
*   **ValueFilter**: Used to modify property values during serialization.
*   **ContextValueFilter**: Same function as `ValueFilter`, but it can modify property values using contextual information.
*   **LabelFilter**: Allows customizing the serialization of different fields based on different scenario labels (Label).
*   **PascalNameFilter**: This is a specific implementation of `NameFilter` that capitalizes the first letter of property names, converting from camelCase to PascalCase.
*   **PropertyPreFilter**: Performs fast filtering based on the property name to decide whether a property should be serialized.
*   **PropertyFilter**: Determines whether a property should be serialized based on its name and value.
*   **SimplePropertyPreFilter**: A simple property pre-filter that controls which fields are serialized by specifying lists of included or excluded properties.

### Effective only during deserialization (converting JSON strings to Java objects):

*   **ExtraProcessor**: Used to handle extra fields that exist in the JSON string but not in the Java object during deserialization.
*   **AutoTypeBeforeHandler**: In Fastjson 2.x, it provides `AutoType`-like functionality during deserialization, using a whitelist mechanism to control which classes are allowed to be deserialized, thus enhancing security.
*   **ContextAutoTypeBeforeHandler**: Also used to handle `AutoType`, but provides contextual information and has built-in support for some common types.
