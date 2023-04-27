package ro.unibuc.fmi.airlliantquartz.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.fmi.airlliantmodel.dto.SeatRow;
import ro.unibuc.fmi.airlliantmodel.dto.Seats;
import ro.unibuc.fmi.airlliantmodel.entity.Flight;
import ro.unibuc.fmi.airlliantmodel.entity.Ticket;
import ro.unibuc.fmi.airlliantmodel.entity.User;
import ro.unibuc.fmi.airlliantmodel.exception.ApiException;
import ro.unibuc.fmi.airlliantmodel.exception.ExceptionStatus;
import ro.unibuc.fmi.airlliantquartz.repository.FlightRepository;
import ro.unibuc.fmi.airlliantquartz.repository.TicketRepository;
import ro.unibuc.fmi.airlliantquartz.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;


@Service
@Slf4j
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public Ticket addTicket(Ticket ticket) {

        Long userId = ticket.getUser().getId();
        Long flightId = ticket.getFlight().getId();
        User user;

        try {

            user = userRepository.findByIdAndLock(userId, entityManager);

            Flight flight = flightRepository.findById(flightId).orElseThrow(() -> new ApiException(ExceptionStatus.FLIGHT_NOT_FOUND, String.valueOf(flightId)));

            Seats seats = flight.getSeats();

            Field seatRowField = seats.getClass().getDeclaredField(ticket.getSeatRow());
            seatRowField.setAccessible(true);

            SeatRow seatRowObject = (SeatRow) seatRowField.get(seats);

            Field seatNumberField = seatRowObject.getClass().getDeclaredField(ticket.getSeatNumber());
            seatNumberField.setAccessible(true);
            seatNumberField.setBoolean(seatRowObject, true);

            seatRowField.set(seats, seatRowObject);

            flight.setSeats(seats);

        } catch (NoResultException e1) {
            throw new ApiException(ExceptionStatus.USER_NOT_FOUND, String.valueOf(userId));
        } catch (IllegalAccessException | NoSuchFieldException e2) {
            log.error("Error during seat assignment!", e2);
            throw new ApiException(ExceptionStatus.AIRLLIANT_INTERNAL_ERROR);
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