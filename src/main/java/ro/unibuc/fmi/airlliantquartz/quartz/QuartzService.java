package ro.unibuc.fmi.airlliantquartz.quartz;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.fmi.airlliantmodel.entity.Ticket;
import ro.unibuc.fmi.airlliantmodel.exception.ApiException;
import ro.unibuc.fmi.airlliantmodel.exception.ExceptionStatus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


@Slf4j
@Service
@AllArgsConstructor
public class QuartzService {

    private static final JobKey TICKET_JOB_KEY = new JobKey(ProcessTicketNotificationJob.class.getSimpleName(), "airlliant_ticket_jobs");
    private static final String TICKET_TRIGGER_GROUP = "airlliant_ticket_triggers";
    private static final boolean IS_REPLACED = false;

    @Value(value = "${spring.jpa.properties.hibernate.jdbc.time_zone}")
    private final String timeZone;
    private final Scheduler scheduler;

    @PostConstruct
    public void init() {

        try {
            scheduler.start();

            if (scheduler.getJobDetail(TICKET_JOB_KEY) == null) {

                JobDetail jobDetail = newJob(ProcessTicketNotificationJob.class)
                        .withIdentity(TICKET_JOB_KEY)
                        .storeDurably()
                        .build();

                scheduler.addJob(jobDetail, IS_REPLACED);

            }

        } catch (SchedulerException e) {
            log.error(String.valueOf(e));
        }

    }

    @PreDestroy
    public void close() {

        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            log.error(String.valueOf(e));
        }

    }

    @Transactional
    public void addTrigger(Ticket ticket) {

        try {

            String triggerName = String.valueOf(ticket.getId());
            TriggerKey ticketTriggerKey = new TriggerKey(triggerName, TICKET_TRIGGER_GROUP);

            if (scheduler.getTrigger(ticketTriggerKey) == null) {

                Trigger trigger = makeTrigger(ticket, ticketTriggerKey);
                scheduler.scheduleJob(trigger);

                log.info("Trigger for ticket with id '{}' added.", ticket.getId());
                log.info("Trigger start time: {}, end time: {}", trigger.getStartTime(), trigger.getEndTime());

            }

        } catch (SchedulerException e) {
            log.error(String.valueOf(e));
            throw new ApiException(ExceptionStatus.ADD_TICKET_TRIGGER_ERROR, String.valueOf(ticket.getId()));
        }

    }

    private Trigger makeTrigger(Ticket ticket, TriggerKey ticketTriggerKey) {

        LocalDateTime startDate = LocalDateTime.now().plusSeconds(30);


//        LocalDateTime startDate = ticket.getFlight().getDepartureDate().minusDays(3);
        Date triggerStartDate = Date.from(startDate.atZone(ZoneId.of(timeZone)).toInstant());

        return newTrigger()
                .withIdentity(ticketTriggerKey)
                .forJob(TICKET_JOB_KEY)
                .startAt(triggerStartDate)
                .build();

    }

    @Transactional
    public void deleteTrigger(Long ticketId) {

        try {

            String triggerName = ticketId.toString();
            TriggerKey ticketTriggerKey = new TriggerKey(triggerName, TICKET_TRIGGER_GROUP);

            if (scheduler.getTrigger(ticketTriggerKey) != null) {

                scheduler.unscheduleJob(ticketTriggerKey);
                log.info("Trigger for ticket with id '{}' deleted.", ticketId);

            }

        } catch (SchedulerException e) {
            log.error(String.valueOf(e));
            throw new ApiException(ExceptionStatus.DELETE_TICKET_TRIGGER_ERROR, String.valueOf(ticketId));
        }

    }

}