# Habit Tracker with Gamification

A lightweight, distraction-free, offline command-line application built with Java to help users build and maintain consistent daily and weekly habits through habit tracking, streak analytics, and gamified milestones.

---

## Features

- **Habit Management:**
  - Create daily or weekly habits across 8 domains (*Study, Fitness, Health, Coding, Reading, Meditation, Hydration, Other*).
  - Enforces non-blank names, 60-character limits, and case-insensitive duplicate prevention.
  - In-place renaming, category reclassification, and frequency adjustments.
  - Non-destructive archiving (soft-delete preserves streak history) and permanent deletion.

- **Tracking & Streak Analytics:**
  - Fast check-in logging for today or historical dates (`YYYY-MM-DD`).
  - Strict validation preventing future dates and duplicate daily check-ins.
  - Multi-frequency streak calculation: consecutive calendar days for daily habits and consecutive ISO 8601 weeks for weekly habits.
  - Computes active streak, lifetime best streak, and overall completion percentages.
  - 7-period rolling visual activity strip (`[V]` done, `[X]` skipped, `[-]` no entry).

- **Gamification Mechanics:**
  - Experience Points (XP) reward formula scaling with consistency:
    $$\text{XP} = 10 + \min(40, (\text{streak} - 1) \times 2)$$
  - Profile leveling system (100 XP per level) with visual ASCII progress bars.
  - 8 Unlockable Achievement Badges: *First Step, 7-Day Warrior, 30-Day Champion, 100-Day Legend, Comeback Kid, Consistency Master, Rising Star, Habit Hero*.
  - Terminal-native ASCII dashboard and achievement cabinet.

- **Reliability & Data Safety:**
  - 100% offline flat-file CSV storage (`data/habits.csv`, `data/logs.csv`, `data/profile.csv`).
  - Atomic file writes using temporary files and atomic moves (`Files.move`) to eliminate the risk of corrupted files.
  - $O(1)$ fast appending for check-in logs.
  - In-memory caching for sub-millisecond query responses.
  - Zero external dependencies: works with any standard Java Development Kit (JDK 8 or higher).

---

## Project Structure

```text
habit-tracker/
├── src/
│   └── com/
│       └── habittracker/
│           ├── Main.java                          # Application Entry Point
│           ├── model/                             # Entities & Enums
│           │   ├── Habit.java                     # Habit Entity
│           │   ├── CompletionLog.java             # Completion Log Record
│           │   ├── UserProfile.java               # User Profile & Gamification State
│           │   ├── Frequency.java                 # DAILY, WEEKLY Enum
│           │   ├── HabitCategory.java             # 8 Category Enums
│           │   └── BadgeType.java                 # 8 Milestone Badges
│           ├── repository/                        # Data Persistence Layer
│           │   ├── Repository.java                # Generic CRUD Repository Interface
│           │   ├── FileHabitRepository.java       # Atomic CSV Habit Store
│           │   ├── FileLogRepository.java         # High-Performance CSV Log Store
│           │   └── FileProfileRepository.java     # User Profile Store
│           ├── service/                           # Business Logic Layer
│           │   ├── HabitService.java              # Habit CRUD & Validation
│           │   ├── TrackingService.java           # Streak & Tracking Engine
│           │   ├── GamificationService.java       # XP & Badge Evaluator
│           │   └── ReportService.java             # ASCII Dashboard & Visualizations
│           ├── exception/                         # Custom Exception Hierarchy
│           │   ├── HabitTrackerException.java     # Base Checked Exception
│           │   ├── HabitNotFoundException.java    # Missing Record Exception
│           │   ├── InvalidInputException.java     # Input Validation Exception
│           │   └── StorageException.java          # Persistence Exception
│           ├── util/                              # Utilities
│           │   ├── CsvUtil.java                   # RFC-4180 CSV Parser & Escaper
│           │   ├── DateUtil.java                  # ISO-8601 & Week Arithmetic
│           │   ├── AsciiChartUtil.java            # ASCII Bars & Indicators
│           │   └── AppLogger.java                 # Timestamped Local Logger
│           ├── ui/                                # Presentation Layer
│           │   └── ConsoleUI.java                 # Terminal Menu Loop
│           └── test/                              # Built-in Test Suite
│               ├── TestRunner.java                # Reflection Test Harness
│               ├── AllTests.java                  # Test Suite Runner
│               ├── CsvUtilTest.java               # CSV Utility Tests
│               ├── AsciiChartUtilTest.java        # ASCII Formatting Tests
│               ├── HabitServiceTest.java          # Habit Service Tests
│               └── TrackingServiceTest.java       # Tracking & Gamification Tests
├── data/                                          # Persisted Data Files
│   ├── habits.csv                                 # Saved Habits
│   ├── logs.csv                                   # Check-in History
│   └── profile.csv                                # User Profile & Badges
├── statement.md                                   # Problem Statement & Scope Specification
├── README.md                                      # Documentation & Guide
└── .gitignore                                     # Git Ignore Rules
```

