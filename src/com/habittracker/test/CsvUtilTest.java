package com.habittracker.test;

import com.habittracker.util.CsvUtil;

import java.util.List;

public class CsvUtilTest {

    public void testJoinSimple() {
        String line = CsvUtil.join("1", "Exercise", "Daily");
        TestRunner.assertEquals("Simple join should match", "1,Exercise,Daily", line);
    }

    public void testJoinEscapedWithCommas() {
        String line = CsvUtil.join("1", "Read, Write, Code", "Study");
        TestRunner.assertEquals("Commas should be wrapped in quotes", "1,\"Read, Write, Code\",Study", line);
    }

    public void testJoinEscapedWithQuotes() {
        String line = CsvUtil.join("1", "Say \"Hello\"", "Other");
        TestRunner.assertEquals("Quotes should be doubled and wrapped", "1,\"Say \"\"Hello\"\"\",Other", line);
    }

    public void testParseLineSimple() {
        List<String> tokens = CsvUtil.parseLine("1,Fitness,Daily,true");
        TestRunner.assertEquals("Should parse 4 tokens", 4, tokens.size());
        TestRunner.assertEquals("Token 0", "1", tokens.get(0));
        TestRunner.assertEquals("Token 1", "Fitness", tokens.get(1));
    }

    public void testParseLineWithQuotedCommas() {
        List<String> tokens = CsvUtil.parseLine("10,\"Math, Physics, Chem\",STUDY,DAILY");
        TestRunner.assertEquals("Should parse 4 tokens preserving comma inside quotes", 4, tokens.size());
        TestRunner.assertEquals("Quoted content should preserve comma", "Math, Physics, Chem", tokens.get(1));
    }

    public void testRoundTrip() {
        String originalName = "Study \"Algorithms\" & Data, Structures";
        String line = CsvUtil.join("42", originalName, "CODING");
        List<String> parsed = CsvUtil.parseLine(line);

        TestRunner.assertEquals("Round-trip count", 3, parsed.size());
        TestRunner.assertEquals("Round-trip string must match exactly", originalName, parsed.get(1));
    }
}
