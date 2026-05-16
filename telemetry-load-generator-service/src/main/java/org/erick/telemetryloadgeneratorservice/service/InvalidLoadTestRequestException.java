package org.erick.telemetryloadgeneratorservice.service;

public class InvalidLoadTestRequestException extends RuntimeException {

    public InvalidLoadTestRequestException(String message) {
        super(message);
    }
}
