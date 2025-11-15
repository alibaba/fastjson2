
# Int32 Integer Handling
*   All three protocols use integer compression algorithms. Kryo's compression is the most effective, but their sizes will be quite similar in actual use.
*   jsonb and hessian have the same size.
*   For jsonb, in the size-1 range of [-16, 47], the binary value is consistent with the numeric value, which is designed for better readability during debugging.

|     | jsonb                                       | hessian                                   | kryo                                              | 
|-----|---------------------------------------------|-------------------------------------------|---------------------------------------------------|
| 1   | [-16, 47]                                   | [-16, 47]                                 | [-64, 63]                                         |
| 2   | [-2048, -17] <br/>[48, 2047]                     | [-2048, -17] <br/>[48, 2047]                   | [-8192, -65] <br/>[64, 8191]                           |
| 3   | [-262144, -2049] <br/>[2048, 262143]             | [-262144, -2049] <br/>[2048, 262143]           | [-1048576, -8193] <br/>[8192, 1048575]                 |
| 4   |                                             |                                           | [-134217728, -1048577] <br/>[1048576, 134217727]       |
| 5   | [-2147483648, -262145] <br/>[262144, 2147483647] | [-2147483648, -262145]<br/> [262144, 2147483647] | [-2147483648, -134217729] <br/>[134217727, 2147483647] |

