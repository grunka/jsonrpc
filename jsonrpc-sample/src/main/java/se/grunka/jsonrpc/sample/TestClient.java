package se.grunka.jsonrpc.sample;

import se.grunka.jsonrpc.Client;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TestClient {


    private static class DurationCounter {
        private AtomicLong[] counts = createLongs(1000);
        private AtomicLong longer = new AtomicLong();


        private AtomicLong[] createLongs(int length) {
            AtomicLong[] result = new AtomicLong[length];
            for (int i = 0; i < length; i++) {
                result[i] = new AtomicLong();
            }
            return result;
        }


        public void add(long duration) {
            if (duration > counts.length - 1) {
                longer.incrementAndGet();
            } else {
                counts[(int) duration].incrementAndGet();
            }
        }


        public long[] get() {
            long[] result = new long[counts.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = counts[i].get();
            }
            return result;
        }


        public long longer() {
            return longer.get();
        }
    }


    public static void main(String[] args) throws Exception {
        functional();

        final DurationCounter counter = new DurationCounter();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(64);
        for (int i = 0; i < 100; i++) {
            executor.scheduleAtFixedRate(new Runnable() {
                private TestInterface service = Client.create(TestInterface.class, "http://localhost:8888/api");


                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    service.sayHello("world");
                    long duration = System.currentTimeMillis() - start;
                    counter.add(duration);
                }
            }, 0, 1, TimeUnit.NANOSECONDS);
        }
        Thread.sleep(60000);
        long[] durations = counter.get();
        outputRuby(new long[][]{durations});
        long sum = 0;
        for (long count : durations) {
            sum += count;
        }
        System.out.println(Arrays.toString(durations));
        System.out.println("counter.longer() = " + counter.longer());
        System.out.println("sum = " + sum);
        System.exit(0);
    }


    private static void functional() {
        TestInterface testInterface = Client.create(TestInterface.class, "http://localhost:8888/api");
        System.out.println("testInterface.sayHello(\"world\") = " + testInterface.sayHello("world"));
        TestData testData = new TestData(1, 1, 1, 1, 1, "abc");
        System.out.println("testData = " + testData);
        System.out.println("testInterface.doThings(testData) = " + testInterface.doThings(testData));
        try {
            testInterface.unsupportedOperation();
            throw new Error("Did not fail");
        } catch (UnsupportedOperationException e) {
            System.out.println("Expected exception caught");
            if (!e.getMessage().equals("message")) {
                throw new Error("Did not get the expected exception");
            }
        }
        try {
            testInterface.somethingException();
            throw new Error("Did not fail");
        } catch (TestInterface.SomethingException e) {
            System.out.println("Expected exception caught");
            if (!e.getMessage().equals("something message")) {
                throw new Error("Did not get the expected exception");
            }
        }
    }


    private static void outputRuby(long[][] distributions) {
        System.out.println("require 'rubygems'");
        System.out.println("require 'gruff'");
        System.out.println("g = Gruff::Line.new");
        System.out.println("g.hide_legend = true");


        int maxLength = 0;
        for (int i = 0; i < distributions.length; i++) {
            maxLength = Math.max(maxLength, distributions[i].length);
            System.out.println("g.data(\"Series " + i + "\", " + Arrays.toString(distributions[i]) + ")");
        }
        System.out.print("g.labels = { 0 => \"0\"");
        for (int i = 20; i < maxLength; i += 20) {
            System.out.print(", " + i + " => \"" + i + "\"");
        }
        System.out.println("}");
        System.out.println("g.write('responses.png')");
    }
}
