
# Key-Value Mapping with Type Information

|                | size |                             |
|----------------|------|-----------------------------|
| jsonb          | 409  | Using WriteClassName        |
| hessian        | 644  |                             |
| fury           | 670  | Configured with CompatibleMode.COMPATIBLE |
| java serialize | 1123 |                             |


# Key-Value Mapping without Type Information
Objects are mapped using a Key-Value approach. The advantage of this method is that adding and deleting fields does not affect compatibility.

|            | size |               |
|------------|------|---------------|
| json       | 451  |               |
| jsonb      | 348  |               |

# Array Mapping without Type Information
Objects are mapped using an array approach. This method's compatibility is affected by adding or deleting fields.

|            | size |               |
|------------|------|---------------|
| json       | 276  | Configured with BeanToArray |
| jsonb      | 223  | Configured with BeanToArray |
| protobuf-3 | 235  |               |
