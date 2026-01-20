package com.example.squarespool.dto;

import com.example.squarespool.model.Quarter;
import jakarta.validation.constraints.NotNull;

public class ConfirmQuarterRequest {
    @NotNull
    private Quarter quarter;

    public Quarter getQuarter() {
        return quarter;
    }

    public void setQuarter(Quarter quarter) {
        this.quarter = quarter;
    }
}
