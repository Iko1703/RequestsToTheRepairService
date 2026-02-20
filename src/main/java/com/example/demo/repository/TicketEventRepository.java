package com.example.demo.repository;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    List<TicketEvent> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}