---

## Prerequisites

- **Java Development Kit (JDK):** Version 8 or higher (`javac` and `java` available on your `PATH`).
- **External Dependencies:** **None.** Uses only the Java Standard Library.

---

## Compilation & Execution

### 1. Compile the Source Code

#### Windows (PowerShell / Command Prompt):
```cmd
if not exist out mkdir out
javac --release 8 -d out src\com\habittracker\*.java src\com\habittracker\model\*.java src\com\habittracker\repository\*.java src\com\habittracker\service\*.java src\com\habittracker\exception\*.java src\com\habittracker\util\*.java src\com\habittracker\ui\*.java src\com\habittracker\test\*.java
```

*Or in PowerShell:*
```powershell
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out }
javac --release 8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
```

#### Linux / macOS:
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

---

### 2. Run the Application

```bash
java -cp out com.habittracker.Main
```

---

### 3. Run the Unit Test Suite

The project includes 21 built-in unit tests across 4 test classes, executed via a reflection test harness:

```bash
java -cp out com.habittracker.test.AllTests
```

**Expected Test Output:**
```text
================================================================================
                  HABIT TRACKER TEST SUITE (Zero Dependencies)                  
================================================================================
Running test suite: CsvUtilTest
  [PASS] testParseLineWithQuotedCommas
  [PASS] testJoinEscapedWithQuotes
  [PASS] testJoinEscapedWithCommas
  [PASS] testParseLineSimple
  [PASS] testJoinSimple
  [PASS] testRoundTrip

Running test suite: AsciiChartUtilTest
  [PASS] testProgressBarZeroPercent
  [PASS] testWeekStripMarkers
  [PASS] testProgressBarFiftyPercent
  [PASS] testProgressBarHundredPercent
  [PASS] testStreakStars

Running test suite: HabitServiceTest
  [PASS] testSuccessfulCreation
  [PASS] testMissingIdLookup
  [PASS] testBlankNameRejection
  [PASS] testDuplicateNameRejection
  [PASS] testArchiveBehavior

Running test suite: TrackingServiceTest
  [PASS] testCompletionRateCalculation
  [PASS] testStreakResetAfterGap
  [PASS] testXpGrowthWithStreakLength
  [PASS] testDuplicateLogRejection
  [PASS] testStreakBuildingAcrossConsecutiveDays

================================================================================
TEST SUMMARY
================================================================================
Total Tests Run : 21
Passed          : 21
Failed          : 0
Execution Time  : ~200 ms

RESULT: ALL TESTS PASSED! [OK]
```

---

## Sample Dashboard Output

```text
========================================================================================
                                   HABIT TRACKER DASHBOARD                              
========================================================================================
 User: User            | Level 2  | Total XP: 156   XP
 Level Progress: [##############-----------]  56% (56/100 XP to Level 3)
 Badges Unlocked: 4 of 8
----------------------------------------------------------------------------------------
 ID   Habit Name           Category     Streak         Consistency        Last 7 Days                 
----------------------------------------------------------------------------------------
 1    Daily Java Coding    Coding       ** 8 periods   [########] 100%    [V] [V] [V] [V] [V] [V] [V] 
 2    Morning Workout      Fitness      * 1 period     [#####---]  67%    [-] [-] [-] [-] [V] [X] [V] 
 3    Read Technical Book  Reading      0 days         [--------]   0%    [-] [-] [-] [-] [-] [-] [-] 
========================================================================================
 Legend: [V] Completed   [X] Missed/Skipped   [-] No Entry
```

---

## License

This project is open source and available under the [MIT License](https://opensource.org/licenses/MIT).
