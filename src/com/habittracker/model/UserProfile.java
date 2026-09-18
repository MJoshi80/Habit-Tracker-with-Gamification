package com.habittracker.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class UserProfile {
    private String userName;
    private int totalXp;
    private final Set<BadgeType> badges;

    public UserProfile(String userName, int totalXp, Set<BadgeType> badges) {
        this.userName = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "User";
        this.totalXp = Math.max(0, totalXp);
        this.badges = (badges != null) ? EnumSet.copyOf(badges) : EnumSet.noneOf(BadgeType.class);
    }

    public UserProfile(String userName) {
        this(userName, 0, EnumSet.noneOf(BadgeType.class));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "User";
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = Math.max(0, totalXp);
    }

    public void addXp(int xp) {
        if (xp > 0) {
            this.totalXp += xp;
        }
    }

    public int getLevel() {
        return Math.max(1, (totalXp / 100) + 1);
    }

    public int getXpIntoCurrentLevel() {
        return totalXp % 100;
    }

    public int getXpRemainingForNextLevel() {
        return 100 - getXpIntoCurrentLevel();
    }

    public Set<BadgeType> getBadges() {
        return Collections.unmodifiableSet(badges);
    }

    public boolean unlockBadge(BadgeType badge) {
        if (badge == null) return false;
        return badges.add(badge);
    }

    public boolean hasBadge(BadgeType badge) {
        return badge != null && badges.contains(badge);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return totalXp == that.totalXp && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, totalXp);
    }
}
