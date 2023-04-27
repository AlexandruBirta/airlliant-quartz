package ro.unibuc.fmi.airlliantquartz.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.fmi.airlliantmodel.entity.Flight;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

}
