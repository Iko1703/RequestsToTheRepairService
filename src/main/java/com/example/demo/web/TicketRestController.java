package com.example.demo.web;

import com.example.demo.dto.TicketCreateRequest;
import com.example.demo.dto.TicketResponse;
import com.example.demo.model.Ticket;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final TicketService ticketService;

    public TicketRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // TODO: временный маппинг UserDetails -> доменный User (сейчас UserRole не связан с Security-пользователем)
    private User toDomainUser(UserDetails principal, UserRole role, Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername(principal.getUsername());
        u.setRole(role);
        return u;
    }

    @PostMapping
    public TicketResponse createTicket(@Valid @RequestBody TicketCreateRequest request,
                                       @AuthenticationPrincipal UserDetails principal) {
        // Для простоты считаем, что заявку создаёт диспетчер
        User creator = toDomainUser(principal, UserRole.DISPATCHER, 1L);
        Ticket ticket = ticketService.createTicket(request, creator);
        return TicketResponse.fromEntity(ticket);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal) {
        // Здесь роль/ид пользователя пока захардкожены, позже можно связать с UserRepository
        User current = toDomainUser(principal, UserRole.DISPATCHER, 1L);
        Ticket ticket = ticketService.findByIdForUser(id, current);
        return TicketResponse.fromEntity(ticket);
    }

    @GetMapping
    public List<TicketResponse> listTickets(@AuthenticationPrincipal UserDetails principal) {
        User current = toDomainUser(principal, UserRole.DISPATCHER, 1L);
        List<Ticket> tickets = current.getRole() == UserRole.DISPATCHER
                ? ticketService.listForDispatcher()
                : ticketService.listForMaster(current);

        return tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }
}




