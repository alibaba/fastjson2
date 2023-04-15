CSV (comma-seperated values)是一种使用逗号作为分隔符的被广泛使用的数据交换文件格式。

fastjson提供了一个高性能CSV读写支持。

# 1. 识别行数
分析CSV格式的文件有多少行，如果CSV文件有Header，具体数据行数要减1
```java
File file = ...;
int rowCount = CSVReader.rowCount(file);
```

# 2. 分析文件内容
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// 先读取Header第一行
parser.readHeader();

// 对数据进行统计分析
parser.statAll();

// 获取各个列的分析结果
List<StreamReader.ColumnStat> columns = csvReader.getColumnStats();

// 根据列的统计信息生成建表语句
StringBuilder sql = new StringBuilder();
sql.append("CREATE TABLE ").append(tableName).append(" (\n");
for (int i = 0; i < columns.size(); i++) {
    StreamReader.ColumnStat columnStat = columns.get(i);
    sql.append('\t')
        .append(columnName)
        .append(' ')
        .append(columnStat.getInferSQLType());

    if (i != columns.size() - 1) {
        sql.append(',');
    }
    sql.append("\n");
}
sql.append(");");
```

# 3. 缺省按照String类型来读取文件
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
    // handler line
}
```

# 4. 指定每列的数据类型来读取文件
```java
File file = ...;
Type[] types = new Type[] {
        Integer.class, 
        Long.class, 
        String.class , 
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
    Integer v0 = line[0];
    Long v1 = line[1];
    String v2 = line[2];
    Date v3 = line[3];
}
```

# 5. 将每行数据读取成一个JavaBean
```java
@Data
class Bean {
    long id;
    int age;
    String name;
    Date created;
}

File file = ...;

// 构造CSVReader传入对象类型
SVReader parser = CSVReader.of(file, Bean.clss);

// 根据需要，先读取Header第一行，如果没有Header可以忽略
parser.readHeader();

while (true) {
    Bean object = (Bean) parser.readLineObject();
    if (object == null) {
        break;
    }
    
    // 处理数据 ...
}
```

# 6. 写入CSV格式文件
```java
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// 写入数据
Object[] row = ...;
writer.writeLine(row);

writer.close();
```
