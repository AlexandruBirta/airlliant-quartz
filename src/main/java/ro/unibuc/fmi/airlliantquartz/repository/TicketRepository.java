package ro.unibuc.fmi.airlliantquartz.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.fmi.airlliantmodel.entity.Ticket;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

}