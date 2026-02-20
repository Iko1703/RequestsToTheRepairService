package com.example.demo.service;

import com.example.demo.dto.TicketCreateRequest;
import com.example.demo.model.Ticket;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
class TicketServiceRaceConditionTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    private User createAndSaveDispatcher(String suffix) {
        String username = "test_dispatcher_" + suffix;
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setPasswordHash("test");
                    u.setRole(UserRole.DISPATCHER);
                    return userRepository.save(u);
                });
    }

    private User createAndSaveMaster(String suffix) {
        String username = "test_master_" + suffix;
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setPasswordHash("test");
                    u.setRole(UserRole.MASTER);
                    return userRepository.save(u);
                });
    }

    private Ticket createNewTicket(String testId) {
        User dispatcher = createAndSaveDispatcher(testId);

        TicketCreateRequest req = new TicketCreateRequest();
        req.setClientName("Client");
        req.setPhone("123");
        req.setAddress("Street");
        req.setProblemText("Race test " + testId);

        return ticketService.createTicket(req, dispatcher);
    }

    @Test
    void onlyOneMasterCanTakeTicketInParallel() throws InterruptedException {
        String testId = String.valueOf(System.currentTimeMillis());
        Ticket ticket = createNewTicket(testId);

        User master1 = createAndSaveMaster(testId + "_1");
        User master2 = createAndSaveMaster(testId + "_2");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        tasks.add(() -> tryTake(ticket.getId(), master1));
        tasks.add(() -> tryTake(ticket.getId(), master2));

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();

        int successCount = 0;
        int failCount = 0;

        for (Future<Boolean> f : results) {
            try {
                if (Boolean.TRUE.equals(f.get())) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (ExecutionException e) {
                // В случае OptimisticLockingFailureException считаем как неуспех
                if (e.getCause() instanceof OptimisticLockingFailureException) {
                    failCount++;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        Assertions.assertEquals(1, successCount, "Ровно один мастер должен успешно взять заявку");
        Assertions.assertEquals(1, failCount, "Второй мастер должен получить ошибку при попытке взять ту же заявку");
    }

    @Transactional
    protected Boolean tryTake(Long ticketId, User master) {
        try {
            ticketService.takeTicket(ticketId, master);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}




