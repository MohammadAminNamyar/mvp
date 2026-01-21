package com.example.squarespool.dto;

public class PlayerHistorySquare {
    private int idx;
    private boolean wonQ1;
    private boolean wonQ2;
    private boolean wonQ3;
    private boolean wonFinal;

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isWonQ1() {
        return wonQ1;
    }

    public void setWonQ1(boolean wonQ1) {
        this.wonQ1 = wonQ1;
    }

    public boolean isWonQ2() {
        return wonQ2;
    }

    public void setWonQ2(boolean wonQ2) {
        this.wonQ2 = wonQ2;
    }

    public boolean isWonQ3() {
        return wonQ3;
    }

    public void setWonQ3(boolean wonQ3) {
        this.wonQ3 = wonQ3;
    }

    public boolean isWonFinal() {
        return wonFinal;
    }

    public void setWonFinal(boolean wonFinal) {
        this.wonFinal = wonFinal;
    }
}
