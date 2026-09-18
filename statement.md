# Project Statement: Habit Tracker with Gamification

A Command-Line Java Application for Building Consistent Habits

---

## 1. Problem Statement

Building consistent personal and professional habits is notoriously difficult because progress is invisible day to day: a missed day feels no different from a completed one until weeks have elapsed and an inconsistent pattern has already settled in. Existing habit-tracking solutions fall into two extremes:
1. **Heavyweight mobile/cloud apps** that require cloud account creation, continuous internet connectivity, intrusive notifications, and monetization/ad walls, while failing to protect personal habit privacy.
2. **Static analog checklists or basic note files** that provide zero automated feedback on momentum, streaks, completion trajectories, or long-term consistency rates.

There is a distinct need for a distraction-free, privacy-first, fully offline command-line application that provides instantaneous, mathematically sound, and motivating feedback on consistency every time it is opened, with zero setup overhead or external infrastructure dependencies.

---

## 2. Scope of the Project

The scope of **Habit Tracker with Gamification** encompasses an end-to-end command-line habit management, streak analytics, and gamification system developed in Java. 

### In-Scope:
- **Habit Lifecycle Management:** Full creation, naming, categorization across 8 domains (Study, Fitness, Health, Coding, Reading, Meditation, Hydration, Other), scheduling (Daily and Weekly intervals), renaming, category reclassification, frequency updates, soft-deletion (archival), and permanent deletion.
- **Log Tracking & Streak Analytics:** Logging completions and explicit skips for today or historical dates (`YYYY-MM-DD`), input validation preventing future-dated or duplicate daily entries, and dual-frequency streak calculation algorithms (consecutive calendar days for daily habits and consecutive ISO 8601 calendar weeks for weekly habits).
- **Gamification Mechanics:** Experience Point (XP) calculation formula scaling with active streak length (`xp = base_xp (10) + min(max_bonus (40), (streak - 1) * 2)`), profile level progression (100 XP per level), and automated evaluation of 8 distinct achievement badges.
- **ASCII Visual Reporting:** Terminal-native visual dashboard featuring dynamic XP progress bars, star-rated streak indicators, 7-day visual completion strips (`[V]`, `[X]`, `[-]`), and a comprehensive badge cabinet.
- **Robust Persistence & Reliability:** Fully offline file-based persistence across three CSV schemas (`habits.csv`, `logs.csv`, `profile.csv`) utilizing an in-memory cache, $O(1)$ fast appending for daily logs, and atomic temporary-file renaming (`Files.move`) to prevent file truncation or corruption.
- **Built-in Quality Assurance:** A zero-dependency reflection test harness with 21 automated unit tests verifying core domain logic, streak rules, validation constraints, and file persistence.

### Out-of-Scope (Future Enhancements):
- Multi-user authentication and encrypted PIN login.
- Direct operating system push notifications or background daemon scheduling.
- External cloud synchronization or relational database server management (e.g., PostgreSQL/MySQL).

---

## 3. Target Users

1. **Students and Learners:** Individuals seeking a distraction-free, terminal-native environment to track study hours, coding drills, reading milestones, and personal habits alongside their existing daily workflow.
2. **Software Engineers and Terminal Users:** Professionals who spend the majority of their workday in the command line and prefer lightweight, privacy-preserving tools that operate without internet access or telemetry.
3. **Personal Productivity Enthusiasts:** Users who value data sovereignty, transparent mathematical progress metrics, and offline-first software.

---

## 4. High-Level Features

- **Module 1: Habit Management**
  - Interactive and programmatic habit definition with name validation (non-blank, $\le 60$ characters).
  - Case-insensitive active name uniqueness enforcement.
  - Frequency selection (Daily vs. Weekly ISO-week tracking).
  - Flexible lifecycle controls: in-place rename, category reassignment, non-destructive archiving, and complete deletion.

- **Module 2: Tracking & Streaks**
  - Idempotent and duplicate-safe check-in logging (completed or skipped).
  - Retrospective logging for past dates with strict future-date rejection.
  - Multi-frequency streak computation engine (walks backwards across temporal unit keys).
  - Longest unbroken streak calculation and historical consistency rate percentage.
  - 7-period rolling activity strip (`[V]` completed, `[X]` skipped/missed, `[-]` unlogged).

- **Module 3: Gamification & Reporting**
  - Dynamic XP calculation rewarding consistency while capping streak inflation.
  - Level progression and progress visualization to the next milestone.
  - 8 Unlockable Badges: *First Step*, *7-Day Warrior*, *30-Day Champion*, *100-Day Legend*, *Comeback Kid*, *Consistency Master*, *Rising Star* (Level 5), and *Habit Hero* (Level 10).
  - High-density ASCII terminal dashboard and badge showcase.

- **Non-Functional & Operational Capabilities**
  - Zero external libraries or package managers needed (pure Java Standard Edition SDK).
  - Atomic file writes preventing corruption during abnormal process termination.
  - Lightweight timestamped diagnostic logger (`data/app.log`).
  - Comprehensive automated unit test suite with instant command-line test runner.

