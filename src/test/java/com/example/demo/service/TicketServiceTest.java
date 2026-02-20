package com.example.demo.service;

import com.example.demo.dto.TicketCreateRequest;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private User createAndSaveDispatcher() {
        return userRepository.findByUsername("dispatcher")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("dispatcher");
                    u.setPasswordHash("test");
                    u.setRole(UserRole.DISPATCHER);
                    return userRepository.save(u);
                });
    }

    private User createAndSaveMaster(String name) {
        return userRepository.findByUsername(name)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(name);
                    u.setPasswordHash("test");
                    u.setRole(UserRole.MASTER);
                    return userRepository.save(u);
                });
    }

    @Test
    void createAndCompleteTicketHappyPath() {
        User dispatcher = createAndSaveDispatcher();
        User master = createAndSaveMaster("master1");

        TicketCreateRequest req = new TicketCreateRequest();
        req.setClientName("Client");
        req.setPhone("123");
        req.setAddress("Street");
        req.setProblemText("Something is broken");

        Ticket created = ticketService.createTicket(req, dispatcher);
        Assertions.assertEquals(TicketStatus.NEW, created.getStatus());

        // Назначаем мастера
        Ticket assigned = ticketService.assignTicket(created.getId(), master.getId(), dispatcher);
        Assertions.assertEquals(TicketStatus.ASSIGNED, assigned.getStatus());

        // Начинаем работу
        Ticket inProgress = ticketService.startWork(created.getId(), master);
        Assertions.assertEquals(TicketStatus.IN_PROGRESS, inProgress.getStatus());

        // Завершаем
        Ticket done = ticketService.completeTicket(created.getId(), master);
        Assertions.assertEquals(TicketStatus.DONE, done.getStatus());
    }

    @Test
    void masterCannotCompleteIfNotInProgress() {
        User dispatcher = createAndSaveDispatcher();
        User master = createAndSaveMaster("master1");

        TicketCreateRequest req = new TicketCreateRequest();
        req.setClientName("Client");
        req.setPhone("123");
        req.setAddress("Street");
        req.setProblemText("Something is broken");

        Ticket created = ticketService.createTicket(req, dispatcher);

        // Пытаемся сразу завершить из NEW
        Assertions.assertThrows(ForbiddenException.class,
                () -> ticketService.completeTicket(created.getId(), master));
    }
}




