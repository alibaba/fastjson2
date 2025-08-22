# fastjson2 CSV 支持

CSV (Comma-Separated Values) 是一种使用逗号作为分隔符的广泛使用的数据交换文件格式。fastjson2 提供了高性能的 CSV 读写支持。

## 1. 读取 CSV 文件

### 1.1 识别行数
分析 CSV 格式文件的行数，如果 CSV 文件有 Header，具体数据行数要减 1：
```java
File file = ...;
int rowCount = CSVReader.rowCount(file);
```

### 1.2 分析文件内容
对 CSV 文件进行统计分析，获取各列的统计信息：
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// 先读取Header第一行
parser.readHeader();

// 对数据进行统计分析
parser.statAll();

// 获取各个列的分析结果
List<StreamReader.ColumnStat> columns = parser.getColumnStats();

// 根据列的统计信息生成建表语句
StringBuilder sql = new StringBuilder();
sql.append("CREATE TABLE ").append(tableName).append(" (\n");
for (int i = 0; i < columns.size(); i++) {
    StreamReader.ColumnStat columnStat = columns.get(i);
    sql.append('\t')
        .append(columnName)  // 注意：这里应该是实际的列名
        .append(' ')
        .append(columnStat.getInferSQLType());

    if (i != columns.size() - 1) {
        sql.append(',');
    }
    sql.append("\n");
}
sql.append(");");
```

### 1.3 缺省按照 String 类型读取文件
默认情况下，所有列都按 String 类型读取：
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// 根据需要，先读取Header第一行，如果没有Header可以忽略
parser.readHeader();
        
while (true) {
    String[] line = parser.readLine();
    if (line == null) {
        break;
    }
    // 处理每一行数据
    // line[0], line[1], ... 分别对应每一列的数据
}
```

### 1.4 指定每列的数据类型读取文件
可以为每列指定具体的数据类型：
```java
File file = ...;
Type[] types = new Type[] {
        Integer.class, 
        Long.class, 
        String.class, 
        Date.class 
};
// 构造CSVReader传入各列的类型信息
CSVReader parser = CSVReader.of(file, types);

// 根据需要，先读取Header第一行，如果没有Header可以忽略
parser.readHeader();
        
while (true) {
    Object[] line = parser.readLineValues();
    if (line == null) {
        break;
    }
    
    // 处理数据，每列的值都会和构造CSVReader时传入的types对应
    Integer v0 = (Integer) line[0];
    Long v1 = (Long) line[1];
    String v2 = (String) line[2];
    Date v3 = (Date) line[3];
}
```

### 1.5 将每行数据读取成 JavaBean
可以将每行数据直接映射到 JavaBean 对象：
```java
@Data
public class Person {
    private long id;
    private int age;
    private String name;
    private Date created;
}

File file = ...;

// 构造CSVReader传入对象类型
CSVReader<Person> parser = CSVReader.of(file, Person.class);

// 根据需要，先读取Header第一行，如果没有Header可以忽略
parser.readHeader();

while (true) {
    Person person = parser.readLineObject();
    if (person == null) {
        break;
    }
    
    // 处理数据
    System.out.println("ID: " + person.id + ", Name: " + person.name);
}
```

### 1.6 使用 Lambda Consumer 读取 JavaBean
使用函数式编程方式处理所有数据：
```java
File file = ...;

// 构造CSVReader传入对象类型
CSVReader<Person> parser = CSVReader.of(file, Person.class);

// 根据需要，是否要读取Header第一行
boolean readHeader = true;
parser.readLineObjectAll(
        readHeader,
        person -> {
            // 处理每个Person对象
            System.out.println("ID: " + person.id + ", Name: " + person.name);
        }
);
```

### 1.7 一次性读取所有行
可以一次性读取所有行数据：
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// 读取Header（可选）
parser.readHeader();

// 一次性读取所有行
List<String[]> allLines = parser.readLineAll();

// 处理所有行
for (String[] line : allLines) {
    // 处理每一行
}
```

## 2. 写入 CSV 文件

### 2.1 基本写入操作
```java
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// 写入Header
writer.writeLine("ID", "Name", "Age", "Created");

// 写入数据行
writer.writeLine(1001, "Alice", 25, new Date());
writer.writeLine(1002, "Bob", 30, new Date());

// 关闭writer
writer.close();
```

### 2.2 使用对象写入
```java
// 定义数据类
@Data
public class Person {
    private long id;
    private String name;
    private int age;
    private Date created;
}

// 创建数据
List<Person> persons = Arrays.asList(
    new Person(1001, "Alice", 25, new Date()),
    new Person(1002, "Bob", 30, new Date())
);

// 写入CSV文件
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// 写入Header
writer.writeLine("ID", "Name", "Age", "Created");

// 写入数据
for (Person person : persons) {
    writer.writeLineObject(person);
}

writer.close();
```

### 2.3 使用 StringWriter 写入字符串
```java
StringWriter stringWriter = new StringWriter();
CSVWriter writer = CSVWriter.of(stringWriter);

// 写入数据
writer.writeLine("Name", "Age");
writer.writeLine("Alice", 25);
writer.writeLine("Bob", 30);

// 获取CSV内容
String csvContent = stringWriter.toString();
writer.close();
```

### 2.4 高级写入操作
```java
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// 写入不同类型的数据
writer.writeInt32(123);           // 写入整数
writer.writeComma();              // 写入逗号分隔符
writer.writeString("text");       // 写入字符串
writer.writeComma();
writer.writeDouble(3.14);         // 写入双精度浮点数
writer.writeComma();
writer.writeDate(new Date());     // 写入日期
writer.writeLine();               // 写入换行符

writer.close();
```

## 3. 性能优化建议

1. **使用适当的缓冲区大小**：CSVReader 和 CSVWriter 内部使用缓冲区来提高性能。
2. **批量操作**：尽可能使用 `writeLine(Object...)` 或 `writeLine(List)` 而不是逐个写入字段。
3. **及时关闭资源**：使用 try-with-resources 语句确保 CSVReader/CSVWriter 被正确关闭。
4. **类型预定义**：在读取时预定义列类型可以避免运行时类型转换开销。

## 4. 注意事项

1. **字符编码**：确保读写时使用正确的字符编码，推荐使用 UTF-8。
2. **特殊字符处理**：CSVWriter 会自动处理包含逗号、引号和换行符的字符串。
3. **内存使用**：对于大文件，建议使用流式处理而不是一次性加载所有数据。
4. **异常处理**：适当处理可能的 IOException 和数据解析异常。
