package com.campusfind.lostfound.dto;

public class StatsSummaryDto {
    private long totalLost;
    private long totalFound;
    private long totalReturned;
    private long pendingClaims;
    private long totalUsers;
    private long potentialMatchesCount;

    public StatsSummaryDto() {
    }

    public StatsSummaryDto(long totalLost, long totalFound, long totalReturned, long pendingClaims, long totalUsers, long potentialMatchesCount) {
        this.totalLost = totalLost;
        this.totalFound = totalFound;
        this.totalReturned = totalReturned;
        this.pendingClaims = pendingClaims;
        this.totalUsers = totalUsers;
        this.potentialMatchesCount = potentialMatchesCount;
    }

    public long getTotalLost() {
        return totalLost;
    }

    public void setTotalLost(long totalLost) {
        this.totalLost = totalLost;
    }

    public long getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(long totalFound) {
        this.totalFound = totalFound;
    }

    public long getTotalReturned() {
        return totalReturned;
    }

    public void setTotalReturned(long totalReturned) {
        this.totalReturned = totalReturned;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getPotentialMatchesCount() {
        return potentialMatchesCount;
    }

    public void setPotentialMatchesCount(long potentialMatchesCount) {
        this.potentialMatchesCount = potentialMatchesCount;
    }
}
