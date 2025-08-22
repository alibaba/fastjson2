# fastjson2 CSV Support

CSV (Comma-Separated Values) is a widely used data exchange file format that uses commas as separators. fastjson2 provides high-performance CSV reading and writing support.

## 1. Reading CSV Files

### 1.1 Counting Rows
Analyze how many rows a CSV format file has. If the CSV file has a header, the actual data row count should subtract 1:
```java
File file = ...;
int rowCount = CSVReader.rowCount(file);
```

### 1.2 Analyzing File Content
Perform statistical analysis on the CSV file to obtain column statistics:
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// Read the first header row first
parser.readHeader();

// Perform statistical analysis on the data
parser.statAll();

// Get analysis results for each column
List<StreamReader.ColumnStat> columns = parser.getColumnStats();

// Generate table creation statements based on column statistics
StringBuilder sql = new StringBuilder();
sql.append("CREATE TABLE ").append(tableName).append(" (
");
for (int i = 0; i < columns.size(); i++) {
    StreamReader.ColumnStat columnStat = columns.get(i);
    sql.append('\t')
        .append(columnName)  // Note: This should be the actual column name
        .append(' ')
        .append(columnStat.getInferSQLType());

    if (i != columns.size() - 1) {
        sql.append(',');
    }
    sql.append("
");
}
sql.append(");");
```

### 1.3 Reading Files with Default String Type
By default, all columns are read as String type:
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// Read the first header row if needed, can be omitted if there's no header
parser.readHeader();
        
while (true) {
    String[] line = parser.readLine();
    if (line == null) {
        break;
    }
    // Process each row of data
    // line[0], line[1], ... correspond to each column's data respectively
}
```

### 1.4 Reading Files with Specified Column Data Types
You can specify specific data types for each column:
```java
File file = ...;
Type[] types = new Type[] {
        Integer.class, 
        Long.class, 
        String.class, 
        Date.class 
};
// Construct CSVReader with column type information
CSVReader parser = CSVReader.of(file, types);

// Read the first header row if needed, can be omitted if there's no header
parser.readHeader();
        
while (true) {
    Object[] line = parser.readLineValues();
    if (line == null) {
        break;
    }
    
    // Process data, each column's value corresponds to the types specified when constructing CSVReader
    Integer v0 = (Integer) line[0];
    Long v1 = (Long) line[1];
    String v2 = (String) line[2];
    Date v3 = (Date) line[3];
}
```

### 1.5 Reading Each Row as a JavaBean
You can directly map each row of data to a JavaBean object:
```java
@Data
public class Person {
    private long id;
    private int age;
    private String name;
    private Date created;
}

File file = ...;

// Construct CSVReader with object type
CSVReader<Person> parser = CSVReader.of(file, Person.class);

// Read the first header row if needed, can be omitted if there's no header
parser.readHeader();

while (true) {
    Person person = parser.readLineObject();
    if (person == null) {
        break;
    }
    
    // Process data
    System.out.println("ID: " + person.id + ", Name: " + person.name);
}
```

### 1.6 Using Lambda Consumer to Read JavaBeans
Use functional programming approach to process all data:
```java
File file = ...;

// Construct CSVReader with object type
CSVReader<Person> parser = CSVReader.of(file, Person.class);

// Whether to read the first header row as needed
boolean readHeader = true;
parser.readLineObjectAll(
        readHeader,
        person -> {
            // Process each Person object
            System.out.println("ID: " + person.id + ", Name: " + person.name);
        }
);
```

### 1.7 Reading All Rows at Once
You can read all row data at once:
```java
File file = ...;
CSVReader parser = CSVReader.of(file);

// Read header (optional)
parser.readHeader();

// Read all rows at once
List<String[]> allLines = parser.readLineAll();

// Process all rows
for (String[] line : allLines) {
    // Process each row
}
```

## 2. Writing CSV Files

### 2.1 Basic Writing Operations
```java
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// Write header
writer.writeLine("ID", "Name", "Age", "Created");

// Write data rows
writer.writeLine(1001, "Alice", 25, new Date());
writer.writeLine(1002, "Bob", 30, new Date());

// Close writer
writer.close();
```

### 2.2 Writing with Objects
```java
// Define data class
@Data
public class Person {
    private long id;
    private String name;
    private int age;
    private Date created;
}

// Create data
List<Person> persons = Arrays.asList(
    new Person(1001, "Alice", 25, new Date()),
    new Person(1002, "Bob", 30, new Date())
);

// Write CSV file
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// Write header
writer.writeLine("ID", "Name", "Age", "Created");

// Write data
for (Person person : persons) {
    writer.writeLineObject(person);
}

writer.close();
```

### 2.3 Writing to String with StringWriter
```java
StringWriter stringWriter = new StringWriter();
CSVWriter writer = CSVWriter.of(stringWriter);

// Write data
writer.writeLine("Name", "Age");
writer.writeLine("Alice", 25);
writer.writeLine("Bob", 30);

// Get CSV content
String csvContent = stringWriter.toString();
writer.close();
```

### 2.4 Advanced Writing Operations
```java
File file = ...;
CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8);

// Write different types of data
writer.writeInt32(123);           // Write integer
writer.writeComma();              // Write comma separator
writer.writeString("text");       // Write string
writer.writeComma();
writer.writeDouble(3.14);         // Write double precision floating point
writer.writeComma();
writer.writeDate(new Date());     // Write date
writer.writeLine();               // Write line terminator

writer.close();
```

## 3. Performance Optimization Recommendations

1. **Use appropriate buffer sizes**: CSVReader and CSVWriter internally use buffers to improve performance.
2. **Batch operations**: Use `writeLine(Object...)` or `writeLine(List)` instead of writing fields individually whenever possible.
3. **Close resources promptly**: Use try-with-resources statements to ensure CSVReader/CSVWriter are properly closed.
4. **Predefine types**: Predefining column types during reading can avoid runtime type conversion overhead.

## 4. Important Notes

1. **Character encoding**: Ensure correct character encoding is used during reading and writing. UTF-8 is recommended.
2. **Special character handling**: CSVWriter automatically handles strings containing commas, quotes, and line breaks.
3. **Memory usage**: For large files, it's recommended to use streaming processing instead of loading all data at once.
4. **Exception handling**: Properly handle possible IOException and data parsing exceptions.