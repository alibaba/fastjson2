import com.alibaba.fastjson2.internal.Cast;

public class TestToNumber {
    public static void main(String[] args) {
        System.out.println("Testing Cast.toNumber methods...");
        
        // Test with different input types
        System.out.println("toNumber(42): " + Cast.toNumber(42));
        System.out.println("toNumber(3.14): " + Cast.toNumber(3.14));
        System.out.println("toNumber(123L): " + Cast.toNumber(123L));
        System.out.println("toNumber(\"456\"): " + Cast.toNumber("456"));
        System.out.println("toNumber(\"3.14\"): " + Cast.toNumber("3.14"));
        System.out.println("toNumber(true): " + Cast.toNumber(true));
        System.out.println("toNumber(false): " + Cast.toNumber(false));
        System.out.println("toNumber('A'): " + Cast.toNumber('A'));
        
        // Test with Object to Number
        Object obj = 789;
        System.out.println("toNumber(Object 789): " + Cast.toNumber(obj));
        
        Object strObj = "999.5";
        System.out.println("toNumber(Object \"999.5\"): " + Cast.toNumber(strObj));
        
        System.out.println("All tests completed successfully!");
    }
}