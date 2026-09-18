package com.habittracker.test;

import com.habittracker.util.AsciiChartUtil;

import java.util.Arrays;
import java.util.List;

public class AsciiChartUtilTest {

    public void testProgressBarZeroPercent() {
        String bar = AsciiChartUtil.renderProgressBar(0, 100, 10);
        TestRunner.assertTrue("Zero percent should contain 0%", bar.contains("0%"));
        TestRunner.assertTrue("Zero percent should contain empty blocks", bar.contains("----------"));
    }

    public void testProgressBarFiftyPercent() {
        String bar = AsciiChartUtil.renderProgressBar(50, 100, 10);
        TestRunner.assertTrue("50% progress bar should contain 50%", bar.contains("50%"));
        TestRunner.assertTrue("50% should have half filled", bar.contains("#####-----"));
    }

    public void testProgressBarHundredPercent() {
        String bar = AsciiChartUtil.renderProgressBar(100, 100, 10);
        TestRunner.assertTrue("100% progress bar should contain 100%", bar.contains("100%"));
        TestRunner.assertTrue("100% should be fully filled", bar.contains("##########"));
    }

    public void testStreakStars() {
        String s0 = AsciiChartUtil.renderStreakStars(0);
        TestRunner.assertTrue("0 streak representation", s0.contains("0 days"));

        String s7 = AsciiChartUtil.renderStreakStars(7);
        TestRunner.assertTrue("7 streak should have 2 stars", s7.contains("** 7 periods"));

        String s30 = AsciiChartUtil.renderStreakStars(30);
        TestRunner.assertTrue("30 streak should have 4 stars", s30.contains("**** 30 periods"));
    }

    public void testWeekStripMarkers() {
        List<String> statuses = Arrays.asList("DONE", "MISSED", "NONE", "COMPLETED");
        String strip = AsciiChartUtil.renderWeekStrip(statuses);

        TestRunner.assertTrue("Strip should contain done marker [V]", strip.contains("[V]"));
        TestRunner.assertTrue("Strip should contain missed marker [X]", strip.contains("[X]"));
        TestRunner.assertTrue("Strip should contain no-data marker [-]", strip.contains("[-]"));
    }
}
