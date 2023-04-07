package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class JSONStreamReader
        extends StreamReader {
    public JSONStreamReader(Type[] types) {
        super(types);
    }

    public JSONStreamReader(ObjectReaderAdapter objectReader) {
        super(objectReader);
    }

    public static JSONStreamReader of(File file) throws IOException {
        return of(new FileInputStream(file), StandardCharsets.UTF_8);
    }

    public static JSONStreamReader of(InputStream in) throws IOException {
        return of(in, StandardCharsets.UTF_8);
    }

    public static JSONStreamReader of(InputStream in, Type... types) throws IOException {
        return of(in, StandardCharsets.UTF_8, types);
    }

    public static JSONStreamReader of(InputStream in, Charset charset, Type... types) throws IOException {
        if (charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE) {
            return new JSONStreamReaderUTF16(new InputStreamReader(in, charset), types);
        }
        return new JSONStreamReaderUTF8(in, charset, types);
    }

    public static JSONStreamReader of(InputStream in, Class objectClass) {
        return of(in, StandardCharsets.UTF_8, objectClass);
    }

    public static JSONStreamReader of(InputStream in, Charset charset, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);

        if (charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE) {
            return new JSONStreamReaderUTF16(new InputStreamReader(in, charset), objectReader);
        }
        return new JSONStreamReaderUTF8(in, charset, objectReader);
    }

    public ColumnStat getColumnStat(String name) {
        if (this.columnStatsMap == null) {
            this.columnStatsMap = new LinkedHashMap<>();
        }
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        if (columnStats == null) {
            columnStats = new ArrayList<>();
        }

        ColumnStat stat = columnStatsMap.get(name);
        if (stat == null && columnStatsMap.size() <= 100) {
            stat = new ColumnStat(name);
            columnStatsMap.put(name, stat);
            columns.add(name);
            columnStats.add(stat);
        }
        return stat;
    }

    protected static void stat(ColumnStat stat, Object value) {
        if (stat == null) {
            return;
        }

        if (value == null) {
            stat.nulls++;
            return;
        }
        stat.values++;

        if (value instanceof Number) {
            stat.numbers++;

            if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
                stat.integers++;
            } else if (value instanceof Float || value instanceof Double) {
                stat.doubles++;
            }
            return;
        }

        if (value instanceof String) {
            stat.stat((String) value);
            return;
        }

        if (value instanceof Boolean) {
            stat.booleans++;
            return;
        }

        if (value instanceof Map) {
            stat.maps++;
            return;
        }

        if (value instanceof Collection) {
            stat.arrays++;
        }
    }

    public void statAll() {
        this.columnStatsMap = new LinkedHashMap<>();
        this.columns = new ArrayList<>();
        this.columnStats = new ArrayList<>();

        while (true) {
            Object object = readLineObject();
            if (object == null) {
                break;
            }

            statLine(object);
        }
    }

    public void statLine(Object object) {
        if (object instanceof Map) {
            statMap(null, (Map) object, 0);
        } else if (object instanceof List) {
            statArray(null, (List) object, 0);
        }
        rowCount++;
    }

    private void statArray(String parentKey, List list, int level) {
        if (level > 10) {
            return;
        }

        if (list.size() > 10) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            String strKey = parentKey == null ? "[" + i + "]" : parentKey + "[" + i + "]";
            ColumnStat stat = getColumnStat(parentKey);
            stat(stat, item);

            if (item instanceof Map) {
                statMap(strKey, (Map) item, level + 1);
            } else if (item instanceof List) {
                statArray(strKey, (List) item, level + 1);
            }
        }
    }

    private void statMap(String parentKey, Map map, int level) {
        if (level > 10) {
            return;
        }

        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            if (key instanceof String) {
                String strKey = parentKey == null ? (String) key : parentKey + "." + key;
                ColumnStat stat = getColumnStat(strKey);
                Object entryValue = entry.getValue();
                stat(stat, entryValue);

                if (entryValue instanceof Map) {
                    statMap(strKey, (Map) entryValue, level + 1);
                } else if (entryValue instanceof List) {
                    statArray(strKey, (List) entryValue, level + 1);
                }
            }
        }
    }
}
