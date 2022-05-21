JSONB存储格式设计

# 1. 设计目标
1. 和JSON格式对应，能完全表示JSON
2. 紧凑，数据不留空洞
3. 对常用整数-16~63做紧凑设计
4. 对null/true/false做紧凑设计
5. 对0~15长度的ARRAY 做紧凑设计
6. 对0~47长度的ascii编码的字符串做紧凑设计
7. 对OBJECT类型的Name做短编码的支持


# 2. JSON支持格式定义
```
x91          # bianry len_int32 bytes
x92          # type [str] symbol_int32 jsonb
x93          # reference

x94 - xa3    # array_0 - array_15
xa4          # array len_int32 item*

xa5          # object_end
xa6          # object_start

xa7          # local time b0 b1 b2
xa8          # local datetime b0 b1 b2 b3 b4 b5 b6
xa9          # local date b0 b1 b2 b3
xab          # timestamp millis b0 b1 b2 b3 b4 b5 b6 b7
xac          # timestamp seconds b0 b1 b2 b3
xad          # timestamp minutes b0 b1 b2 b3
xae          # timestamp b0 b1 b2 b3 b4 b5 b6 b7 nano_int32

xaf          # null
xb0          # boolean false
xb1          # boolean true
xb2          # double 0
xb3          # double 1
xb4          # double_long
xb5          # double
xb6          # float_int
xb7          # float
xb8          # decimal_long
xb9          # decimal
xba          # bigint_long
xbb          # bigint
xbc          # short
xbd          # byte
xbe          # long
xbf          # long encoded as 32-bit int ('Y')
xc0 - xc7    # three-octet compact long (-x40000 to x3ffff)
xc8 - xd7    # two-octet compact long (-x800 to x7ff, xd0 is 0)
xd8 - xef    # one-octet compact long (-x8 to xf, xe0 is 0)

xf0 - xff    # one-octet compact int
x00 - x2f    # one-octet compact int

x30 - x3f    # two-octet compact int (-x800 to x7ff)
x40 - x47    # three-octet compact int (-x40000 to x3ffff)
x48          # 32-bit signed integer ('I')

x49 - x78    # ascii string length 0-47
x79          # ascii-8 variable-length
x7a          # utf-8 variable-length
x7b          # utf-16 variable-length
x7c          # utf-16LE variable-length
x7d          # utf-16BE variable-length
x7e          # gb18030 variable-length
x7f          # symbol
```
