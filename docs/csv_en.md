CSV (comma-separated values) is a widely used data exchange file format that uses commas as delimiters.

fastjson provides high-performance support for reading and writing CSV.

# 1. Count Rows
Analyze how many rows a CSV file has. If the CSV file has a header, the number of data rows will be one less.
```java
File file = ...;
int rowCount = CSVReader.rowCount(file);
```

# 2. Analyze File Content
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// First, read the header (the first line)
parser.readHeader();

// Perform statistical analysis on the data
parser.statAll();

// Get the analysis results for each column
List<StreamReader.ColumnStat> columns = parser.getColumnStats();

// Generate a CREATE TABLE statement based on the column statistics
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

# 3. Read File with String Type by Default
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// If needed, read the header first. If there is no header, this can be skipped.
parser.readHeader();
        
while (true) {
    String[] line = parser.readLine();
    if (line == null) {
        break;
    }
    // handler line
}
```

# 4. Read File by Specifying Data Types for Each Column
```java
File file = ...;
Type[] types = new Type[] {
        Integer.class, 
        Long.class, 
        String.class , 
        Date.class 
};
// Construct the CSVReader, passing in the type information for each column
CSVReader parser = CSVReader.of(file, types);

// If needed, read the header first. If there is no header, this can be skipped.
parser.readHeader();
        
while (true) {
    Object[] line = parser.readLineValues();
    if (line == null) {
        break;
    }
    
    // Process the data. The value of each column will correspond to the types passed in when constructing the CSVReader.
    Integer v0 = (Integer) line[0];
    Long v1 = (Long) line[1];
    String v2 = (String) line[2];
    Date v3 = (Date) line[3];
}
```

# 5. Read Each Row into a JavaBean
```java
@Data
class Bean {
    long id;
    int age;
    String name;
    Date created;
}

File file = ...;

// Construct the CSVReader, passing in the object type
CSVReader parser = CSVReader.of(file, Bean.class);

// If needed, read the header first. If there is no header, this can be skipped.
parser.readHeader();

while (true) {
    Bean object = parser.readLineObject();
    if (object == null) {
        break;
    }
    
    // Process data ...
}
```

## 5.1 Read JavaBeans Using a Lambda Consumer
```java
File file = ...;

// Construct the CSVReader, passing in the object type
CSVReader parser = CSVReader.of(file, Bean.class);

// Specify whether to read the header first
boolean readHeader = true;
parser.readLineObjectAll(
        readHeader,
        e -> {
            // Process data ...
        }
);
```

# 6. Write to a CSV Format File
```java
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// Write data
Object[] row = ...;
writer.writeLine(row);

writer.close();
```
