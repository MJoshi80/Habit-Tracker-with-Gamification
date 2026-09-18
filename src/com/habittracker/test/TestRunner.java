package com.habittracker.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public static class TestResult {
        public int passed = 0;
        public int failed = 0;
        public List<String> failures = new ArrayList<String>();
    }

    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    public static void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new AssertionError("Assertion Failed (expected false): " + message);
        }
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion Failed: " + message + " (Expected: <" + expected + ">, Actual: <" + actual + ">)");
    }

    public static void assertEquals(String message, double expected, double actual, double delta) {
        if (Math.abs(expected - actual) <= delta) return;
        throw new AssertionError("Assertion Failed: " + message + " (Expected: <" + expected + ">, Actual: <" + actual + "> with delta " + delta + ")");
    }

    public static void assertNotNull(String message, Object obj) {
        if (obj == null) {
            throw new AssertionError("Assertion Failed: Object was null. " + message);
        }
    }

    public static TestResult runClass(Class<?> clazz) {
        TestResult result = new TestResult();
        System.out.println("Running test suite: " + clazz.getSimpleName());

        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("test") && Modifier.isPublic(m.getModifiers())) {
                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    m.invoke(instance);
                    System.out.printf("  [PASS] %s\n", m.getName());
                    result.passed++;
                } catch (Throwable t) {
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    System.out.printf("  [FAIL] %s: %s\n", m.getName(), cause.getMessage());
                    result.failed++;
                    result.failures.add(clazz.getSimpleName() + "." + m.getName() + ": " + cause.getMessage());
                }
            }
        }
        return result;
    }
}
