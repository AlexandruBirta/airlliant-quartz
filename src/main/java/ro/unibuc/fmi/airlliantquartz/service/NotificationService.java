package ro.unibuc.fmi.airlliantquartz.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.unibuc.fmi.airlliantmodel.entity.User;


@Slf4j
@Service
public class NotificationService {

    //TODO add email notification implementation

    public void notifyUser(User user) {
        log.info("Notified user with email {}.", user.getEmail());
    }


}