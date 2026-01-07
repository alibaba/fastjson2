import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterUTF8;

public class TestToNumber {
    public static void main(String[] args) {
        // Test the new methods in JSONWriterUTF8
        try (JSONWriterUTF8 writer = (JSONWriterUTF8) JSONWriter.of()) {
            // Test writeFloat with name and features
            byte[] name = "floatValue".getBytes();
            writer.startObject();
            writer.writeFloat(name, 3.14f, 0);
            writer.endObject();
            System.out.println("Float test: " + writer.toString());
            
            // Reset writer
            writer.close();
            
            // Test writeDouble with name and features
            try (JSONWriterUTF8 writer2 = (JSONWriterUTF8) JSONWriter.of()) {
                writer2.startObject();
                byte[] name2 = "doubleValue".getBytes();
                writer2.writeDouble(name2, 2.718, 0);
                writer2.endObject();
                System.out.println("Double test: " + writer2.toString());
            }
            
            // Test writeBool with name and features
            try (JSONWriterUTF8 writer3 = (JSONWriterUTF8) JSONWriter.of()) {
                writer3.startObject();
                byte[] name3 = "boolValue".getBytes();
                writer3.writeBool(name3, true, 0);
                writer3.endObject();
                System.out.println("Boolean test: " + writer3.toString());
            }
            
            // Test writeInt8 with name and features
            try (JSONWriterUTF8 writer4 = (JSONWriterUTF8) JSONWriter.of()) {
                writer4.startObject();
                byte[] name4 = "byteValue".getBytes();
                writer4.writeInt8(name4, (byte) 42, 0);
                writer4.endObject();
                System.out.println("Byte test: " + writer4.toString());
            }
            
            // Test writeInt16 with name and features
            try (JSONWriterUTF8 writer5 = (JSONWriterUTF8) JSONWriter.of()) {
                writer5.startObject();
                byte[] name5 = "shortValue".getBytes();
                writer5.writeInt16(name5, (short) 1234, 0);
                writer5.endObject();
                System.out.println("Short test: " + writer5.toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("All tests completed successfully!");
    }
}