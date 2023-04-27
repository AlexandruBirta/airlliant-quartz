package ro.unibuc.fmi.airlliantquartz.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.fmi.airlliantmodel.entity.Ticket;
import ro.unibuc.fmi.airlliantmodel.entity.User;
import ro.unibuc.fmi.airlliantmodel.exception.ApiException;
import ro.unibuc.fmi.airlliantmodel.exception.ExceptionStatus;
import ro.unibuc.fmi.airlliantquartz.repository.TicketRepository;
import ro.unibuc.fmi.airlliantquartz.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;


@Service
@Slf4j
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public Ticket addTicket(Ticket ticket) {

        Long userId = ticket.getUser().getId();
        User user;

        try {
            user = userRepository.findByIdAndLock(userId, entityManager);
        } catch (NoResultException e) {
            throw new ApiException(ExceptionStatus.USER_NOT_FOUND, String.valueOf(userId));
        }

        log.info("Locked user: {}", user);

        return ticketRepository.save(ticket);

    }

    @Transactional
    public void deleteTicket(Long ticketId) {

        Ticket ticketToDelete = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ApiException(ExceptionStatus.TICKET_NOT_FOUND, String.valueOf(ticketId)));


        log.info("Deleted ticket: {}", ticketToDelete);

    }

}