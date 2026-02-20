package com.example.demo.service;

import com.example.demo.dto.TicketCreateRequest;
import com.example.demo.model.Ticket;
import com.example.demo.model.User;

import java.util.List;

public interface TicketService {

    Ticket createTicket(TicketCreateRequest request, User createdBy);

    Ticket takeTicket(Long ticketId, User master);

    Ticket assignTicket(Long ticketId, Long masterId, User dispatcher);

    Ticket startWork(Long ticketId, User master);

    Ticket completeTicket(Long ticketId, User master);

    void cancelTicket(Long ticketId, User actor);

    Ticket findByIdForUser(Long id, User currentUser);

    List<Ticket> listForDispatcher();

    List<Ticket> listForMaster(User master);
}


