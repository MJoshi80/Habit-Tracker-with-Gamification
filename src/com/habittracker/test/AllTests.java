package com.habittracker.test;

import java.util.ArrayList;
import java.util.List;

public class AllTests {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                  HABIT TRACKER TEST SUITE (Zero Dependencies)                  ");
        System.out.println("================================================================================");

        List<Class<?>> testClasses = new ArrayList<Class<?>>();
        testClasses.add(CsvUtilTest.class);
        testClasses.add(AsciiChartUtilTest.class);
        testClasses.add(HabitServiceTest.class);
        testClasses.add(TrackingServiceTest.class);

        int totalPassed = 0;
        int totalFailed = 0;
        List<String> allFailures = new ArrayList<String>();

        long startTime = System.currentTimeMillis();

        for (Class<?> clazz : testClasses) {
            TestRunner.TestResult result = TestRunner.runClass(clazz);
            totalPassed += result.passed;
            totalFailed += result.failed;
            allFailures.addAll(result.failures);
            System.out.println();
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("================================================================================");
        System.out.println("TEST SUMMARY");
        System.out.println("================================================================================");
        System.out.printf("Total Tests Run : %d\n", (totalPassed + totalFailed));
        System.out.printf("Passed          : %d\n", totalPassed);
        System.out.printf("Failed          : %d\n", totalFailed);
        System.out.printf("Execution Time  : %d ms\n", duration);

        if (totalFailed > 0) {
            System.out.println("\nFAILURES:");
            for (String f : allFailures) {
                System.out.println("  - " + f);
            }
            System.out.println("\nRESULT: FAILED");
            System.exit(1);
        } else {
            System.out.println("\nRESULT: ALL TESTS PASSED! [OK]");
            System.exit(0);
        }
    }
}
