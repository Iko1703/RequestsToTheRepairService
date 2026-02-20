package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketCreateRequest {

    @NotBlank
    private String clientName;

    @NotBlank
    private String phone;

    @NotBlank
    private String address;

    @NotBlank
    private String problemText;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProblemText() {
        return problemText;
    }

    public void setProblemText(String problemText) {
        this.problemText = problemText;
    }
}




