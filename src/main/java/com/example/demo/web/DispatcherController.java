package com.example.demo.web;

import com.example.demo.dto.TicketResponse;
import com.example.demo.model.Ticket;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dispatcher")
public class DispatcherController {

    private final TicketService ticketService;

    public DispatcherController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public String dispatcherPanel(@AuthenticationPrincipal UserDetails principal, Model model) {
        User dispatcher = new User();
        dispatcher.setId(1L);
        dispatcher.setUsername(principal.getUsername());
        dispatcher.setRole(UserRole.DISPATCHER);

        List<Ticket> tickets = ticketService.listForDispatcher();
        List<TicketResponse> responses = tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());

        model.addAttribute("tickets", responses);
        return "dispatcher-panel";
    }
}


