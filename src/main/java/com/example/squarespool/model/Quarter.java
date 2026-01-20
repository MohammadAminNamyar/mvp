package com.example.squarespool.model;

public enum Quarter {
    Q1("1st"),
    Q2("half"),
    Q3("3rd"),
    Q4("final");

    private final String label;

    Quarter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
