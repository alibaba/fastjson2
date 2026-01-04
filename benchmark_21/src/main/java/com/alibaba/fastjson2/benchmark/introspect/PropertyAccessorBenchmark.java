package com.alibaba.fastjson2.benchmark.introspect;

import com.alibaba.fastjson2.introspect.PropertyAccessor;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactory;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactoryMethodHandle;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactoryUnsafe;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactoryVarHandle;
import lombok.Getter;
import lombok.Setter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class PropertyAccessorBenchmark {
    // Test class with fields and methods of different types
    @Getter
    @Setter
    public static class TestClass {
        private int intField = 1000;
        private String stringField = "test";
        private boolean booleanField = true;
        private long longField = 10000L;
        private float floatField = 30.5f;
        private double doubleField = 20.7;

        // Wrapper type fields
        private Integer intObjField = 1000;
        private String stringObjField = "test";
        private Boolean booleanObjField = true;
        private Long longObjField = 10000L;
        private Float floatObjField = 30.5f;
        private Double doubleObjField = 20.7;

        // Additional primitive types
        private byte byteField = 10;
        private short shortField = 100;
        private char charField = 'A';

        // Wrapper types
        private Byte byteObjField = 10;
        private Short shortObjField = 100;
        private Character charObjField = 'A';
    }

    @Param({
            "reflect",
            "unsafe & lambda",
            "MethodHandle",
            "VarHandle & lambda"
    })
    public String factory;
    private PropertyAccessorFactory propertyAccessorFactory;
    private Supplier supplier;
    private TestClass testObject;

    private PropertyAccessor
            byteFieldAccessor,
            byteGetterAccessor,
            byteSetterAccessor,
            shortFieldAccessor,
            shortGetterAccessor,
            shortSetterAccessor,
            charFieldAccessor,
            charGetterAccessor,
            charSetterAccessor,
            intFieldAccessor,
            intGetterAccessor,
            intSetterAccessor,
            longFieldAccessor,
            longGetterAccessor,
            longSetterAccessor,
            booleanFieldAccessor,
            booleanGetterAccessor,
            booleanSetterAccessor,
            floatFieldAccessor,
            floatGetterAccessor,
            floatSetterAccessor,
            doubleFieldAccessor,
            doubleGetterAccessor,
            doubleSetterAccessor;

    /**
     * Setup method that initializes the PropertyAccessorFactory based on the factory parameter
     * and creates property accessors for different field types in the TestClass.
     * This method is executed once before each benchmark run.
     *
     * @throws Exception if an error occurs during setup
     */
    @Setup
    public void setup() throws Exception {
        propertyAccessorFactory = switch (factory) {
            case "unsafe & lambda" -> new PropertyAccessorFactoryUnsafe();
            case "MethodHandle" -> new PropertyAccessorFactoryMethodHandle();
            case "VarHandle & lambda" -> new PropertyAccessorFactoryVarHandle();
            default -> new PropertyAccessorFactory();
        };

        Class<TestClass> clazz = TestClass.class;

        // Byte field accessors
        byteFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("byteField")
        );
        byteGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getByteField")
        );
        byteSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setByteField", byte.class));

        // Short field accessors
        shortFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("shortField")
        );
        shortGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getShortField")
        );
        shortSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setShortField", short.class));

        // Char field accessors
        charFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("charField")
        );
        charGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getCharField")
        );
        charSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setCharField", char.class));

        // Int field accessors
        intFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("intField")
        );
        intGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getIntField")
        );
        intSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setIntField", int.class));

        // Long field accessors
        longFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("longField")
        );
        longGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getLongField")
        );
        longSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setLongField", long.class));

        // Boolean field accessors
        booleanFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("booleanField")
        );
        booleanGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("isBooleanField")
        );
        booleanSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setBooleanField", boolean.class));

        // Float field accessors
        floatFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("floatField")
        );
        floatGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getFloatField")
        );
        floatSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setFloatField", float.class));

        // Double field accessors
        doubleFieldAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredField("doubleField")
        );
        doubleGetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("getDoubleField")
        );
        doubleSetterAccessor = propertyAccessorFactory.create(
                clazz.getDeclaredMethod("setDoubleField", double.class));

        supplier = propertyAccessorFactory.createSupplier(clazz.getDeclaredConstructor());
    }

    /**
     * Benchmark for measuring object allocation performance using Supplier.
     * This test measures how quickly objects can be instantiated using the
     * Supplier created by the PropertyAccessorFactory.
     *
     * @param bh Blackhole to prevent dead code elimination
     */
    @Benchmark
    public void allocate(Blackhole bh) {
        bh.consume(supplier.get());
    }

    /**
     * Benchmark for measuring field getter performance.
     * This test measures the performance of getting field values using
     * PropertyAccessor implementations for different primitive types.
     *
     * @param bh Blackhole to prevent dead code elimination
     */
    @Benchmark
    public void fieldGet(Blackhole bh) {
        bh.consume(byteFieldAccessor.getByteValue(testObject));
        bh.consume(shortFieldAccessor.getShortValue(testObject));
        bh.consume(charFieldAccessor.getCharValue(testObject));
        bh.consume(intFieldAccessor.getIntValue(testObject));
        bh.consume(longFieldAccessor.getLongValue(testObject));
        bh.consume(booleanFieldAccessor.getBooleanValue(testObject));
        bh.consume(floatFieldAccessor.getFloatValue(testObject));
        bh.consume(doubleFieldAccessor.getDoubleValue(testObject));
    }

    /**
     * Benchmark for measuring field setter performance.
     * This test measures the performance of setting field values using
     * PropertyAccessor implementations for different primitive types.
     *
     * @param bh Blackhole to prevent dead code elimination
     */
    @Benchmark
    public void fieldSet(Blackhole bh) {
        byteFieldAccessor.setByteValue(testObject, (byte) 30);
        shortFieldAccessor.setShortValue(testObject, (short) 30);
        charFieldAccessor.setCharValue(testObject, (char) 65);  // ASCII for 'A'
        intFieldAccessor.setLongValue(testObject, 30);
        longFieldAccessor.setLongValue(testObject, 30L);
        booleanFieldAccessor.setBooleanValue(testObject, true);
        floatFieldAccessor.setFloatValue(testObject, 30.5f);
        doubleFieldAccessor.setDoubleValue(testObject, 30.5);
        bh.consume(testObject);
    }

    /**
     * Benchmark for measuring method getter performance.
     * This test measures the performance of getting values through getter methods
     * using PropertyAccessor implementations for different primitive types.
     *
     * @param bh Blackhole to prevent dead code elimination
     */
    @Benchmark
    public void getter(Blackhole bh) {
        bh.consume(byteGetterAccessor.getByteValue(testObject));
        bh.consume(shortGetterAccessor.getShortValue(testObject));
        bh.consume(charGetterAccessor.getCharValue(testObject));
        bh.consume(intGetterAccessor.getIntValue(testObject));
        bh.consume(longGetterAccessor.getLongValue(testObject));
        bh.consume(booleanGetterAccessor.getBooleanValue(testObject));
        bh.consume(floatGetterAccessor.getFloatValue(testObject));
        bh.consume(doubleGetterAccessor.getDoubleValue(testObject));
    }

    /**
     * Benchmark for measuring method setter performance.
     * This test measures the performance of setting values through setter methods
     * using PropertyAccessor implementations for different primitive types.
     *
     * @param bh Blackhole to prevent dead code elimination
     */
    @Benchmark
    public void setter(Blackhole bh) {
        byteSetterAccessor.setByteValue(testObject, (byte) 30);
        shortSetterAccessor.setShortValue(testObject, (short) 30);
        charSetterAccessor.setCharValue(testObject, (char) 65);  // ASCII for 'A'
        intSetterAccessor.setIntValue(testObject, 30);
        longSetterAccessor.setLongValue(testObject, 30L);
        booleanSetterAccessor.setBooleanValue(testObject, true);
        floatSetterAccessor.setFloatValue(testObject, 30.5f);
        doubleSetterAccessor.setDoubleValue(testObject, 30.5);
        bh.consume(testObject);
    }

    /**
     * Main method to run the PropertyAccessorBenchmark.
     * This method sets up the benchmark options and runs the Runner.
     * It specifically includes only the allocate benchmark method,
     * with 1 warmup iteration, 1 measurement iteration, and 1 fork.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during benchmark execution
     */
    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(PropertyAccessorBenchmark.class.getSimpleName() + ".allocate")
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
