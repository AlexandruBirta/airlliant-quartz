package ro.unibuc.fmi.airlliantquartz.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import ro.unibuc.fmi.airlliantmodel.entity.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;


@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

    public static final String EMAIL_TEMPLATE_PATH = "classpath:templates/email-template.html";
    private static final String SUBJECT_TEMPLATE = "Flight %s Departure Reminder";

    private final JavaMailSender emailSender;

    public void notifyUser(User user, String flightNumber) {

        String userEmail = user.getEmail();
        MimeMessage message = emailSender.createMimeMessage();

        try {

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@baeldung.com");
            helper.setTo(userEmail);
            helper.setSubject(String.format(SUBJECT_TEMPLATE, flightNumber));
            helper.setText(String.format(getEmailTemplate(), user.getFirstName(), user.getLastName(), flightNumber), true);

            emailSender.send(message);

        } catch (MessagingException e) {
            log.error("Error while notifying user with email '{}'.", userEmail, e);
        }

        log.info("Notified user with email {}.", userEmail);

    }

    public static String getEmailTemplate() {

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(EMAIL_TEMPLATE_PATH);

        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }


}