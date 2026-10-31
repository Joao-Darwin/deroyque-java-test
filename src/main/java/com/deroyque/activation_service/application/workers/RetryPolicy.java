package com.deroyque.activation_service.application.workers;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RetryPolicy {

    public LocalDateTime calculateNextAttempt(int attempts) {
        long delayInSeconds = switch (attempts) {
            case 1 -> 30;
            case 2 -> 60;
            default -> 120;
        };

        return LocalDateTime.now().plusSeconds(delayInSeconds);
    }
}