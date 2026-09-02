package com.hirezen.model;

public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    INTERNSHIP,
    CONTRACT;

    public String label() {
        return switch (this) {
            case FULL_TIME -> "Full-time";
            case PART_TIME -> "Part-time";
            case INTERNSHIP -> "Internship";
            case CONTRACT -> "Contract";
        };
    }
}
