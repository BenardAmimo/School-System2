package com.school.response;

public final class Grading {
    private Grading() {}

    /** Replace with your school's scale. */
    public static String grade(double percent) {
        if (percent >= 80) return "A";
        if (percent >= 70) return "B";
        if (percent >= 60) return "C";
        if (percent >= 50) return "D";
        return "E";
    }

    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
