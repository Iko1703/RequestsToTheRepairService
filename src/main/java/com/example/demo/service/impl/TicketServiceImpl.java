package com.example.demo.service.impl;

import com.example.demo.dto.TicketCreateRequest;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TicketEventService;
import com.example.demo.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketEventService ticketEventService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             TicketEventService ticketEventService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketEventService = ticketEventService;
    }

    @Override
    @Transactional
    public Ticket createTicket(TicketCreateRequest request, User createdBy) {
        Ticket ticket = new Ticket();
        ticket.setClientName(request.getClientName());
        ticket.setPhone(request.getPhone());
        ticket.setAddress(request.getAddress());
        ticket.setProblemText(request.getProblemText());
        ticket.setStatus(TicketStatus.NEW);

        Ticket saved = ticketRepository.save(ticket);
        ticketEventService.logCreated(saved, createdBy);
        return saved;
    }

    @Override
    @Transactional
    public Ticket takeTicket(Long ticketId, User master) {
        if (master.getRole() != UserRole.MASTER) {
            throw new ForbiddenException("Only masters can take tickets");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.NEW) {
            throw new ForbiddenException("Only tickets in NEW status can be taken");
        }
        if (ticket.getAssignedTo() != null) {
            throw new ForbiddenException("Ticket is already assigned");
        }

        ticket.setAssignedTo(master);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket saved = ticketRepository.saveAndFlush(ticket);

        ticketEventService.logTaken(saved, master);

        return saved;
    }

    @Override
    @Transactional
    public Ticket assignTicket(Long ticketId, Long masterId, User dispatcher) {
        if (dispatcher.getRole() != UserRole.DISPATCHER) {
            throw new ForbiddenException("Only dispatchers can assign tickets");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

        User master = userRepository.findById(masterId)
                .orElseThrow(() -> new NotFoundException("Master not found: " + masterId));

        if (master.getRole() != UserRole.MASTER) {
            throw new ForbiddenException("Assigned user must be a master");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setAssignedTo(master);
        ticket.setStatus(TicketStatus.ASSIGNED);

        Ticket saved = ticketRepository.saveAndFlush(ticket);
        ticketEventService.logStatusChanged(saved, oldStatus, TicketStatus.ASSIGNED, dispatcher);

        return saved;
    }

    @Override
    @Transactional
    public Ticket startWork(Long ticketId, User master) {
        if (master.getRole() != UserRole.MASTER) {
            throw new ForbiddenException("Only masters can start work");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(master.getId())) {
            throw new ForbiddenException("Ticket is not assigned to this master");
        }

        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new ForbiddenException("Only ASSIGNED tickets can be moved to IN_PROGRESS");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        ticketEventService.logStatusChanged(saved, oldStatus, TicketStatus.IN_PROGRESS, master);
        return saved;
    }

    @Override
    @Transactional
    public Ticket completeTicket(Long ticketId, User master) {
        if (master.getRole() != UserRole.MASTER) {
            throw new ForbiddenException("Only masters can complete tickets");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(master.getId())) {
            throw new ForbiddenException("Ticket is not assigned to this master");
        }

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new ForbiddenException("Only IN_PROGRESS tickets can be completed");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.DONE);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        ticketEventService.logStatusChanged(saved, oldStatus, TicketStatus.DONE, master);
        return saved;
    }

    @Override
    @Transactional
    public void cancelTicket(Long ticketId, User actor) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

        if (actor.getRole() == UserRole.MASTER) {
            if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(actor.getId())) {
                throw new ForbiddenException("Master can cancel only own tickets");
            }
        } else if (actor.getRole() != UserRole.DISPATCHER) {
            throw new ForbiddenException("Only dispatcher or assigned master can cancel ticket");
        }

        if (ticket.getStatus() == TicketStatus.DONE || ticket.getStatus() == TicketStatus.CANCELED) {
            throw new ForbiddenException("Cannot cancel completed or already canceled ticket");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.CANCELED);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        ticketEventService.logStatusChanged(saved, oldStatus, TicketStatus.CANCELED, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket findByIdForUser(Long id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));

        if (currentUser.getRole() == UserRole.MASTER) {
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Master has no access to this ticket");
            }
        }

        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listForDispatcher() {
        return ticketRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listForMaster(User master) {
        return ticketRepository.findByAssignedToAndStatusIn(
                master,
                List.of(TicketStatus.NEW, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS)
        );
    }
}



