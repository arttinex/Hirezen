package com.hirezen.model;

public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    REJECTED;

    public String label() {
        return switch (this) {
            case APPLIED -> "Applied";
            case SHORTLISTED -> "Shortlisted";
            case REJECTED -> "Rejected";
        };
    }
}
