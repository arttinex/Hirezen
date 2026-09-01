package com.hirezen.model;

/**
 * One stat card on a dashboard: a label and a number. Every role's
 * dashboard controller method builds a List<StatCard> with different
 * content, but the shape is always the same - which is what lets a
 * single dashboard.html template render all three roles without any
 * role-specific branching for the stats grid itself.
 */
public record StatCard(String label, long value) {
}
