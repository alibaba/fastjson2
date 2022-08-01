package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FloatValidate {
    static final NumberFormat format = NumberFormat.getNumberInstance();

    public static void main(String[] args) throws Exception {
        ExecutorService executors = Executors.newFixedThreadPool(64);
        long end = 1_000_000_000_000_000L;
        int split = (int) (end / 10_000_000);
        long startMillis = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(split);
        for (int i = 0; i < split; i++) {
            Task task = new Task(latch, i, end, split);
            executors.submit(task);
        }
        latch.await();
        long millis = System.currentTimeMillis() - startMillis;

        System.out.println(
                format.format(end) + " all completed, millis " + format.format(millis)
        );
        executors.shutdown();
    }

    public static class Task
            implements Runnable {
        final CountDownLatch latch;
        final long start;
        final long end;
        final int increment;
        final char[] chars = new char[64];

        public Task(CountDownLatch latch, long start, long end, int increment) {
            this.latch = latch;
            this.start = start;
            this.end = end;
            this.increment = increment;
            chars[0] = '0';
            chars[1] = '.';
        }

        @Override
        public void run() {
            System.out.println(LocalDateTime.now() + " task-" + start + "/" + increment + " started");
            long startMillis = System.currentTimeMillis();
            for (long i = start; i < end; i += increment) {
                int len = IOUtils.stringSize(i);
                IOUtils.getChars(i, len + 2, chars);
                String str = new String(chars, 0, len + 2);
                double doubleValue = Double.parseDouble(str);
                double doubleValue1 = i / JSONFactory.SMALL_10_POW[len];
                if (doubleValue != doubleValue1) {
                    System.out.println(LocalDateTime.now() + "not match : " + str);
                }
            }
            long millis = System.currentTimeMillis() - startMillis;
            System.out.println(LocalDateTime.now() + " task-" + start + "/" + increment + " completed, millis " + format.format(millis));
            latch.countDown();
        }
    }
}
