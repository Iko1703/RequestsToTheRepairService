package com.example.demo.service.impl;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketEvent;
import com.example.demo.model.TicketEventType;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.User;
import com.example.demo.repository.TicketEventRepository;
import com.example.demo.service.TicketEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketEventServiceImpl implements TicketEventService {

    private final TicketEventRepository ticketEventRepository;

    public TicketEventServiceImpl(TicketEventRepository ticketEventRepository) {
        this.ticketEventRepository = ticketEventRepository;
    }

    @Override
    @Transactional
    public void logCreated(Ticket ticket, User user) {
        TicketEvent event = new TicketEvent();
        event.setTicket(ticket);
        if (user != null) {
            event.setUserName(user.getUsername());
        }
        event.setType(TicketEventType.CREATED);
        event.setDetails("Ticket created");
        ticketEventRepository.save(event);
    }

    @Override
    @Transactional
    public void logStatusChanged(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus, User user) {
        TicketEvent event = new TicketEvent();
        event.setTicket(ticket);
        if (user != null) {
            event.setUserName(user.getUsername());
        }
        event.setType(TicketEventType.STATUS_CHANGED);
        event.setDetails("Status changed from " + oldStatus + " to " + newStatus);
        ticketEventRepository.save(event);
    }

    @Override
    @Transactional
    public void logTaken(Ticket ticket, User master) {
        TicketEvent event = new TicketEvent();
        event.setTicket(ticket);
        if (master != null) {
            event.setUserName(master.getUsername());
        }
        event.setType(TicketEventType.ASSIGNED);
        event.setDetails("Ticket taken by master " + master.getUsername());
        ticketEventRepository.save(event);
    }
}




