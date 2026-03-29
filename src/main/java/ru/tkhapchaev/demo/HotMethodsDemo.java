package ru.tkhapchaev.demo;

import java.util.Random;

public class HotMethodsDemo {
    private final Random random = new Random(42);

    public void runScenario() {
        long checksum = 0;

        for (int i = 0; i < 6; i++) {
            checksum += cpuHeavyLoop(2_000_000 + i * 250_000);
            checksum += repeatedCalculations(1_400 + i * 200);
            checksum += slowBubbleSort(650 + i * 60)[0];
            checksum += manyTinyCalls(600_000);
        }

        System.out.println("Demo checksum: " + checksum);
    }

    private long cpuHeavyLoop(int iterations) {
        long accumulator = 0;

        for (int i = 0; i < iterations; i++) {
            accumulator += ((long) i * 31L) ^ (i >>> 3);
            accumulator %= 1_000_000_007L;
        }

        return accumulator;
    }

    private int repeatedCalculations(int repetitions) {
        int value = 0;

        for (int i = 0; i < repetitions; i++) {
            value += fibonacci(16);
        }

        return value;
    }

    private int fibonacci(int n) {
        if (n <= 1) {
            return n;
        }

        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    private int[] slowBubbleSort(int size) {
        int[] values = randomArray(size);

        for (int i = 0; i < values.length - 1; i++) {
            for (int j = 0; j < values.length - i - 1; j++) {
                if (values[j] > values[j + 1]) {
                    int temp = values[j];

                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                }
            }
        }

        return values;
    }

    private int[] randomArray(int size) {
        int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            values[i] = random.nextInt(1_000_000);
        }

        return values;
    }

    private long manyTinyCalls(int count) {
        long sum = 0;

        for (int i = 0; i < count; i++) {
            sum += tinyFunction(i);
        }

        return sum;
    }

    private int tinyFunction(int input) {
        return (input * 17) ^ (input >>> 2);
    }
}