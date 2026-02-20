package com.example.demo.service;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.User;

public interface TicketEventService {

    void logCreated(Ticket ticket, User user);

    void logStatusChanged(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus, User user);

    void logTaken(Ticket ticket, User master);
}




