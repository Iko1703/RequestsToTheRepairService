package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class TicketAssignRequest {

    @NotNull
    private Long masterId;

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }
}




