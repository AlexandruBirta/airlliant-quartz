package ro.unibuc.fmi.airlliantquartz.quartz;


import com.fasterxml.uuid.Generators;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.fmi.airlliantmodel.entity.Ticket;
import ro.unibuc.fmi.airlliantmodel.exception.ApiException;
import ro.unibuc.fmi.airlliantmodel.exception.ExceptionStatus;
import ro.unibuc.fmi.airlliantquartz.repository.TicketRepository;
import ro.unibuc.fmi.airlliantquartz.service.NotificationService;

import java.util.UUID;


@Slf4j
@AllArgsConstructor
public class ProcessTicketNotificationJob extends QuartzJobBean {

    private final QuartzService quartzService;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final BuildProperties buildProperties;

    @Timed("airlliant_quartz_process_ticket_job_execute_internal")
    @Override
    @Transactional
    public void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {

        try {

            UUID uuid = Generators.timeBasedGenerator().generate();
            String correlationId = uuid.toString().replace("-", "");
            String applicationName = buildProperties.getArtifact() + "-" + buildProperties.getVersion();
            MDC.put("Correlation-Id", correlationId);
            MDC.put("Application-Name", applicationName);

            String jobName = jobExecutionContext.getJobDetail().getKey().getName();
            String schedulerId = jobExecutionContext.getScheduler().getSchedulerInstanceId();
            String triggerName = jobExecutionContext.getTrigger().getKey().getName();

            Long ticketId = Long.parseLong(triggerName);

            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                    () -> new ApiException(ExceptionStatus.TICKET_NOT_FOUND, String.valueOf(ticketId)));

            notificationService.notifyUser(ticket.getUser());

            log.info("Executed job '{}' fired from trigger '{}' by scheduler with id '{}'.", jobName, triggerName, schedulerId);

        } catch (ApiException e) {
            log.error(e.getErrorMessage());
            String triggerName = jobExecutionContext.getTrigger().getKey().getName();
            quartzService.deleteTrigger(Long.parseLong(triggerName));
        } catch (Exception e) {
            log.error("Error during ticket job processing.", e);
        }

    }

}