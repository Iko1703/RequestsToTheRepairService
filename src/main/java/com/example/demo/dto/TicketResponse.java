package com.example.demo.dto;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;

import java.time.Instant;

public class TicketResponse {

    private Long id;
    private String clientName;
    private String phone;
    private String address;
    private String problemText;
    private TicketStatus status;
    private Long assignedToId;
    private String assignedToName;
    private Instant createdAt;
    private Instant updatedAt;

    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponse resp = new TicketResponse();
        resp.setId(ticket.getId());
        resp.setClientName(ticket.getClientName());
        resp.setPhone(ticket.getPhone());
        resp.setAddress(ticket.getAddress());
        resp.setProblemText(ticket.getProblemText());
        resp.setStatus(ticket.getStatus());
        if (ticket.getAssignedTo() != null) {
            resp.setAssignedToId(ticket.getAssignedTo().getId());
            resp.setAssignedToName(ticket.getAssignedTo().getUsername());
        }
        resp.setCreatedAt(ticket.getCreatedAt());
        resp.setUpdatedAt(ticket.getUpdatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}




