package ro.unibuc.fmi.airlliantquartz.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.fmi.airlliantmodel.entity.Ticket;
import ro.unibuc.fmi.airlliantquartz.quartz.QuartzService;
import ro.unibuc.fmi.airlliantquartz.service.TicketService;

import javax.validation.Valid;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/airlliant/v1/scheduler")
public class QuartzTicketController {

    private final QuartzService quartzService;
    private final TicketService ticketService;

    @Transactional
    @PostMapping(value = "/tickets",
            produces = {"application/json"},
            consumes = {"application/json"})
    public void addTicketTrigger(@Valid @RequestBody Ticket ticket) {
        Ticket savedTicket = ticketService.addTicket(ticket);
        log.info("Added ticket: {}", savedTicket);
        quartzService.addTrigger(savedTicket);
    }

    @Transactional
    @DeleteMapping(value = "/tickets/{ticketId}",
            produces = {"application/json"},
            consumes = {"application/json"})
    public void deleteTicketTrigger(@PathVariable(value = "ticketId") Long ticketId) {
        ticketService.deleteTicket(ticketId);
        quartzService.deleteTrigger(ticketId);
    }

}