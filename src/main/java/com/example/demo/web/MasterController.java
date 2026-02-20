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
@RequestMapping("/master")
public class MasterController {

    private final TicketService ticketService;

    public MasterController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public String masterPanel(@AuthenticationPrincipal UserDetails principal, Model model) {
        User master = new User();
        master.setId(2L); // временный id для демо
        master.setUsername(principal.getUsername());
        master.setRole(UserRole.MASTER);

        List<Ticket> tickets = ticketService.listForMaster(master);
        List<TicketResponse> responses = tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());

        model.addAttribute("tickets", responses);
        return "master-panel";
    }
}


